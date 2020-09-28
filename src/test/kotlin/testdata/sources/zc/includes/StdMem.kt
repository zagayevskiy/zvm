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
        asm{"
            lloadi src
            lloadi dst
            lloadi count
            memcp
        "}
    }

    fn itos(value: int, buffer: [byte]) {
        var cursor: int;
        var v: int;
        val zero: byte = 48;
        when(value) {
            0 -> {
                cast<[int]>(buffer)[0] = 1;
                buffer[4] = zero;
                return;
            }
            -2147483648 -> {
                copy("-2147483648", buffer, 15);
                return;
            }
        }

        var digitsBegin: int;
        if (value > 0) {
            v = value;
            digitsBegin = 4;
        } else {
            v = -1*value;
            buffer[4] = 45;
            digitsBegin = 5;
        }
        cursor = digitsBegin;

        while(v != 0) {
            buffer[cursor] = zero + cast<byte>(v % 10);
            v = v / 10;
            cursor = cursor + 1;
        }
        cast<[int]>(buffer)[0] = (cursor - 4);
        cursor = cursor - 1;
        for (var i = digitsBegin; i < cursor; i = i + 1, cursor = cursor - 1) {
            val tmp = buffer[cursor];
            buffer[cursor] = buffer[i];
            buffer[i] = tmp;
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