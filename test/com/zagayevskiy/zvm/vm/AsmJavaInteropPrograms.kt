package com.zagayevskiy.zvm.vm

class NewInteropTestClass


private val clazzName = NewInteropTestClass::class.java.name
private val clazzNameBytes = clazzName.toByteArray(Charsets.UTF_8)

internal val javaNewTest = """

    .fun main: locals = 1
    consti ${clazzNameBytes.size}
    alloc
${clazzNameBytes.mapIndexed { index, byte ->
            listOf(
                    "dup",
                    "consti $index",
                    "constb $byte",
                    "mstorb")
        }
        .flatten()
        .joinToString(separator = "\n"){"    $it"}
    }

    dup
    lstori 0
    consti ${clazzNameBytes.size}
    jnew 0
    lloadi 0
    free
    """.trimIndent()

//fun main(args: Array<String>) {
//
//
//    println(asm)
//
//}