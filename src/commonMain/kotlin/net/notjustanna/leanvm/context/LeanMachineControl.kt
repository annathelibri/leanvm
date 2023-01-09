package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny

/**
 * Interface implemented by [net.notjustanna.leanvm.LeanMachine] in order to provide a way for [LeanContext]s to
 * interact with the machine.
 */
public interface LeanMachineControl {
    /**
     * Signals the machine to stop execution of the current context and push the given context to the top of the
     * execution stack.
     *
     * Once the pushed context returns or throws an exception, the machine will call the appropriate [LeanContext.onReturn]
     * or [LeanContext.onThrow] method, and then resume execution of the current context.
     */
    public fun push(layer: LeanContext)

    /**
     * Signals the machine to stop execution of the current context and replace it with the given context.
     *
     * The pushed context will act as if it was the current context.
     */
    public fun replace(layer: LeanContext)

    /**
     * Signals the machine to stop execution of the current context and pop it from the execution stack.
     *
     * The machine will call [LeanContext.onReturn] of the new current context, and then resume execution of it.
     */
    public fun onReturn(value: LAny)

    /**
     * Signals the machine to stop execution of the current context and pop it from the execution stack.
     *
     * The machine will call [LeanContext.onThrow] of the new current context, and then resume execution of it.
     */
    public fun onThrow(value: LAny)

    /**
     * Generates a new stack trace list of the entire execution stack.
     *
     * The stack trace is generated by calling [LeanContext.trace] on each context in the stack.
     */
    public fun stackTrace(): List<StackTrace>
}