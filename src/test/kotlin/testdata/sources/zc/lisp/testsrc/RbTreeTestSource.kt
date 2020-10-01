package testdata.sources.zc.lisp.testsrc

import testdata.cases.TestSource
import testdata.sources.zc.lisp.src.includeAutoMemory
import testdata.sources.zc.lisp.src.includeRedBlackTree

object RbTreeTestSource {

    val MakeNode = TestSource("make rb node", """
        fn main(): int {
            testMakeNode();

            return 0;
        }

        fn testMakeNode() {
            val mem = makeAutoMemory(100*sizeof<Cons>);
            val k = makeNumber(mem, 1);
            val v = makeNumber(mem, 2);
            val parent = cons(mem, nil, nil);
            val node = makeRbNode(mem, k, v, parent);

            assertRefEq(nil, leftChild(node), "expect left nil");
            assertRefEq(nil, rightChild(node), "expect right nil");
            assertRefEq(parent, nodeParent(node), "expect parent ref eq");
            assertRefEq(k, nodeKey(node), "expect key ref eq");
            assertRefEq(v, nodeValue(node), "expect value ref eq");

            val fakeLeft = cons(mem, nil, nil);
            val fakeRight = cons(mem, nil, nil);

            setNodeLeft(node, fakeLeft);
            setNodeRight(node, fakeRight);

            assertRefEq(fakeLeft, leftChild(node), "expect left set");
            assertRefEq(fakeRight, rightChild(node), "expect right set");

            freeAutoMemory(mem);
        }

        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>

    """.trimIndent())

    val AllPuttedKeysExistsAndValuesCorrect = TestSource("putted exists", """
        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>

        ${isBinarySearchTree()}

        fn main(): int {
            val mem = makeAutoMemory(301*sizeof<Cons>);
            val tree = makeRbTree(mem);

            val count = 50;
            val exists: [bool] = alloc(count*sizeof<bool>);
            for (var i = 0; i < count; i = i + 1) {
                exists[i] = false;
                putRbTree(mem, tree, makeNumber(mem, i), makeNumber(mem, i*100), ::compare);
            }
            check(tree.root, exists, count);
            for (var i = 0; i < count; i = i + 1) {
                if(!exists[i]) crashm(i, "key not exists");
            }

            checkIsBst(tree, ::compare);
            
            free(exists);
            freeAutoMemory(mem);
            return 0;
        }

        fn check(root: Cons, exists: [bool], count: int) {
            if (root == nil) return;
            val key = getInt(nodeKey(root));
            if (key < 0 || key >= count) {
                crashm(key, "unexpected key");
            }
            if (exists[key]) crashm(key, "key exists twice");
            val value = getInt(nodeValue(root));
            assertIntEq(key*100, value, "value must be key*100");
            exists[key] = true;
            check(leftChild(root), exists, count);
            check(rightChild(root), exists, count);
        }

    """.trimIndent())

    val PutGet = TestSource("put/get ints", """

        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>

        fn main(): int {
            testPutGet();
            return 0;
        }

        ${isBinarySearchTree()}
        ${makeCongruentTree()}

        fn testPutGet() {
            val mem = makeAutoMemory(27000);

            val count = 30;
            val tree = makeCongruentTree(mem, count);

            var keyGen = 1;
            for (var i = 0 ; i < count; i = i + 1) {
                keyGen = (keyGen * 8121 + 28411) % 134456;
                val k = makeNumber(mem, keyGen);
                val v = makeNumber(mem, i);
                val minusK = makeNumber(mem, -1*keyGen);
                val minusV = makeNumber(mem, i*keyGen);

                val actual = findTree(tree.root, k, ::compare);
                val minusActual = findTree(tree.root, minusK, ::compare);

                assertByteEq(0, compare(v, actual), "expect values eq 1");
                assertByteEq(0, compare(minusV, minusActual), "expect values eq 2");
            }

            freeAutoMemory(mem);
        }

    """.trimIndent())

    val RedBlackRequirements = TestSource("check requirements", """
        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>

        ${makeCongruentTree()}

        fn main(): int {
            val mem = makeAutoMemory(27000);

            val tree = makeCongruentTree(mem, 30);

            checkRequirements(tree);

            freeAutoMemory(mem);
            return 0;
        }
    """.trimIndent())

