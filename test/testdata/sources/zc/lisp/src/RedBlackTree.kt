package testdata.sources.zc.lisp.src

fun includeRedBlackTree() = """

    struct RbTree {
        var root: Cons;
    }

    fn makeRbTree(): RbTree {
        val tree: RbTree = alloc(sizeof<RbTree>);
        tree.root = nil;
        return tree;
    }

    fn putRbTree(mem: AutoMemory, tree: RbTree, key: Cons, value: Cons, compare: (Cons, Cons) -> byte) {
        var cursor = tree.root;
        if (cursor == nil) {
            tree.root = makeRbNode(mem, key, value, nil);
        } else {
            var parent: Cons = nil;
            while (true) {
                val curKey = nodeKey(cursor);
                val compared = compare(key, curKey);
                if (compared == 0) {
                    setNodeValue(cursor, value);
                    return;
                }
                parent = cursor;
                if (compared < 0) {
                    cursor = leftChild(cursor);
                    if (cursor == nil) {
                        setNodeLeft(parent, makeRbNode(mem, key, value, parent));
                        return;
                    }
                } else {
                    cursor = rightChild(cursor);
                    if (cursor == nil) {
                        setNodeRight(parent, makeRbNode(mem, key, value, parent));
                        return;
                    }
                }
            }
        }
    }

    fn findTree(root: Cons, key: Cons, compare: (Cons, Cons) -> byte): Cons {
        var cursor = root;
        while (cursor != nil) {
            val compared = compare(key, nodeKey(cursor));
            if (compared == 0) {
                return nodeValue(cursor);
            }
            if (compared < 0) {
                cursor = leftChild(cursor);
            } else {
                cursor = rightChild(cursor);
            }
        }

        return nil;
    }

    fn makeRbNode(mem: AutoMemory, key: Cons, value: Cons, parent: Cons): Cons {
        val keyValue = cons(mem, key, value);
        val leftRight = cons(mem, nil, nil);
        val parentLeftRight = cons(mem, parent, leftRight);
        val node = cons(mem, keyValue, parentLeftRight);
        return node;
    }

    fn setNodeValue(node: Cons, value: Cons) {
        val keyValue = car(node);
        keyValue.right = value;
    }

    fn setNodeLeft(node: Cons, leftNode: Cons) {
        nodeChildren(node).left = leftNode;
    }

    fn setNodeRight(node: Cons, rightNode: Cons) {
        nodeChildren(node).right = rightNode;
    }

    fn nodeKey(node: Cons): Cons {
        return caar(node);
    }

    fn nodeValue(node: Cons): Cons {
        return cadr(node);
    }

    fn nodeParent(node: Cons): Cons {
        return cdar(node);
    }

    fn leftChild(node: Cons): Cons {
        return cddar(node);
    }

    fn nodeChildren(node: Cons): Cons {
        return cddr(node);
    }

    fn rightChild(node: Cons): Cons {
        return cdddr(node);
    }

""".trimIndent()


/***
 *
 *  Node
 *  ( (key . value) . (parent . (left . right)) )
 *  key == caar
 *  value == cadr
 *  parent == cdar
 *  left == cddar
 *  right == cdddr
 *
* */