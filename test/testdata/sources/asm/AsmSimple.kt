package testdata.sources.asm

import testdata.cases.TestSource


object AsmSimple {

    val JustRet0 = TestSource("Just ret 0", """
        .fun main
        consti 0
        ret
    """.trimIndent())

    val JustRetArg = TestSource("Just ret arg", """
        .fun main: x: int
        pushfp
        consti x
        mloadi
        ret
    """.trimIndent())
}
