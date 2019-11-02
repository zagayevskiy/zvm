package testdata.sources.zc.includes

internal fun includeStdIo() = """
    fn print(string: [byte]) {
        asm{"
            lloadi string
            out
        "}
    }

    fn itos(value: int, buffer: [byte]) {
        var cursor: int;
        var v: int;
        val zero: byte = 48;
        if (value == 0) {
            cast<[int]>(buffer)[0] = 1;
            buffer[4] = zero;
            return;
        }

        if (value > 0) {
            v = value;
            cursor = 4;
        } else {
            v = -1*value;
            buffer[4] = 45;
            cursor = 5;
        }

        while(v != 0) {
            buffer[cursor] = zero + cast<byte>(v % 10);
            v = v / 10;
            cursor = cursor + 1;
        }
        cursor = cursor - 1;
        cast<[int]>(buffer)[0] = cursor;
        for (var i = 4; i < cursor; i = i + 1, cursor = cursor - 1) {
            val tmp = buffer[cursor];
            buffer[cursor] = buffer[i];
            buffer[i] = tmp;
        }
    }
""".trimIndent()