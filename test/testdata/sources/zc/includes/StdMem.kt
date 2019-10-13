package testdata.sources.zc.includes


internal fun includeStdMem() = """
    fn alloc(size: int): [void] {
        asm{"
            lloadi size
            alloc
            ret
        "}
    }

    fn free(memory: [void]): int {
        asm {"
            lloadi memory
            free
            consti 0
            ret
        "}
    }

    fn null(): [void] {
        asm {"
            consti 0
            ret
        "}
    }
"""