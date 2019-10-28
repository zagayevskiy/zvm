package testdata.sources.zc.lisp.src

fun includeRedBlackTree() = """

    fn putRbTree(mem: AutoMemory, root: Cons, key: Cons, value: Cons): Cons {
        if (root == nil) return makeRbNode(mem, key, value);
    }

    fn makeRbNode(mem: AutoMemory, key: Cons, value: Cons): Cons {
        val nodeValue = makeList(mem, key, value);
        val children = makeList(mem, nil, nil);
        val node = makeList(mem, nodeValue, children);
        return node;
    }

    fn getRbNodeLeft(node: Cons): Cons {
        
    }

""".trimIndent()


/***
 * Node
 * ( ((key . value)) .+r|b ((left . right)) )
 *
 *
 *
 *
 *
 *
 *
 *
 *
* */