package testdata.sources.asm

import testdata.cases.TestSource

object AsmFactorial {

    //run with one int argument(n) and get n! (computed by recursive way)
    internal val Recursive = TestSource("n! recursive", """
        .fun main: n: int;
        pushfp
        consti n
        mloadi
        consti 0
        greati
        jnz recursion
        consti 1
        ret
        ->recursion
        pushfp
        consti n
        mloadi
        deci
        call main
        pushfp
        consti n
        mloadi
        muli
        ret
    """.trimIndent())

    //run with one int argument(n) and get n! (computed by iterative way)
    internal val Iterative = TestSource("n! iterative", """
        .fun main: n: int;
        addsp 8
        lloadi n
        lstori 0
        consti 1
        lstori 4
        ->loop
        lloadi 0
        lloadi 4
        muli
        lstori 4
        lloadi 0
        deci
        lstori 0
        lloadi 0
        itob
        jnz loop
        lloadi 4
        ret
    """.trimIndent())
}
