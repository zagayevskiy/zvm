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

        fn main(): int {
            testPutGet();
            return 0;
        }

        fn testPutGet() {
            val tree = makeRbTree();
            val mem = makeAutoMemory(2700);
            val zero = makeNumber(mem, 0);
            putRbTree(mem, tree, zero, makeNumber(mem, 1), ::compare);
            assertByteEq(0, compare(findTree(tree.root, zero, ::compare), makeNumber(mem, 1)), "must be eq");

            var keyGen = 1;
            for(var i = 0; i < 10; i = i + 1){
                keyGen = (keyGen * (i + 17)) % 317;
                val k = makeNumber(mem, keyGen % (i + 19));
                val v = makeNumber(mem, i);
                putRbTree(mem, tree, k, v, ::compare);

                keyGen = (keyGen * (i + 13)) % 317;
                val minusK = makeNumber(mem, -1*keyGen);
                val minusV = makeNumber(mem, i*keyGen);
                putRbTree(mem, tree, minusK, minusV, ::compare);

                val actual = findTree(tree.root, k, ::compare);
                assertRefEq(v, actual, "expect ref equality");
                assertByteEq(0, compare(v, actual), "expect values eq");

                val minusActual = findTree(tree.root, minusK, ::compare);
                assertRefEq(minusV, minusActual, "expect ref equality");
                assertByteEq(0, compare(minusV, minusActual), "expect values eq");
            }

            freeAutoMemory(mem);
        }

        ${includeStdIo()}
        ${includeStdMem()}
        ${includeAutoMemory()}
        ${includeRedBlackTree()}
        ${includeCrash()}

    """.trimIndent())
}


