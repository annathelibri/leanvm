package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.bytecode.LeanNode
import net.notjustanna.leanvm.exceptions.MalformedBytecodeException
import net.notjustanna.leanvm.exceptions.MismatchedFunctionArgsException
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LCompiledFunction

public class FunctionSetupContext(
    private val control: LeanMachineControl,
    private val function: LCompiledFunction,
    override val runtime: LeanRuntime,
    private val thisValue: LAny? = null,
    arguments: List<LAny>,
) : LeanContext {
    private val body: LeanNode = function.source.nodeArr.getOrElse(function.data.bodyId) {
        throw MalformedBytecodeException(
            "Tried to load non-existent function body at index ${function.data.bodyId}.",
            control.stackTrace()
        )
    }
    private val scope: Scope = Scope(function.rootScope)
    private val paramCount: Int = function.data.paramArr.size
    private var paramNext: Int = 0
    private val argsLeft: MutableList<LAny> = arguments.toMutableList()
    private var resolvedParamName: String? = null

    override fun step() {
        while (paramNext < paramCount) {
            val parameter = function.data.paramArr.getOrElse(paramNext++) {
                throw MalformedBytecodeException(
                    "Tried to load function parameter $paramNext which wasn't defined.",
                    control.stackTrace()
                )
            }
            val value = argsLeft.removeFirstOrNull()

            val paramName = function.source.sConstArr.getOrElse(parameter.nameConst) {
                throw MalformedBytecodeException(
                    "Tried to load string constant ${parameter.nameConst} which wasn't defined.",
                    control.stackTrace()
                )
            }
            // TODO Not yet implemented: varargs parameter

            if (value != null) {
                scope.define(paramName, true, value)
                continue
            }

            if (parameter.defaultValueNodeId != -1) {
                val paramBody = function.source.nodeArr.getOrElse(parameter.defaultValueNodeId) {
                    throw MalformedBytecodeException(
                        "Tried to load non-existent default parameter body at index ${function.data.bodyId}.",
                        control.stackTrace()
                    )
                }
                resolvedParamName = paramName
                scope.define(paramName, true)
                control.push(
                    NodeExecutionContext(
                        control,
                        function.source,
                        function.name ?: "<anonymous function>",
                        thisValue,
                        runtime,
                        scope,
                        paramBody,
                    )
                )
                return
            }

            throw MismatchedFunctionArgsException(
                "Incorrect number of arguments for function ${function.name}",
                control.stackTrace()
            )
        }

        control.replace(
            NodeExecutionContext(
                control,
                function.source,
                function.name ?: "<anonymous function>",
                thisValue,
                runtime,
                Scope(scope),
                body,
            )
        )
    }

    override fun onReturn(value: LAny) {
        val paramName = resolvedParamName ?: error("resolvedParamName should not be null")
        scope.set(paramName, value)
    }

    override fun onThrow(value: LAny) {
        control.onThrow(value) // Keep cascading.
    }

    override fun trace(): StackTrace? {
        return null
    }
}
