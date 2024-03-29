package net.notjustanna.leanvm.runtimes.functions

import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LMetaObject
import net.notjustanna.leanvm.types.LNativeFunction
import net.notjustanna.leanvm.types.LObject

internal object FnCreateMetaObject : LNativeFunction("createMetaObject") {
    override fun run(thisValue: LAny?, args: List<LAny>): LAny {
        val value = if (thisValue != null) {
            // Called as an extension function.
            if (args.isNotEmpty()) {
                throw IllegalArgumentException("createMetaObject() takes no arguments.")
            } else {
                thisValue
            }
        } else {
            // Called as a static function.
            if (args.size != 1) {
                throw IllegalArgumentException("createMetaObject() takes exactly one argument.")
            } else {
                args[0]
            }
        }

        if (value !is LObject) {
            throw IllegalArgumentException("createMetaObject() can only be called on objects.")
        }

        return LMetaObject(value.value)
    }
}
