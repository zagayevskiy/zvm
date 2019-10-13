package testsrc.asm

import com.zagayevskiy.zvm.vm.Source

object AsmFactorial {

    //run with one int argument(n) and get n! (computed by recursive way)
    internal val Recursive = Source("n! recursive", """
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
    internal val Iterative = Source("n! iterative", """
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
        jnz loop
        lloadi 4
        ret
    """.trimIndent())
}
