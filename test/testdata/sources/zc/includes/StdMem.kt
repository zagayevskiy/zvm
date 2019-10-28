package testdata.sources.zc.includes


internal fun includeStdMem() = """
    fn alloc(size: int): [void] {
        asm{"
            lloadi size
            alloc
            ret
        "}
    }

    fn free(memory: [void]) {
        asm {"
            lloadi memory
            free
        "}
    }

    fn copy(src: [void], dst: [void], count: int) {
        val srcBytes: [byte] = src;
        val dstBytes: [byte] = dst;
        for (var i = 0; i < count; i = i + 1) {
            dstBytes[i] = srcBytes[i];
        }
    }

    fn orderStrings(left: [byte], right: [byte]): byte {
        val leftLength = stringLength(left);
        val rightLength = stringLength(right);

        if (leftLength > rightLength) return 1;
        if (leftLength < rightLength) return -1;

        for(var i = 0; i < leftLength; i = i + 1) {
            if (left[i] > right[i]) return 1;
            if (left[i] < right[i]) return -1;
        }

        return 0;
    }

    fn stringLength(string: [byte]): int {
        return cast<[int]>(string)[0];
    }
"""