package testdata.sources.zc

import testdata.cases.TestSource
import testdata.sources.zc.includes.includeStdIo
import testdata.sources.zc.includes.includeStdMem

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

    val ExpectedInts = (Int.MIN_VALUE until Int.MAX_VALUE step Int.MAX_VALUE / 32).toList()

    val Ints = TestSource("ints", """
        ${includeStdIo()}
        ${includeStdMem()}

        fn main(): int {
            val buffer: [byte] = alloc(32);
            ${
                ExpectedInts.joinToString(separator = "\n") { int ->
                    "       itos($int, buffer); print(buffer);"
                }
            }

            free(buffer);
            return 0;
        }

    """.trimIndent())

    val ExternalInt = TestSource("external int", """
        fn main(x: int): int {
            val buffer: [byte] = alloc(32);
            itos(x, buffer);
            print(buffer);
            free(buffer);
            return 0;
        }

        ${includeStdIo()}
        ${includeStdMem()}
    """.trimIndent())

}