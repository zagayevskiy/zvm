package testsrc.asm

import com.zagayevskiy.zvm.vm.Source


object AsmSimple {

    val JustRet0 = Source("Just ret 0", """
        .fun main
        consti 0
        ret
    """.trimIndent())

    val JustRetArg = Source("Just ret arg", """
        .fun main: x: int;
        pushfp
        consti x
        mloadi
        ret
    """.trimIndent())
}
