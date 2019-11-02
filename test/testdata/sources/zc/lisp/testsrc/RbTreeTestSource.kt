package testdata.sources.zc.lisp.testsrc

import testdata.cases.TestSource
import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStdIo
import testdata.sources.zc.includes.includeStdMem
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

        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}

    """.trimIndent())

    val AllPuttedKeysExistsAndValuesCorrect = TestSource("putted exists", """
        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}

        ${isBinarySearchTree()}

        fn main(): int {
            val tree = makeRbTree();
            val mem = makeAutoMemory(2700);

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

    val PutGet = TestSource("put/get", """

        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}

        fn main(): int {
            testPutGet();
            return 0;
        }

        ${isBinarySearchTree()}

        fn testPutGet() {
            val tree = makeRbTree();
            val mem = makeAutoMemory(27000);

            val count = 30;
            var keyGen = 1;
            for(var i = 0; i < count; i = i + 1){
                keyGen = (keyGen * 8121 + 28411) % 134456;
                val k = makeNumber(mem, keyGen);
                val v = makeNumber(mem, i);
                val minusK = makeNumber(mem, -1*keyGen);
                val minusV = makeNumber(mem, i*keyGen);

                putRbTree(mem, tree, k, v, ::compare);
                checkIsBst(tree, ::compare);
                putRbTree(mem, tree, minusK, minusV, ::compare);
                checkIsBst(tree, ::compare);
            }

            keyGen = 1;
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

    val RedBlackRequirements = TestSource("rb", """
        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}


        fn main(): int {

            val tree = makeRbTree();
            val mem = makeAutoMemory(27000);

            val count = 30;
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

            checkRequirements(tree);

            freeAutoMemory(mem);
            return 0;
        }

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
            if (parentRed && currentRed) {
                crashm(getInt(nodeKey(node)), "children of red node must be black");
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
    """.trimIndent())

    val IsBstCrashedOnNoBst = TestSource("crash on no-bst", """

        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}
        ${isBinarySearchTree()}
        fn main(): int {
            val tree = makeRbTree();
            val mem = makeAutoMemory(27000);

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
}
