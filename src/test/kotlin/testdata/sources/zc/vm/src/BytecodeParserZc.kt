package testdata.sources.zc.vm.src


internal fun includeBytecodeParser() = """
    @include<zvm/context.zc>

    struct ServiceInfo {
        var mainIndex: int;
        var functionsCount: int;
        var globalsCount: int;
        var constantPoolSize: int;
    }

    struct RawFunctionInfo {
        var address: int;
        var argsCount: int;
        var ld0: int;
        var ld1: int;
    }

    fn parseBytecode(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        val serviceInfoSize = sizeof<ServiceInfo>;
        val functionInfoSize = sizeof<RawFunctionInfo>;
        var cursor: [void] = rawBytecode;

        val serviceInfo: ServiceInfo = cursor;
        cursor = cursor + serviceInfoSize;

        val runtimeFunctionSize = sizeof<RuntimeFunction>;
        val functionsTable: [RuntimeFunction] = alloc(4*serviceInfo.functionsCount);
        for(var i = 0; i < serviceInfo.functionsCount; i = i + 1) {
            val rawFunction: RawFunctionInfo = cursor;
            cursor = cursor + functionInfoSize;
            val function: RuntimeFunction = alloc(runtimeFunctionSize);
            functionsTable[i] = function;
            function.address = rawFunction.address;
            function.argsMemorySize = computeArgsMemorySize(rawFunction);
        }
        val constantPool: [byte] = cursor;
        cursor = cursor + serviceInfo.constantPoolSize;

        val bytecodeStart: [byte] = cursor;
        val bytecodeSize = rawBytecodeSize - (cast<int>(bytecodeStart) - cast<int>(rawBytecode));

        val result: ProgramInfo = alloc(sizeof<ProgramInfo>);
        result.mainIndex = serviceInfo.mainIndex;
        result.functionsTable = functionsTable;
        result.functionsTableSize = serviceInfo.functionsCount;
        result.constantPool = constantPool;
        result.constantPoolSize = serviceInfo.constantPoolSize;
        result.bytecode = bytecodeStart;
        result.bytecodeSize = bytecodeSize;

        return result;
     }

    fn computeArgsMemorySize(raw: RawFunctionInfo): int {
        var size = 0;
        val count = cast<byte>(raw.argsCount);
        val description: [byte] = cast<[void]>(raw) + sizeof<int>*2;
        val mask: byte = (3);
        for (var i: byte = 0; i < count; i = i + cast<byte>(1)) {
            val index = 7 - i / 4;
            val offset = (i % 4)*2;
            val descriptionByte: byte = description[index];
            val type: byte = cast<byte>(descriptionByte >> offset) & mask;
            if (type == 1 ) {
                size = size + 1;
            } else if (type == 2) {
                size = size + 4;
            } else {
                crash(type);
            }
        }

        return size;
    }

"""