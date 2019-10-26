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

    val HelloStrings = listOf("Hello!", "'Hello' in russian из Привет!", "Hello по-гречески: Γεια!", "Japan: 今日は。!", "Qazaqsha: Сәлем!", "Кхмерский: ជំរាបសួរ!")

    val PrintHelloFromPool = TestSource("Hello",

            ".fun main" +
                    HelloStrings.mapIndexed { index, s ->
                        """

                            .pool entry$index "$s"
                            pushcp
                            consti entry$index
                            addi
                            out

                        """.trimIndent()
                    }.joinToString(separator = "")

                    + """
                        consti 0
                        ret
                    """.trimIndent()
    )

}
