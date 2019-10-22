package testdata.sources.zc.vm.src

internal fun includeContext() = """
    struct Context {
        var bytecode: [byte];
        var bytecodeSize: int;
        var operandsStack: Stack;
        var callStack: Stack;
        var functions: [RuntimeFunction];
        var functionsCount: int;
        var globals: [void];
        var ip: int;
        var localsStackBaseAddress: [void];
        var sp: [void];
    }

     fn createContext(info: ProgramInfo): Context {
        val result: Context = alloc(sizeof<Context>);
        result.bytecode = info.bytecode;
        result.bytecodeSize = info.bytecodeSize;
        result.operandsStack = createStack(65536);
        result.callStack = createStack(65536);
        result.functions = info.functionsTable;
        result.functionsCount = info.functionsTableSize;
        result.ip = 0;
        result.localsStackBaseAddress = alloc(65536);
        result.sp = result.localsStackBaseAddress;

        return result;
    }

""".trimIndent()