import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

/**
 * This class represent the implementation of [MutableSharedFlow] but have initial value.
 *
 * See the [MutableSharedFlow] documentation for details on shared flows.
 *
 * @author Rajat Sarangal
 * @since March 25, 2022 (Created)
 * @since March 26, 2022 (Updated)
 * @link https://github.com/thesarangal/MutableProSharedFlow/blob/master/MutableProSharedFlow.kt
 */
class MutableProSharedFlow<T>(
    private var initialValue: T,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : MutableSharedFlow<T> {

    // Data Members
    private val flow = MutableSharedFlow<T>(replay, extraBufferCapacity, onBufferOverflow)

    /**
     * Accepts the given [collector] and [emits][FlowCollector.emit] values into it.
     * This method should never be implemented or used directly.
     *
     * The only way to implement the `Flow` interface directly is to extend [AbstractFlow].
     * To collect it into a specific collector, either `collector.emitAll(flow)` or `collect { ... }` extension
     * should be used. Such limitation ensures that the context preservation property is not violated and prevents most
     * of the developer mistakes related to concurrency, inconsistent flow dispatchers and cancellation.
     */
    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {

        if (replayCache().isEmpty()) {
            collector.emit(initialValue)
        }

        flow.collect(collector)
    }

    /**
     * Emits a [value] to this shared flow, suspending on buffer overflow if the shared flow was created
     * with the default [BufferOverflow.SUSPEND] strategy.
     *
     * See [tryEmit] for a non-suspending variant of this function.
     *
     * This method is **thread-safe** and can be safely invoked from concurrent coroutines without
     * external synchronization.
     */
    override suspend fun emit(value: T) {
        flow.emit(value).also {
            initialValue = value
        }
    }

    /**
     * Resets the [replayCache] of this shared flow to an empty state.
     * New subscribers will be receiving only the values that were emitted after this call,
     * while old subscribers will still be receiving previously buffered values.
     * To reset a shared flow to an initial value, emit the value after this call.
     *
     * On a [MutableStateFlow], which always contains a single value, this function is not
     * supported, and throws an [UnsupportedOperationException]. To reset a [MutableStateFlow]
     * to an initial value, just update its [value][MutableStateFlow.value].
     *
     * This method is **thread-safe** and can be safely invoked from concurrent coroutines without
     * external synchronization.
     *
     * **Note: This is an experimental api.** This function may be removed or renamed in the future.
     */
    @ExperimentalCoroutinesApi
    override fun resetReplayCache() = flow.resetReplayCache()

    /**
     * Tries to emit a [value] to this shared flow without suspending. It returns `true` if the value was
     * emitted successfully. When this function returns `false`, it means that the call to a plain [emit]
     * function will suspend until there is a buffer space available.
     *
     * A shared flow configured with a [BufferOverflow] strategy other than [SUSPEND][BufferOverflow.SUSPEND]
     * (either [DROP_OLDEST][BufferOverflow.DROP_OLDEST] or [DROP_LATEST][BufferOverflow.DROP_LATEST]) never
     * suspends on [emit], and thus `tryEmit` to such a shared flow always returns `true`.
     *
     * This method is **thread-safe** and can be safely invoked from concurrent coroutines without
     * external synchronization.
     */
    override fun tryEmit(value: T): Boolean {
        return flow.tryEmit(value).also { result ->
            if (result) {
                initialValue = value
            }
        }
    }

    /**
     * The number of subscribers (active collectors) to this shared flow.
     *
     * The integer in the resulting [StateFlow] is not negative and starts with zero for a freshly created
     * shared flow.
     *
     * This state can be used to react to changes in the number of subscriptions to this shared flow.
     * For example, if you need to call `onActive` when the first subscriber appears and `onInactive`
     * when the last one disappears, you can set it up like this:
     *
     * ```
     * sharedFlow.subscriptionCount
     *     .map { count -> count > 0 } // map count into active/inactive flag
     *     .distinctUntilChanged() // only react to true<->false changes
     *     .onEach { isActive -> // configure an action
     *         if (isActive) onActive() else onInactive()
     *     }
     *     .launchIn(scope) // launch it
     *
     * Deprecated:
     * See [subscriptionCount]
     */
    @Deprecated("Use subscriptionCount() instead")
    override val subscriptionCount: StateFlow<Int>
        get() = flow.subscriptionCount

    /**
     * A snapshot of the replay cache.
     *
     * Deprecated:
     * See [replayCache]
     */
    @Deprecated("Use replayCache() instead")
    override val replayCache: List<T>
        get() = flow.replayCache

    /**
     * @return Initial Value
     * */
    fun getValue(): T = initialValue

    /**
     * Resets the replayCache of this shared flow to an initial state
     * */
    fun reset(value: T) {
        initialValue = value
        flow.resetReplayCache()
    }

    /**
     * A snapshot of the replay cache.
     */
    fun replayCache() = flow.replayCache

    /**
     * The number of subscribers (active collectors) to this shared flow.
     *
     * The integer in the resulting [StateFlow] is not negative and starts with zero for a freshly created
     * shared flow.
     *
     * This state can be used to react to changes in the number of subscriptions to this shared flow.
     * For example, if you need to call `onActive` when the first subscriber appears and `onInactive`
     * when the last one disappears, you can set it up like this:
     *
     * ```
     * sharedFlow.subscriptionCount
     *     .map { count -> count > 0 } // map count into active/inactive flag
     *     .distinctUntilChanged() // only react to true<->false changes
     *     .onEach { isActive -> // configure an action
     *         if (isActive) onActive() else onInactive()
     *     }
     *     .launchIn(scope) // launch it
     * ```
     */
    fun subscriptionCount() = flow.subscriptionCount
}

/**
 * Represents this mutable shared flow as a read-only shared flow.
 *
 * To Use this extension: Implement like following
 *
 * private val _loadingStatus = MutableProSharedFlow(LoadingState.IDLE)
 * val loadingStatus: SharedFlow<LoadingState> = _loadingStatus
 *
 * NOT Like:
 *
 * private val _loadingStatus = MutableProSharedFlow(LoadingState.IDLE)
 * val loadingStatus = _loadingStatus.asSharedFlow()
 * */
fun <T> SharedFlow<T>.getValue(defaultValue: T): T {
    if (this is MutableProSharedFlow) {
        return getValue()
    }
    return defaultValue
}
