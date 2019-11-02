package testdata.sources.zc.includes

internal fun includeCrash() = """
    fn crash(code: int) {
        asm{"
            lloadi code
            crash
        "}
    }

    fn crashm(code: int, message: [byte]) {
        crashPrint(message);
        crash(code);
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

    fn assertByteEq(expect: byte, actual: byte, message: [byte]) {
        if (expect != actual) {
            crashPrint(message);
            crash(cast<int>(actual));
        }
    }

    fn assertRefEq(expect: [void], actual: [void], message: [byte]) {
        assertIntEq(cast<int>(expect), cast<int>(actual), message);
    }

    fn assertTrue(value: bool, message: [byte]) {
        if (!value) {
            crashPrint(message);
            crash(0);
        }
    }
""".trimIndent()