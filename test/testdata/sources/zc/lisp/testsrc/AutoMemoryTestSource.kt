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

        const MAX_CONS = 100;

        fn main(): int {
            testSingleNumberAllocation();
            testSingleNumberGc();
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
            assertTrue(mem.recycled != nil, "expected number recycled");
        }

        fn assertIntEq(expect: int, actual: int, message: [byte]) {
            if (expect != actual) {
                print(message);
                crash(actual);
            }
        }

        fn assertTrue(condition: bool, message: [byte]) {
            if (!condition) {
                print(message);
                crash(20000);
            }
        }

    """.trimIndent())
}