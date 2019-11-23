package testdata.sources.zc.includes

internal fun includeStack() = """
    struct Stack {
        var stack: [void];
        var size: int;
        var top: int;
    }

    fn createStack(size: int): Stack {
        val result: Stack = alloc(sizeof<Stack>);
        result.stack = alloc(size * sizeof<int>);
        result.size = size;
        result.top = 0;
        return result;
    }

    fn pushInt(stack: Stack, value: int) {
        val top = stack.top;
        val newTop = top + sizeof<int>;
        if (newTop >= stack.size) crash(101);
        cast<[int]>(stack.stack + top)[0] = value;
        stack.top = newTop;
    }

    fn popInt(stack: Stack): int {
        val newTop = stack.top - sizeof<int>;
        if (newTop < 0) crash(1002);
        stack.top = newTop;
        return cast<[int]>(stack.stack + newTop)[0];
    }

    fn peekInt(stack: Stack): int {
        if (stack.top == 0) crash(103);
        return cast<[int]>(stack.stack + stack.top - sizeof<int>)[0];
    }

    fn pushByte(stack: Stack, value: byte) {
        val top = stack.top;
        val newTop = top + sizeof<byte>;
        if (newTop >= stack.size) crash(104);
        cast<[byte]>(stack.stack + top)[0] = value;
        stack.top = newTop;
    }

    fn popByte(stack: Stack): byte {
        val newTop = stack.top - sizeof<byte>;
        if (newTop < 0) crash(105);
        stack.top = newTop;
        return cast<[byte]>(stack.stack)[newTop];
    }

    fn peekByte(stack: Stack): byte {
        if (stack.top == 0) crash(106);
        return cast<[byte]>(stack.stack)[stack.top - sizeof<byte>];
    }

    fn drop(stack: Stack, count: int) {
        stack.top = stack.top - count;
    }

    fn pushAll(stack: Stack, values: [void], size: int) {
        val top = stack.top;
        if (top >= stack.size) crash(104);
        val newTop = top + size;
        copy(values, stack.stack + top, size);
        stack.top = newTop;
    }

"""