    val HeterogeneousData = TestSource("Heterogeneous", """

        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>


        fn main(): int {

            var buffer: [byte];
            val mem = makeAutoMemory(6000*sizeof<Cons>);
            val tree = makeRbTree(mem);
            val iFrom = -1000000;
            val iTo = 1000000;
            val iStep = 10000;
            for (var i = iFrom; i <= iTo; i = i + iStep) {
                buffer = alloc(16);
                val p = makeNumber(mem, i);
                itos(i, buffer);
                val s = makeAtom(mem, buffer);
                val c_ps = cons(mem, p, s);
                val c_s_c_ps = cons(mem, s, c_ps);

                putRbTree(mem, tree, p, s, ::compare);
                putRbTree(mem, tree, s, p, ::compare);
                putRbTree(mem, tree, c_ps, c_s_c_ps, ::compare);
                putRbTree(mem, tree, c_s_c_ps, s, ::compare);
                putRbTree(mem, tree, c_s_c_ps, c_ps, ::compare);
            }

            for (var i = iFrom; i <= iTo; i = i + iStep) {
                buffer = alloc(16);
                val p = makeNumber(mem, i);
                itos(i, buffer);
                val s = makeAtom(mem, buffer);
                val c_ps = cons(mem, p, s);
                val c_s_c_ps = cons(mem, s, c_ps);

                assertByteEq(0, compare(s, findTree(tree.root, p, ::compare)), "find by int");
                assertIntEq(i, getInt(findTree(tree.root, s, ::compare)), "find by atom");
                assertByteEq(0, compare(c_s_c_ps, findTree(tree.root, c_ps, ::compare)), "find by complex cons 1");
                assertByteEq(0, compare(c_ps, findTree(tree.root, c_s_c_ps, ::compare)), "find by complex cons 2");
            }

            freeAutoMemory(mem);
            free(buffer);
            return  0;
        }

    """.trimIndent())

    val IsBstCrashedOnNoBst = TestSource("crash on no-bst", """

        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>
        ${isBinarySearchTree()}
        fn main(): int {
            val mem = makeAutoMemory(27000);
            val tree = makeRbTree(mem);

            val one = makeNumber(mem, 1);
            val two = makeNumber(mem, 2);
            val three = makeNumber(mem, 3);
            val four = makeNumber(mem, 4);
            val five = makeNumber(mem, 5);

            val twoNode = makeRbNode(mem, two, two, nil);
            val threeNode = makeRbNode(mem, three, three, twoNode);
            val oneNode = makeRbNode(mem, one, one, threeNode);
            setNodeRight(twoNode, threeNode);
            setNodeLeft(threeNode, oneNode);

            tree.root = twoNode;

            checkIsBst(tree, ::compare);

            return 0;
        }

    """.trimIndent())

    val TreeEqualityChecks = TestSource("tree equality checks", """

        @include<std/io.zc>
        @include<std/mem.zc>
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        @include<std/assert.zc>

        ${makeCongruentTree()}
        ${isTreesEquals()}
        ${copyTree()}
        ${checkParentLinks()}

        fn main(): int {
            testTreeEqualsItself();
            testTreeEqualsToItsCopy();
            testTreeCopyIndependent();
            return 0;
        }

        fn testTreeEqualsItself() {
            val mem = makeAutoMemory(27000);
            val tree = makeCongruentTree(mem, 32);

            assertTrue(isTreesEquals(tree.root, tree.root, ::compare), "tree must be equals to itself");

            freeAutoMemory(mem);
        }

        fn testTreeEqualsToItsCopy() {
            val mem = makeAutoMemory(27000);
            val tree = makeCongruentTree(mem, 32);
            val copy = copyTree(mem, tree);
            checkParentLinks(copy.root, nil);

            assertTrue(isTreesEquals(tree.root, copy.root, ::compare), "tree must be equals to it copy");

            freeAutoMemory(mem);
        }

        fn testTreeCopyIndependent() {
            val mem = makeAutoMemory(27000);
            val tree = makeCongruentTree(mem, 32);
            val copy = copyTree(mem, tree);
            val insertedKey = makeNumber(mem, 100500);
            val inserted = makeNumber(mem, 300400);
            putRbTree(mem, copy, insertedKey, inserted, ::compare);

            assertRefEq(inserted, findTree(copy.root, insertedKey, ::compare), "100500 must be inserted to copy");
            assertRefEq(nil, findTree(tree.root, insertedKey, ::compare), "100500 must NOT be inserted to tree");
            assertTrue(!isTreesEquals(tree.root, copy.root, ::compare), "tree and it's copy must be independent");

            putRbTree(mem,tree, insertedKey, inserted, ::compare);
            assertTrue(isTreesEquals(tree.root, copy.root, ::compare), "tree must be deterministic");

            freeAutoMemory(mem);
        }

    """.trimIndent())

