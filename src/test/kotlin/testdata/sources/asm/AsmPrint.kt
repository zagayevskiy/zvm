package testdata.sources.asm

import testdata.cases.TestSource

object AsmPrint {

    val HelloStrings = listOf("Hello!",
            "'Hello' in russian из Привет!",
            "Hello по-гречески: Γεια!",
            "Japan: 今日は。!",
            "Татарча: Сәлам!",
            "Кхмерский: ជំរាបសួរ!",
            "Сингальский(wat?): ආයුබෝවන්!")

    val HelloFromPool = TestSource("Hello",
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