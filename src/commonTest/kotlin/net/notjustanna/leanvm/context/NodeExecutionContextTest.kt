package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.bytecode.builder.LeanCodeBuilder
import kotlin.test.Test
import kotlin.test.assertFails

class NodeExecutionContextTest {
    @Test
    fun malformedPopScope() {
        val code = LeanCodeBuilder()
        val node = code.newNodeBuilder()
        node.popScopeInsn()

        val c = NodeExecutionContext(DummyLeanMachineControl(), code.build())
        assertFails {
            c.step()
        }
    }
}
