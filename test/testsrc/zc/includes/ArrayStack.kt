package testsrc.zc.includes

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

    fn pushInt(stack: Stack, value: int): int {
        if (stack.top >= stack.size) crash(101);
        cast<[int]>(stack.stack)[stack.top] = value;
        stack.top = stack.top + 1;
        return 0;
    }

    fn popInt(stack: Stack): int {
        if (stack.top == 0) crash(102);
        val newTop = stack.top - 1;
        stack.top = newTop;
        return cast<[int]>(stack.stack)[newTop];
    }

    fn peekInt(stack: Stack): int {
        if (stack.top == 0) crash(103);
        return cast<[int]>(stack.stack)[stack.top - 1];
    }

    fn pushByte(stack: Stack, value: byte): int {
        return pushInt(stack, cast<int>(value));
    }

    fn popByte(stack: Stack): byte {
        return cast<byte>(popInt(stack));
    }

"""