package testdata.sources.zc.includes


internal fun includeStdMem() = """
    fn alloc(size: int): [void] {
        asm{"
            aloadi 0
            alloc
            ret
        "}
    }

    fn free(memory: [void]): int {
        asm {"
            aloadi 0
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