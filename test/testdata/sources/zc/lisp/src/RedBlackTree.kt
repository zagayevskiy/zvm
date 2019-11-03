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
            setNodeBlack(tree.root);
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
                        val insertedNode = makeRbNode(mem, key, value, parent);
                        setNodeLeft(parent, insertedNode);
                        fixAfterInsert(tree, insertedNode);
                        return;
                    }
                } else {
                    cursor = rightChild(cursor);
                    if (cursor == nil) {
                        val insertedNode = makeRbNode(mem, key, value, parent);
                        setNodeRight(parent, insertedNode);
                        fixAfterInsert(tree, insertedNode);
                        return;
                    }
                }
            }
        }
    }

    fn fixAfterInsert(tree: RbTree, insertedNode: Cons) {
        setNodeRed(insertedNode);
        var x = insertedNode;

        while(fixAfterInsertLoopCondition(x)) {
            var xParent = nodeParent(x);
            var xGrandParent = nodeParent(xParent);
            if (xParent == leftChild(xGrandParent)) {
                val y = rightChild(xGrandParent);
                if (isNodeRed(y)) {
                    setNodeBlack(xParent);
                    setNodeBlack(y);
                    setNodeRed(xGrandParent);
                    x = xGrandParent;
                } else {
                    if (x == rightChild(xParent)) {
                        x = xParent;
                        rotateLeft(tree, x);
                        xParent = nodeParent(x);
                        xGrandParent = nodeParent(xParent);
                    }
                    setNodeBlack(xParent);
                    setNodeRed(xGrandParent);
                    rotateRight(tree, xGrandParent);
                }
            } else {
                val y = leftChild(xGrandParent);
                if (isNodeRed(y)) {
                    setNodeBlack(xParent);
                    setNodeBlack(y);
                    setNodeRed(xGrandParent);
                    x = xGrandParent;
                } else {
                    if (x == leftChild(xParent)) {
                        x = xParent;
                        rotateRight(tree, x);
                        xParent = nodeParent(x);
                        xGrandParent = nodeParent(xParent);
                    }
                    setNodeBlack(xParent);
                    setNodeRed(xGrandParent);
                    rotateLeft(tree, xGrandParent);
                }
            }
        }

        setNodeBlack(tree.root);
    }

    fn fixAfterInsertLoopCondition(x: Cons): bool {
        val xParent = nodeParent(x);
        if (xParent == nil) return false;
        return isNodeRed(xParent);
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
        val parentAndChildren = cons(mem, parent, leftRight);
        val node = cons(mem, keyValue, parentAndChildren);
        setNodeRed(node);
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
    
    fn setNodeParent(node: Cons, parent: Cons) {
        cdr(node).left = parent;
    }

    fn nodeKey(node: Cons): Cons {
        return caar(node);
    }

    fn nodeValue(node: Cons): Cons {
        return cadr(node);
    }

    fn nodeKeyValue(node: Cons): Cons {
        return car(node);
    }

    fn nodeParent(node: Cons): Cons {
        if (node == nil) return nil;
        return cdar(node);
    }

    fn nodeGrandparent(node: Cons): Cons {
        if (node == nil) return nil;
        val parent = cdar(node);
        if (parent == nil) return nil;
        return cdar(parent);
    }

    fn nodeUncle(node: Cons): Cons {
        val grandparent = nodeGrandparent(node);
        if (grandparent == nil) return nil;
        val children = nodeChildren(grandparent);
        if (children.left == node) {
            return children.right;
        } else {
            return children.left;
        }
    }

    fn setNodeRed(node: Cons) {
        if (node != nil) {
            setUserBit0(node, false);
        }
    }

    fn setNodeBlack(node: Cons) {
        if(node != nil) {
            setUserBit0(node, true);
        }
    }

    fn isNodeRed(node: Cons): bool {
        if (node == nil) return false;
        return !userBit0(node);
    }

    fn isNodeBlack(node: Cons): bool {
        if (node == nil) return true;
        return userBit0(node);
    }

    fn nodeChildren(node: Cons): Cons {
        return cddr(node);
    }

    fn leftChild(node: Cons): Cons {
        if (node == nil) return nil;
        return cddar(node);
    }

    fn rightChild(node: Cons): Cons {
        if (node == nil) return nil;
        return cdddr(node);
    }

    fn rotateLeft(tree: RbTree, p: Cons) {
        if (p == nil) return;
        val r = rightChild(p);
        val rLeft = leftChild(r);
        setNodeRight(p, rLeft);
        if (rLeft != nil) {
            setNodeParent(rLeft, p);
        }
        val pParent = nodeParent(p);
        setNodeParent(r, pParent);
        if (pParent == nil) {
            tree.root = r;
        } else if (leftChild(pParent) == p) {
            setNodeLeft(pParent, r);
        } else {
            setNodeRight(pParent, r);
        }
        setNodeLeft(r, p);
        setNodeParent(p, r);
    }

    fn rotateRight(tree: RbTree, p: Cons) {
        if (p == nil) return;
        val l = leftChild(p);
        val lRight = rightChild(l);
        setNodeLeft(p, lRight);
        if (lRight != nil) {
            setNodeParent(lRight, p);
        }
        val pParent = nodeParent(p);
        setNodeParent(l, pParent);
        if (pParent == nil) {
            tree.root = l;
        } else if (rightChild(pParent) == p) {
            setNodeRight(pParent, l);
        } else {
            setNodeLeft(pParent, l);
        }
        setNodeRight(l, p);
        setNodeParent(p, l);

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