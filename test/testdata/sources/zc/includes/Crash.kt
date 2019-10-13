package testdata.sources.zc.includes

internal fun includeCrash() = """
    fn crash(code: int) {
        asm{"
            aloadi 0
            crash
        "}
    }
""".trimIndent()