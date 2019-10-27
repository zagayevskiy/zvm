package testdata.sources.zc.lisp.src

fun includeRuntime() ="""

    struct env_t {

    }

    struct runtime_t {
        var mem_manager: mem_manager_t;
        var global_env: env_t;
    }

    fn create_runtime(max_mem: int): runtime_t {
        val runtime = cast<runtime_t>(alloc(sizeof<runtime_t>));
        runtime.mem_manager = create_mem_manager(max_mem);

        return runtime;
    }

    fn free_runtime(runtime: runtime_t) {
        free_mem_manager(runtime.mem_manager);
        free(runtime);
    }

""".trimIndent()