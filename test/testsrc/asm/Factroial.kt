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
}

//run with one int argument(n) and get n! (computed by iterative way)
internal val asmFactorialIterative = """
    .fun main: args = 1, locals = 2
    aloadi 0
    lstori 0
    consti 1
    lstori 1
    ->loop
    lloadi 0
    lloadi 1
    muli
    lstori 1
    lloadi 0
    deci
    lstori 0
    lloadi 0
    jnz loop
    lloadi 1
    ret
""".trimIndent()