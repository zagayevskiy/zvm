package testdata.sources.zc.includes

internal fun includeCrash() = """
    fn crash(code: int) {
        asm{"
            lloadi code
            crash
        "}
    }
""".trimIndent()