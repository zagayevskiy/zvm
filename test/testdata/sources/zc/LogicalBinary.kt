package testdata.sources.zc

import testdata.cases.TestSource

object LogicalBinary {
    val CountPositives = TestSource("count positives", """

        fn main(x: int, y: int, z: int): int {
            if (x > 0 && y > 0 && z > 0) return 3;
            if ((x > 0 && y > 0) || (x > 0 && z > 0) || (y > 0 && z > 0)) return 2;
            if (x > 0 || y > 0 || z > 0 ) return 1;
            return 0;
        }

    """.trimIndent())

    val AndLazy = TestSource("and lazy", """
        fn main(): int {
            if (false && crash()) return 1;
            if (true && false && crash()) return 2;
            if (true && true && false && crash()) return 3;
            return 0;
        }

        fn crash(): bool {
            asm {"
                consti 0
                crash
            "}
        }
    """.trimIndent())

    val OrLazy = TestSource("and lazy", """
        fn main(): int {
            if (true || crash()) {
                if (false || true || crash()) {
                    if (false || false || true || crash()) {
                        if (false || false || false || true || crash()) return 0;
                    }
                }
            }
            return 1;
        }

        fn crash(): bool {
            asm {"
                consti 0
                crash
            "}
        }
    """.trimIndent())
}