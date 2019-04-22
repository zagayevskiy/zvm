package com.zagayevskiy.zvm.zc.vmoverzc.src

import com.zagayevskiy.zvm.zc.includes.includeStack
import com.zagayevskiy.zvm.zc.includes.includeStdMem



internal val vmOverZc = """
    ${includeStdMem()}
    ${includeStack()}
    ${includeBytecodeParser()}
    ${includeContext()}

    struct StackFrame {
        var args: [void];
        var locals: [void];
        var returnAddress: int;
    }

    fn main(rawBytecode: [byte], rawBytecodeSize: int): int {
        val programInfo = parseBytecode(rawBytecode, rawBytecodeSize);
        val context = createContext(programInfo);

        call(context, programInfo.serviceInfo.mainIndex);
        loop(context);

        return popInt(context.operandsStack);
    }

    fn loop(context: Context): int {
        val bytecodeSize = context.bytecodeSize;
        while(context.ip < bytecodeSize) {
            val code = nextByte(context);
            when(code) {
                1 -> call(context, nextInt(context));
                2 -> {
                    if(context.callStack.size == 1) return popInt(context.operandsStack);
                }
            }
        }
        return 0;
    }

    fn nextInt(context: Context): int {
        val bytecode = context.bytecode;
        val ip = context.ip;
        asm{"
            consti 1234567
            pop
        "}
        val result = cast<[int]>(bytecode + ip)[0];
        context.ip = ip + 4;
        return result;
    }

    fn nextByte(context: Context): byte {
        val value = context.bytecode[context.ip];
        context.ip = context.ip + 1;
        return value;
    }

    fn call(context: Context, functionIndex: int): int {
        val function = context.functions[functionIndex];
        pushStackFrame(context.callStack, createStackFrame(context.ip));
        context.ip = function.address;

        return 0;
    }



    fn createStackFrame(returnAddress: int): StackFrame {
        val frame: StackFrame = alloc(sizeof<StackFrame>);
        frame.returnAddress = returnAddress;
        return frame;
    }

    fn deleteStackFrame(frame: StackFrame): int {
        return free(frame);
    }

    fn pushStackFrame(stack: Stack, frame: StackFrame): int {
        return pushInt(stack, cast<int>(frame));
    }

    fn popStackFrame(stack: Stack): StackFrame {
        return cast<StackFrame>(popInt(stack));
    }

"""


