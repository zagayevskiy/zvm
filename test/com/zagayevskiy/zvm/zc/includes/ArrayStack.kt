package com.zagayevskiy.zvm.zc.includes

internal fun includeStack() = """
    struct Stack {
        var stack: [void];
        var intMirror: [int];
        var byteMirror: [byte];
        var size: int;
        var top: int;
    }

    fn createStack(size: int): Stack {
        val result: Stack = alloc(sizeof<Stack>);
        result.stack = alloc(size * sizeof<int>);
        result.intMirror = result.stack;
        result.byteMirror = result.stack;
        result.size = size;
        result.top = 0;
        return result;
    }

    fn pushInt(stack: Stack, value: int): int {
        if (stack.top >= stack.size) return 1;
        (cast<[int]>(stack.stack))[stack.top] = value;
        stack.top = stack.top + 1;
        return 0;
    }

    fn popInt(stack: Stack): int {
        if (stack.top == 0) return 1;
        val newTop = stack.top - 1;
        stack.top = newTop;
        return (cast<[int]>(stack.stack))[newTop];
    }

    fn pushByte(stack: Stack, value: byte): int {
        if (stack.top >= stack.size) return 1;
        stack.byteMirror[stack.top*4] = value;
        stack.top = stack.top + 1;
        return 0;
    }

    fn popByte(stack: Stack): byte {
        if (stack.top == 0) return 1;
        val newTop = stack.top - 1;
        stack.top = newTop;
        return stack.byteMirror[newTop*4];
    }

"""