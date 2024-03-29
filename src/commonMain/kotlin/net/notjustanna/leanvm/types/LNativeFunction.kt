package net.notjustanna.leanvm.types

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.context.LeanContext
import net.notjustanna.leanvm.context.LeanMachineControl
import net.notjustanna.leanvm.context.LeanRuntime

public abstract class LNativeFunction(
    override val name: String? = null,
    private val runtime: LeanRuntime? = null,
) : LFunction() {

    protected abstract fun run(thisValue: LAny?, args: List<LAny>): LAny

    override fun setupContext(
        control: LeanMachineControl,
        thisValue: LAny?,
        args: List<LAny>,
        runtime: LeanRuntime?,
    ): LeanContext {
        return Context(control, runtime ?: this.runtime ?: LeanRuntime(), this, thisValue, args)
    }

    public class Context(
        private val control: LeanMachineControl,
        override val runtime: LeanRuntime,
        private val function: LNativeFunction,
        private val thisValue: LAny? = null,
        private val args: List<LAny>,
    ) : LeanContext {
        override fun step() {
            control.onReturn(function.run(thisValue, args))
        }

        override fun onReturn(value: LAny) {
            control.onReturn(value) // Keep cascading.
        }

        override fun onThrow(value: LAny) {
            control.onThrow(value) // Keep cascading.
        }

        override fun trace(): StackTrace {
            return StackTrace(function.name ?: "<anonymous function>")
        }
    }

    public companion object {
        public fun of(
            name: String? = null,
            runtime: LeanRuntime? = null,
            block: (thisValue: LAny?, args: List<LAny>) -> LAny,
        ): LNativeFunction {
            return object : LNativeFunction(name, runtime) {
                override fun run(thisValue: LAny?, args: List<LAny>): LAny {
                    return block(thisValue, args)
                }
            }
        }
    }
}
