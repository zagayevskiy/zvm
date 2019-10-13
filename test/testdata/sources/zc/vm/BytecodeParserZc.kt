package testdata.sources.zc.vm


internal fun includeBytecodeParser() = """
    struct ServiceInfo {
        var mainIndex: int;
        var functionsCount: int;
        var globalsCount: int;
    }

    struct RawFunctionInfo {
        var address: int;
        var argsCount: int;
        var ld0: int;
        var ld1: int;
    }

    struct RuntimeFunction {
        var address: int;
        var argsMemorySize: int;
    }

    struct ProgramInfo {
        var mainIndex: int;
        var functionsTable: [FunctionInfo];
        var functionsTableSize: int;
        var bytecode: [byte];
        var bytecodeSize: int;
    }

    fn parseBytecode(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        val serviceInfoSize = sizeof<ServiceInfo>;
        val functionInfoSize = sizeof<RawFunctionInfo>;
        var cursor: [void] = rawBytecode;

        val serviceInfo: ServiceInfo = cursor;
        cursor = cursor + serviceInfoSize;

        val bytecodeSize = rawBytecodeSize - (serviceInfoSize + functionInfoSize*serviceInfo.functionsCount);

        val runtimeFunctionSize = sizeof<RuntimeFunction>
        val functionsTable: [RuntimeFunction] = alloc(4*serviceInfo.functionsCount);
        for(var i = 0; i < serviceInfo.functionsCount; i = i + 1) {
            val rawFunction: RawFunctionInfo = cursor;
            cursor = cursor + functionInfoSize;
            val function = alloc(runtimeFunctionSize);
            functionsTable[i] = function;
            function.address = rawFunction.address;
            function.argsMemorySize = computeArgsMemorySize(rawFunction);
        }

        val result: ProgramInfo = alloc(sizeof<ProgramInfo>);
        result.mainIndex = serviceInfo.mainIndex;
        result.functionsTable = functionsTable;
        result.functionsTableSize = serviceInfo.functionsCount;
        result.bytecode = rawBytecode + (rawBytecodeSize - bytecodeSize);
        result.bytecodeSize = bytecodeSize;

        return result;
     }

    fn computeArgsMemorySize(raw: RawFunctionInfo): int {
        var size = 0;
        val count: byte = raw.argsCount;
        val description: [byte] = cast<[void]>(raw) + sizeof<int>*2;
        for (var i: byte = 0; i < count; i = i + 1) {
            val index = i / 4;
            val offset = i % 4;
            val b = description[index];
            val s = b >> offset;
            if (s == 1 ) {
                size = size + 1;
            } else {
                size = size + 4;
            }
        }

        return size;
    }

"""