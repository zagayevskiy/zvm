package testdata.sources.zc.includes

internal fun includeStdIo() = """
    fn outInt(value: int): int {
        asm{"
            lloadi value
            out
        "}
        return 0;
    }

    fn outByte(value: byte): int {
        asm{"
            lloadb value
            out
        "}
        return 0;
    }
""".trimIndent()