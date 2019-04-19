package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.zc.includes.includeStack
import com.zagayevskiy.zvm.zc.includes.includeStdMem

internal val vmOverZc = """
    ${includeStdMem()}
    ${includeStack()}
    ${includeBytecodeParser()}

    struct VmContext {
        var operandsStack: Stack;
        var frames: Stack;
    }

    fn main(rawBytecode: [byte], rawBytecodeSize: int, args: [void], argsCount: int): int {
        val programInfo = parseBytecode(rawBytecode, rawBytecodeSize);
        val operandsStackSize = 1024;
        val operandsStack: [void] = alloc(operandsStackSize);

        val bytecode = programInfo.bytecode;
        val ip = 0;



        return 0;
    }

    fn createContext(): VmContext {
        val result: VmContext = alloc(sizeof<VmContext>);

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