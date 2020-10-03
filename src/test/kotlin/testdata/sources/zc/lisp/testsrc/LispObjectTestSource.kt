package testdata.sources.zc.lisp.testsrc

import testdata.cases.TestSource

object LispObjectTestSource {
    val Test = TestSource("LispObject test", """

        @include<std/mem.zc>
        @include<std/io.zc>
        @include<std/assert.zc>
        @include<lisp/automem.zc>

        const MAX_CONS = 150;

        fn main(): int {
            testUserBits();
            testCaaaaddddr();
            return 0;
        }

        fn testUserBits() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);

            val node = cons(mem, nil, nil);
            setUserBit0(node, true);
            assertTrue(userBit0(node), "0 bit must be true");
            assertType(node, CT_LIST, "type must not be changed");
            setUserBit0(node, false);
            assertTrue(!userBit0(node), "0 bit must be false");
            assertType(node, CT_LIST, "type must not be changed");

            freeAutoMemory(mem);
        }

        fn testCaaaaddddr() {
            val mem = makeAutoMemory(MAX_CONS*sizeof<Cons>);

            val n1 = makeNumber(mem, 10000);
            val n2 = makeNumber(mem, 20000);
            val n1n2 = cons(mem, n1, n2);

            assertIntEq(10000, getInt(car(n1n2)), "car");
            assertIntEq(20000, getInt(cdr(n1n2)), "cdr");

            val n3 = makeNumber(mem, 30000);
            val n4 = makeNumber(mem, 40000);
            val n3n4 = cons(mem, n3, n4);

            val n1n2_n3n4 = cons(mem, n1n2, n3n4);

            assertIntEq(10000, getInt(caar(n1n2_n3n4)), "caar");
            assertIntEq(20000, getInt(cadr(n1n2_n3n4)), "cadr");
            assertIntEq(30000, getInt(cdar(n1n2_n3n4)), "cdar");
            assertIntEq(40000, getInt(cddr(n1n2_n3n4)), "cddr");

            val n5 = makeNumber(mem, 50000);
            val n6 = makeNumber(mem, 60000);
            val n5n6 = cons(mem, n5, n6);

            val n1n2_n3n4_n5n6 = cons(mem, n1n2, cons(mem, n3n4, n5n6));
            assertIntEq(30000, getInt(cdaar(n1n2_n3n4_n5n6)), "cdaar");
            assertIntEq(40000, getInt(cdadr(n1n2_n3n4_n5n6)), "cdadr");
            assertIntEq(50000, getInt(cddar(n1n2_n3n4_n5n6)), "cddar");
            assertIntEq(60000, getInt(cdddr(n1n2_n3n4_n5n6)), "cdddr");
            
            val n7 = makeNumber(mem, 70000);
            val n8 = makeNumber(mem, 80000);
            val n1n2_n3n4__n5n6_n7n8 = cons(mem, n1n2_n3n4, cons(mem, n5n6, cons(mem, n7, n8)));
            assertIntEq(10000, getInt(caaar(n1n2_n3n4__n5n6_n7n8)), "caaar");
            assertIntEq(20000, getInt(caadr(n1n2_n3n4__n5n6_n7n8)), "caadr");
            assertIntEq(30000, getInt(cadar(n1n2_n3n4__n5n6_n7n8)), "cadar");
            assertIntEq(40000, getInt(caddr(n1n2_n3n4__n5n6_n7n8)), "caddr");
            assertIntEq(50000, getInt(cdaar(n1n2_n3n4__n5n6_n7n8)), "cdaar");
            assertIntEq(60000, getInt(cdadr(n1n2_n3n4__n5n6_n7n8)), "cdadr");
            assertIntEq(70000, getInt(cddar(n1n2_n3n4__n5n6_n7n8)), "cddar");
            assertIntEq(80000, getInt(cdddr(n1n2_n3n4__n5n6_n7n8)), "cdddr");


            freeAutoMemory(mem);
        }

    """.trimIndent())
}