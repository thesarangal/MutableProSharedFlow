# MutableProSharedFlow
This is a Kotlin class implementation of [MutableSharedFlow] with an initial value. Shared flows are a type of flow that allows multiple collectors to receive the same stream of data. This implementation is called MutableProSharedFlow and extends the MutableSharedFlow class. It allows the user to set an initial value when creating the flow.
Actually this class is mixer of MutableSharedFlow and StateFlow.

## Parameters

The class takes in the following parameters:

- `initialValue`: the initial value of the flow
- `replay`: the number of values that will be buffered and replayed to new collectors when they start collecting
- `extraBufferCapacity`: additional buffer capacity for buffering values that can be collected later
- `onBufferOverflow`: the strategy to use when the buffer overflows, the default strategy is to suspend

The class has the following functions:

- `collect`: accepts a `FlowCollector` and emits values to it. If the replay cache is empty, the initial value is emitted first.
- `emit`: emits a value to the shared flow and updates the initial value
- `resetReplayCache`: resets the replay cache to an empty state
- `tryEmit`: tries to emit a value to the shared flow without suspending. If it succeeds, it updates the initial value
- `subscriptionCount`: returns the number of subscribers to the shared flow
- `replayCache`: returns a snapshot of the replay cache
The `collect`, `emit`, `resetReplayCache`, and `tryEmit` functions are overridden from the `MutableSharedFlow` class. The `subscriptionCount` and `replayCache` functions are deprecated in the `MutableSharedFlow` class and are therefore overridden in this implementation.

## Benefits
- Value will be collect even if new value is same as last value. [Property of SharedFlow]
- Flow has initial value [Property of StateFlow].
By use of getValue() method.

## Contributions

Contributions to this library are welcome. If you find a bug or have a feature request,
please open an issue on the [GitHub repository](https://github.com/thesarangal/MutableProSharedFlow).

## License

This library is released under the [MIT License](https://opensource.org/licenses/MIT).
