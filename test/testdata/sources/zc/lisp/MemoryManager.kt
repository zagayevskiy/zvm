package testdata.sources.zc.lisp

fun includeMemoryManager() = """

    struct mem_manager_t {
        var mem: [void];
        var mem_size: int;
        var unlayouted: int;
        var recycled: cons_t;
        var available: cons_t;
    }

    struct cons_t {
        left: [void];
        right: [void];
        type: byte;
    }

    
    fn create_mem_manager(max_mem: int): mem_manager_t {
        val result = cast<mem_manager_t>(alloc(sizeof<mem_manager_t>));
        result.mem = alloc(max_mem);
        result.mem_size = max_mem;
        result.unlayouted = 0;
        result.recycled = nil;
        result.available = max_mem / sizeof<cons_t>;

        return result;
    }

    fn free_mem_manager(mem_manager: mem_manager_t) {
        free(mem_manager.mem);
        free(mem_manager);
    }

    fn make_cons(left: [void], right: [void]): cons_t {
        val result = alloc_cons();
        result.left = left;
        result.right = right;
        return result;
    }

    fn alloc_cons(mem_manager: mem_manager_t): cons_t {
        mem_manager.available = mem_manager.available - 1;
        val recycled = mem_manager.recycled
        if(recycled == nil) {
            return layout_next();
        }

        mem_manager.recycled = recycled.right;

        return recycled;
    }

    fn layout_next(mem_manager: mem_manager_t): cons_t {
        val layouted = mem_manager.unlayouted;
        val next_unlayouted = layouted + sizeof<cons_t>;
        if (next_unlayouted >= mem_manager.mem_size) {
            crash(123000);
        }

        mem_manager.unlayouted = next_unlayouted;
        return cast<cons_t>(mem_manager.mem + layouted);
    }

    fn recycle(mem_manager: mem_manager_t, cons: cons_t) {
        cons.left = nil;
        cons.right = mem_manager.recycled;
        cons.type = CT_LIST;
        mem_manager.recycled = cons;
        mem_manager.available = mem_manager.available + 1;
    }

    const MARK_MASK: byte = 128;
    const UNMARK_MASK: byte = ~MARK_MASK;

    fn mark(cons: cons_t) {
        if (cons.type == CT_LIST) {
            mark(cons.left);
            mark(cons.right);
        }
        cons.type = cons.type | MARK_MASK;
    }

    fn sweep(mem_manager: mem_manager_t) {
        val mem = mem_manager.mem;
        val unlayouted_absolete = cast<int>(mem + mem_manager.unlayouted);
        for(var cursor = cast<int>(mem); cursor <= unlayouted_absolete; cursor = cursor + 9) {
            val current = cast<cons_t>(cursor);
            if (current.type & MARK_MASK) {
                current.type = current.type & UNMARK_MASK;
            } else {
                recycle(current);
            }
        }
    }

    fn gc(mem_manager: mem_manager_t) {
        mark_all(...);
        sweep();
    }
    
    

""".trimIndent()