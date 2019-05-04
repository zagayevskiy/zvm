package testsrc.zc.vmoverzc

internal fun includeContext() = """
    struct Context {
        var bytecode: [byte];
        var bytecodeSize: int;
        var operandsStack: Stack;
        var callStack: Stack;
        var functions: [FunctionInfo];
        var globals: [void];
        var ip: int;
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

""".trimIndent()