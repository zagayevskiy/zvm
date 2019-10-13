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

    fn copy(src: [void], dst: [void], count: int): int {
        val srcBytes: [byte] = src;
        val dstBytes: [byte] = dst;
        for (var i = 0; i < count; i = i + 1) {
            dstBytes[i] = srcBytes[i];
        }
        return 0;
    }
"""