    private fun rbTreeRequirements() = """
        fn checkRequirements(tree: RbTree) {
            checkRootBlack(tree);
            checkChildrenOfRedIsBlack(tree.root, false);
            checkAllPathsContainsSameBlackNodesCount(tree);
        }

        fn checkRootBlack(tree: RbTree) {
            if (tree.root != nil) {
                if (isNodeRed(tree.root)) {
                    crashm(getInt(nodeValue(tree.root)), "root of rb-tree must be black");
                }
            }
        }

        fn checkChildrenOfRedIsBlack(node: Cons, parentRed: bool) {
            if (node == nil) return;
            val currentRed = isNodeRed(node);
            if (parentRed) {
                if (currentRed) {
                    printSubTree(nodeParent(node), 0);
                    crashm(getInt(nodeKey(node)), "\nchildren of red node must be black");
                }
            }

            checkChildrenOfRedIsBlack(leftChild(node), currentRed);
            checkChildrenOfRedIsBlack(rightChild(node), currentRed);
        }

        fn checkAllPathsContainsSameBlackNodesCount(tree: RbTree) {
            countAndCheckBlackNodes(tree.root);
        }

        fn countAndCheckBlackNodes(node: Cons): int {
            if (node == nil) return 0;
            val leftCount = countAndCheckBlackNodes(leftChild(node));
            val rightCount = countAndCheckBlackNodes(rightChild(node));
            if (leftCount != rightCount) crashm(rightCount, "black nodes count in left and right must be same");
            if (isNodeBlack(node)) {
                return leftCount + 1;
            }

            return leftCount;
        }
    """.trimIndent()

    private fun makeCongruentTree() = """
        ${rbTreeRequirements()}

        fn makeCongruentTree(mem: AutoMemory, count: int): RbTree {
            val tree = makeRbTree(mem);
            var keyGen = 1;

            for(var i = 0; i < count; i = i + 1){
                keyGen = (keyGen * 8121 + 28411) % 134456;
                val k = makeNumber(mem, keyGen);
                val v = makeNumber(mem, i);
                val minusK = makeNumber(mem, -1*keyGen);
                val minusV = makeNumber(mem, i*keyGen);

                putRbTree(mem, tree, k, v, ::compare);
                putRbTree(mem, tree, minusK, minusV, ::compare);
            }
            return tree;
        }
    """.trimIndent()

    private fun isBinarySearchTree() = """
        fn checkIsBst(tree: RbTree, compare:(Cons, Cons) -> byte) {
            checkBstNodes(tree.root, nil, nil, compare);
        }

        fn checkBstNodes(node: Cons, min: Cons, max: Cons, compare:(Cons, Cons) -> byte) {
            if (node == nil) return;
            val nodeKey = nodeKey(node);
            checkBounds(nodeKey, min, max, compare);

            checkBstNodes(leftChild(node), min, nodeKey, compare);
            checkBstNodes(rightChild(node), nodeKey, max, compare);
        }

        fn checkBounds(value: Cons, min: Cons, max: Cons, compare:(Cons, Cons) -> byte) {
            if (min != nil) {
                if (compare(min, value) >= 0) crashm(1, "value less then min");
            }
            if (max != nil) {
                if (compare(value, max) >= 0) crashm(2, "value greater then max");
            }
        }
    """.trimIndent()

    private fun copyTree() = """
        fn copyTree(mem: AutoMemory, tree: RbTree): RbTree {
            val copy = makeRbTree(mem);
            copy.root = copyNode(mem, tree.root, nil);
            return copy;
        }

        fn copyNode(mem: AutoMemory, node: Cons, parentCopy: Cons): Cons {
            if (node == nil) return nil;
            val copy = makeRbNode(mem, nodeKey(node), nodeValue(node), parentCopy);
            setNodeLeft(copy, copyNode(mem, leftChild(node), copy));
            setNodeRight(copy, copyNode(mem, rightChild(node), copy));

            return copy;
        }

    """.trimIndent()

    private fun isTreesEquals() = """
        fn isTreesEquals(left: Cons, right: Cons, compare: (Cons, Cons) -> byte): bool {
            if (left == nil) return right == nil;
            if (compare(nodeKeyValue(left), nodeKeyValue(right)) != 0) return false;
            if (isTreesEquals(leftChild(left), leftChild(right), compare)) return isTreesEquals(rightChild(left), rightChild(right), compare);
            return false;
        }

    """.trimIndent()

    private fun checkParentLinks() = """
        fn checkParentLinks(node: Cons, parent: Cons) {
            if (node == nil) return;
            assertRefEq (nodeParent(node), parent, "Link to parent must be equals by reference");
            checkParentLinks(leftChild(node), node);
            checkParentLinks(rightChild(node), node);
        }
    """.trimIndent()
}
