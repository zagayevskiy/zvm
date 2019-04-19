package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.zc.includes.includeStack
import com.zagayevskiy.zvm.zc.includes.includeStdMem

internal val vmOverZc = """
    ${includeStdMem()}
    ${includeStack()}
    ${includeBytecodeParser()}

    struct Context {
        var bytecode: [byte];
        var bytecodeSize: int;
        var operandsStack: Stack;
        var callStack: Stack;
        var functions: [FunctionInfo];
        var globals: [void];
        var ip: int;
    }

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
        }
        return 0;
    }

    fn nextByte(context: Context): byte {
        val value = context.bytecode[context.ip];
        context.ip = context.ip + 1;
        return value;
    }

    fn call(context: Context, functionIndex: int) {
        val function = context.functions[functionIndex];
        pushStackFrame(context.callStack, createStackFrame(context.ip));
        context.ip = function.address;
    }

    fn createContext(info: ProgramInfo): Context {
        val result: Context = alloc(sizeof<Context>);
        result.bytecode = info.bytecode;
        result.bytecodeSize = info.bytecodeSize;
        result.operandsStack = createStack(2048);
        result.callStack = createStack(1024);
        result.functions = info.functionsTable;
        result.ip = 0;

        return result;
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



internal fun includeBytecodeParser() = """
    struct ServiceInfo {
        var mainIndex: int;
        var functionsCount: int;
        var globalsCount: int;
    }

    struct FunctionInfo {
        var address: int;
        var argsCount: int;
        var localsCount: int;
    }

    struct ProgramInfo {
        var serviceInfo: ServiceInfo;
        var functionsTable: [FunctionInfo];
        var bytecode: [byte];
        var bytecodeSize: int;
    }

    fn parseBytecode(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        val serviceInfoSize = sizeof<ServiceInfo>;
        val functionInfoSize = sizeof<FunctionInfo>;
        var cursor: [void] = rawBytecode;

        val serviceInfo: ServiceInfo = cursor;
        cursor = cursor + serviceInfoSize;

        val bytecodeSize = rawBytecodeSize - (serviceInfoSize + functionInfoSize*serviceInfo.functionsCount);

        val functionsTable: [FunctionInfo] = alloc(4*serviceInfo.functionsCount);
        for(var i = 0; i < serviceInfo.functionsCount; i = i + 1) {
            functionsTable[i] = cursor;
            cursor = cursor + functionInfoSize;
        }

        val result: ProgramInfo = alloc(sizeof<ProgramInfo>);
        result.serviceInfo = serviceInfo;
        result.functionsTable = functionsTable;
        result.bytecode = rawBytecode + (rawBytecodeSize - bytecodeSize);
        result.bytecodeSize = bytecodeSize;

        return result;
     }
"""