package testsrc.asm

import com.zagayevskiy.zvm.vm.Source


object AsmSimple {

    val JustRet0 = Source("just ret 0", """
        .fun main
        consti 0
        ret
    """.trimIndent())

    val JustRetArg = Source("just ret arg", """
        .fun main: x: int;
        pushfp
        consti x
        mloadi
        ret
    """.trimIndent())

    val Sum = Source("x+y", """
        .fun main: x: int, y: int;
        pushfp
        consti x
        mloadi
        pushfp
        consti y
        mloadi
        addi
        ret
    """.trimIndent())

}