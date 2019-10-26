package testdata.sources.zc.includes

internal fun includeStdIo() = """
    fn print(string: [byte]) {
        asm{"
            lloadi string
            out
        "}
    }
""".trimIndent()