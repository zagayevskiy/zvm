package testdata.sources.zc

import testdata.cases.TestSource

object Print {

    val HelloStrings = listOf("Hello!",
            "'Hello' in russian из Привет!",
            "Hello по-гречески: Γεια!",
            "Japan: 今日は。!",
            "Qazaqsha: Сәлем!",
            "Кхмерский: ជំរាបសួរ!")

    val HelloStringLiteral = TestSource("Hello",
            """
                fn out(string: [byte]): int {
                    asm{"
                        lloadi string
                        out
                    "}
                    return 0;
                }

                fn main(): int {

            """.trimIndent() +
                    HelloStrings.joinToString(separator = "\n") { hello ->
                        """     out("$hello");"""
                    } +
                    """
                            return 0;
                        }
                    """.trimIndent()
    )

}