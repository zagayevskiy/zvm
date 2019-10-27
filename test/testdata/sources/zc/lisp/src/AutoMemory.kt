package testdata.sources.zc.lisp.src

fun includeAutoMemory() = """

    struct Cons {
        var left: [void];
        var right: [void];
        var type: byte;
    }

    struct AutoMemory {
        var mem: [void];
        var size: int;
        var unlayouted: int;
        var recycled: Cons;
        var available: int;
    }
    
    const CT_LIST: byte = 1;
    const CT_INT: byte = 2;

    fn makeAutoMemory(maxMemorySize: int): AutoMemory {
        val result = cast<AutoMemory>(alloc(sizeof<AutoMemory>));
        result.mem = alloc(maxMemorySize);
        result.size = maxMemorySize;
        result.unlayouted = 0;
        result.recycled = nil;
        result.available = maxMemorySize / sizeof<Cons>;

        return result;
    }

    fn freeAutoMemory(autoMemory: AutoMemory) {
        free(autoMemory.mem);
        free(autoMemory);
    }

    fn cons(autoMemory: AutoMemory, left: Cons, right: Cons): Cons {
        return makeCons(autoMemory, left, right, CT_LIST);
    }

    fn car(cons: Cons): Cons {
        if (cons == nil) return cast<Cons>(nil);
        assertType(cons, CT_LIST, "list expected");
        return cast<Cons>(cons.left);
    }

    fn cdr(cons: Cons): Cons {
        if(cons == nil) return cast<Cons>(nil);
        assertType(cons, CT_LIST, "list expected");
        return cast<Cons>(cons.right);
    }

    fn makeNumber(autoMemory: AutoMemory, value: int): Cons {
        return makeCons(autoMemory, cast<[void]>(value), nil, CT_INT);
    }

    fn getInt(cons: Cons): int {
        assertType(cons, CT_INT, "int expected");
        return cast<int>(cons.left);
    }

    fn assertType(cons: Cons, type: byte, message: [byte]) {
        if (cons.type != type) {
            print(message);
            crash(2989);
        }
    }

    fn makeCons(autoMemory: AutoMemory, left: [void], right: [void], type: byte): Cons {
        val result = allocCons(autoMemory);
        result.left = left;
        result.right = right;
        result.type = type;
        return result;
    }

    fn allocCons(autoMemory: AutoMemory): Cons {
        if(autoMemory.available <= 0) {
            crashWithMessage(autoMemory.available, "OOM");
        }
        autoMemory.available = autoMemory.available - 1;
        val recycled = autoMemory.recycled;
        if(recycled == nil) {
            return layoutNext(autoMemory);
        }

        autoMemory.recycled = recycled.right;

        return recycled;
    }

    fn layoutNext(autoMemory: AutoMemory): Cons {
        val layouted = autoMemory.unlayouted;
        val nextUnlayouted = layouted + sizeof<Cons>;
        if (nextUnlayouted > autoMemory.size) {
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
    const UNMARK_MASK: byte = 127;

    fn mark(cons: Cons) {
        if (cons != nil) {
            if (cons.type == CT_LIST) {
                mark(cons.left);
                mark(cons.right);
            }
            cons.type = cons.type | MARK_MASK;
        }
    }

    fn sweep(autoMemory: AutoMemory) {
        val mem = autoMemory.mem;
        val unlayoutedAbsolete = cast<int>(mem + autoMemory.unlayouted);
        for(var cursor = cast<int>(mem); cursor < unlayoutedAbsolete; cursor = cursor + sizeof<Cons>) {
            val current = cast<Cons>(cursor);
            if ((current.type & MARK_MASK) != 0) {
                current.type = current.type & UNMARK_MASK;
            } else {
                recycle(autoMemory, current);
            }
        }
    }

    fn gc(autoMemory: AutoMemory, root: Cons) {
        mark(root);
        sweep(autoMemory);
    }

    fn crashWithMessage(code: int, message: [byte]) {
        print(message);
        crash(code);
    }
    

""".trimIndent()