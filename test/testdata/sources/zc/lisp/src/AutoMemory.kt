package testdata.sources.zc.lisp.src

fun includeAutoMemory() = """

    struct AutoMemory {
        var mem: [void];
        var size: int;
        var unlayouted: int;
        var recycled: Cons;
        var available: Cons;
    }

    struct Cons {
        left: [void];
        right: [void];
        type: byte;
    }

    const CT_LIST: byte = 1;

    fn makeAutoMemory(maxMemorySize: int): AutoMemory {
        val result = cast<AutoMemory>(alloc(sizeof<AutoMemory>));
        result.mem = alloc(maxMemorySize);
        result.size = maxMemorySize;
        result.unlayouted = 0;
        result.recycled = nil;
        result.available = maxMemorySize / sizeof<Cons>;

        return result;
    }

    fn free_autoMemory(autoMemory: AutoMemory) {
        free(autoMemory.mem);
        free(autoMemory);
    }

    fn make_cons(left: [void], right: [void]): Cons {
        val result = alloc_cons();
        result.left = left;
        result.right = right;
        return result;
    }

    fn alloc_cons(autoMemory: AutoMemory): Cons {
        autoMemory.available = autoMemory.available - 1;
        val recycled = autoMemory.recycled
        if(recycled == nil) {
            return layout_next();
        }

        autoMemory.recycled = recycled.right;

        return recycled;
    }

    fn layout_next(autoMemory: AutoMemory): Cons {
        val layouted = autoMemory.unlayouted;
        val nextUnlayouted = layouted + sizeof<Cons>;
        if (nextUnlayouted >= autoMemory.size) {
            crash(123000);
        }

        autoMemory.unlayouted = nextUnlayouted;
        return cast<Cons>(autoMemory.mem + layouted);
    }

    fn recycle(autoMemory: AutoMemory, cons: Cons) {
        cons.left = nil;
        cons.right = autoMemory.recycled;
        cons.type = CT_LIST;
        autoMemory.recycled = cons;
        autoMemory.available = autoMemory.available + 1;
    }

    const MARK_MASK: byte = 128;

    fn mark(cons: Cons) {
        if (cons.type == CT_LIST) {
            mark(cons.left);
            mark(cons.right);
        }
        cons.type = cons.type | MARK_MASK;
    }

    fn sweep(autoMemory: AutoMemory) {
        val mem = autoMemory.mem;
        val unlayoutedAbsolete = cast<int>(mem + autoMemory.unlayouted);
        val unmarkMask = ~MARK_MASK;
        for(var cursor = cast<int>(mem); cursor <= unlayoutedAbsolete; cursor = cursor + 9) {
            val current = cast<Cons>(cursor);
            if (current.type & MARK_MASK) {
                current.type = current.type & unmarkMask;
            } else {
                recycle(current);
            }
        }
    }

    fn gc(autoMemory: AutoMemory) {
        mark(null);
        sweep();
    }
    
    

""".trimIndent()