package testdata.sources.zc.includes

internal fun includeCrash() = """
    fn crash(code: int) {
        asm{"
            lloadi code
            crash
        "}
    }

    fn crashPrint(message: [byte]) {
        asm{"
            lloadi message
            out
        "}
    }

    fn assertIntEq(expect: int, actual: int, message: [byte]) {
        if (expect != actual) {
            crashPrint(message);
            crash(actual);
        }
    }
""".trimIndent()