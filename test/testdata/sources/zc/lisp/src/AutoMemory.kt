package testdata.sources.zc.lisp.src

fun includeAutoMemory() = """

    struct Cons {
        var left: [void];
        var right: [void];
        var flags: byte;
    }

    struct AutoMemory {
        var mem: [void];
        var size: int;
        var unlayouted: int;
        var recycled: Cons;
        var available: int;
    }
    
    const CT_TYPE_MASK: byte = 7;
    const CT_MARK_MASK: byte = 128;
    const CT_UNMARK_MASK: byte = 127;
    const CT_NIL: byte = 0;
    const CT_LIST: byte = 1;
    const CT_INT: byte = 2;
    const CT_ATOM: byte = 3;
    const CT_USER_BIT0_MASK: byte = 16;
    const CT_USER_BIT0_UNMASK: byte = 239;
    const CT_USER_BIT1_MASK: byte = 32;
    const CT_USER_BIT2_MASK: byte = 64;

    fn userBit0(cons: Cons): bool {
        return (cons.flags & CT_USER_BIT0_MASK) == CT_USER_BIT0_MASK;
    }

    fn setUserBit0(cons: Cons, flag: bool) {
        if (flag) {
            cons.flags = cons.flags | CT_USER_BIT0_MASK;
        } else {
            cons.flags = cons.flags & (~CT_USER_BIT0_MASK);
        }
    }

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
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "car list expected");
        return cast<Cons>(cons.left);
    }

    fn cdr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cdr list expected");
        return cons.right;
    }

    fn caar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "caar list expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "caar list in head expected");
        return a.left;
    }

    fn cadr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cadr list expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "cadr list in head expected");
        return a.right;
    }

    fn cdar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cdar list expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cdar list in tail expected");
        return d.left;
    }

    fn cddr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cddr list expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cddr list in tail expected");
        return d.right;
    }

    fn caaar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "caaar list in 1st car expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "caaar list in 2nd car expected");
        val aa: Cons = a.left;
        if (aa == nil) return nil;
        assertType(aa, CT_LIST, "caaar list in 3rd car expected");
        return aa.left;
    }

    fn caadr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "caadr list in 1st car expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "caadr list in 2nd car expected");
        val aa: Cons = a.left;
        if (aa == nil) return nil;
        assertType(aa, CT_LIST, "caadr list in cdr expected");
        return aa.right;
    }

    fn cadar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cadar list in 1st car expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "cadar list in cdr expected");
        val ad: Cons = a.right;
        if (ad == nil) return nil;
        assertType(ad, CT_LIST, "cadar list in 2nd car expected");
        return ad.left;
    }

    fn caddr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "caddr list in car expected");
        val a: Cons = cons.left;
        if (a == nil) return nil;
        assertType(a, CT_LIST, "caddr list in 1st cdr expected");
        val ad: Cons = a.right;
        if (ad == nil) return nil;
        assertType(ad, CT_LIST, "caddr list in 2nd cdr expected");
        return ad.right;
    }

    fn cdaar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cdaar list in cdr expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cdaar list in 1st car expected");
        val da: Cons = d.left;
        if (da == nil) return nil;
        assertType(da, CT_LIST, "cdaar list in 2nd car expected");
        return da.left;
    }

    fn cdadr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cdadr list in 1s cdr expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cdadr list in car expected");
        val da: Cons = d.left;
        if (da == nil) return nil;
        assertType(da, CT_LIST, "cdadr list in 2nd cdr expected");
        return da.right;
    }

    fn cddar(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cddar list in 1st cdr expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cddar list in 2nd cdr expected");
        val dd: Cons = d.right;
        if (dd == nil) return nil;
        assertType(dd, CT_LIST, "cddar list in car expected");
        return dd.left;
    }

    fn cdddr(cons: Cons): Cons {
        if (cons == nil) return nil;
        assertType(cons, CT_LIST, "cdddr list in 1st cdr expected");
        val d: Cons = cons.right;
        if (d == nil) return nil;
        assertType(d, CT_LIST, "cdddr list in 2nd cdr expected");
        val dd: Cons = d.right;
        if (dd == nil) return nil;
        assertType(dd, CT_LIST, "cdddr list in 3rd cdr expected");
        return dd.right;
    }

    fn makeNumber(autoMemory: AutoMemory, value: int): Cons {
        return makeCons(autoMemory, cast<[void]>(value), nil, CT_INT);
    }

    fn makeAtom(mem: AutoMemory, name: [byte]): Cons {
        return makeCons(mem, name, nil, CT_ATOM);
    }

    fn getAtomName(atom: Cons): [byte] {
        return cast<[byte]>(car(atom));
    }

    fn getInt(cons: Cons): int {
        assertType(cons, CT_INT, "int expected");
        return cast<int>(cons.left);
    }
    
    fn getType(cons: Cons): byte {
        if (cons == nil) return CT_NIL;
        return cons.flags & CT_TYPE_MASK;
    }

    fn assertType(cons: Cons, type: byte, message: [byte]) {
        if (getType(cons) != type) {
            print(message);
            crash(2989);
        }
    }

    fn makeCons(autoMemory: AutoMemory, left: [void], right: [void], type: byte): Cons {
        val result = allocCons(autoMemory);
        result.left = left;
        result.right = right;
        result.flags = type;
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
        cons.flags = CT_LIST;
        autoMemory.recycled = cons;
        autoMemory.available = autoMemory.available + 1;
    }

    fn mark(cons: Cons) {
        if (cons != nil) {
            if (getType(cons) == CT_LIST) {
                mark(cons.left);
                mark(cons.right);
            }
            cons.flags = cons.flags | CT_MARK_MASK;
        }
    }

    fn sweep(autoMemory: AutoMemory) {
        val mem = autoMemory.mem;
        val unlayoutedAbsolete = cast<int>(mem + autoMemory.unlayouted);
        for(var cursor = cast<int>(mem); cursor < unlayoutedAbsolete; cursor = cursor + sizeof<Cons>) {
            val current = cast<Cons>(cursor);
            if ((current.flags & CT_MARK_MASK) != 0) {
                current.flags = current.flags & CT_UNMARK_MASK;
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
    
    fn compare(left: Cons, right: Cons): byte {
        val leftType = getType(left);
        val rightType = getType(right);
        if (leftType < rightType) return -1;
        if (leftType > rightType) return 1;
        when(leftType) {
            CT_NIL -> return 0;
            CT_INT -> return compareInts(getInt(left), getInt(right));
            CT_ATOM ->return orderStrings(getAtomName(left), getAtomName(right));
            CT_LIST -> {
                val cmpCar = compare(car(left), car(right));
                if (cmpCar != 0) return cmpCar;
                return compare(cdr(left), cdr(right));
            }
            else -> crash(12345);
        }
    }

    fn compareInts(left: int, right: int): byte {
        if (left == right) {
            return cast<byte>(0);
        } else if (left < right) {
            return cast<byte>(-1);
        } else {
            return cast<byte>(1);
        }
    }
""".trimIndent()