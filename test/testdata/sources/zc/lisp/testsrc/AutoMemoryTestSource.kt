package testdata.sources.zc.lisp.testsrc

import testdata.cases.TestSource
import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStdIo
import testdata.sources.zc.includes.includeStdMem
import testdata.sources.zc.lisp.src.includeAutoMemory

object AutoMemoryTestSource {
    val Test = TestSource("Auto memory test", """

        ${includeStdMem()}
        ${includeStdIo()}
        ${includeCrash()}
        ${includeAutoMemory()}

        const MAX_CONS = 150;

        fn main(): int {
            testSingleNumberAllocation();
            testSingleNumberGc();
            testEntireMemoryAllocated();
            testEntireMemoryGc();
            testReachableNoGcAndUnreachableGc();
            return 0;
        }

        fn testSingleNumberAllocation() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);
            val number = makeNumber(mem, 123456);
            val actual = getInt(number);
            assertIntEq(123456, actual, "expected 123456");
            assertIntEq(MAX_CONS - 1, mem.available, "expected one cons allocated");
            assertIntEq(sizeof<Cons>, mem.unlayouted, "expected unlayouted moved");
        }

        fn testSingleNumberGc() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);
            val number = makeNumber(mem, 123456);
            gc(mem, nil);
            assertIntEq(MAX_CONS, mem.available, "expected number collected");
            assertIntEq(sizeof<Cons>, mem.unlayouted, "expected unlayouted not changed");
            assertRecycledCount(1, mem, "expected number recycled");
        }

        fn testEntireMemoryAllocated() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);
            for (var i = 0; i < MAX_CONS; i = i + 1) {
                makeNumber(mem, i);
            }

            assertIntEq(0, mem.available, "expected entire memory allocated");
            assertIntEq(mem.size, mem.unlayouted, "expected no unlayouted memory");
            assertRecycledCount(0, mem, "expected no objects recycled");
        }

        fn testEntireMemoryGc() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);
            for (var i = 0; i < MAX_CONS; i = i + 1) {
                makeNumber(mem, i);
            }

            gc(mem, nil);

            assertIntEq(MAX_CONS, mem.available, "expected entire memory available");
            assertIntEq(mem.size, mem.unlayouted, "expected no unlayouted memory");

            assertRecycledCount(MAX_CONS, mem, "expected all objects recycled");
        }

        fn testReachableNoGcAndUnreachableGc() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);
            var root: Cons = nil;
            for (var i = 0; i < MAX_CONS/3; i = i + 1) {
                root = cons(mem, makeNumber(mem, i), root);
                setUserBit0(root, true);
                makeNumber(mem, i*1000);
            }

            gc(mem, root);
            assertIntEq(MAX_CONS/3, mem.available, "expected 1/3 mem available");
            assertIntEq(mem.size, mem.unlayouted, "expected no unlayouted memory");
            assertRecycledCount(MAX_CONS/3, mem, "expected unreachable objects recycled");
            var n = MAX_CONS / 3 - 1;
            var cursor = root;
            while(cursor != nil) {
                val number = car(cursor);
                val value = getInt(number);
                assertIntEq(n, value, "expect list in reversed order");
                n = n - 1;
                cursor = cdr(cursor);
            }
        }

        fn assertRecycledCount(expect: int, mem: AutoMemory, message: [byte]) {
            var cursor = mem.recycled;
            var recycledCount = 0;
            while(cursor != nil) {
                cursor = cdr(cursor);
                recycledCount = recycledCount + 1;
            }
            assertIntEq(expect, recycledCount, message);
        }
    """.trimIndent())
}