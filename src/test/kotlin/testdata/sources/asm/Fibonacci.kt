package testdata.sources.asm

import testdata.cases.TestSource

internal object AsmFibonacci {
    //run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
    val Recursive = TestSource("fibonacci recursive", """
        .fun main: number: int
        consti 1
        consti 1
        pushfp
        consti number
        mloadi
        call fib
        ret

        .fun fib: f0: int, f1: int, number: int
        pushfp
        consti number
        mloadi
        cmpic 1
        jz finish

        pushfp
        consti f1
        mloadi
        pushfp
        consti f0
        mloadi
        pushfp
        consti f1
        mloadi
        addi
        pushfp
        consti number
        mloadi
        consti -1
        addi
        call fib
        ret
        ->finish
        pushfp
        consti f0
        mloadi
        ret

    """.trimIndent())

    //run with one int argument (n) and get n's Fibonacci number
    val Iterative = TestSource("fibonacci iterative", """
        .fun main: n: int
        ${iterativeFunctionBody()}
    """.trimIndent())

    //Function body. Expects 1 arg declared. Return n's Fibonacci number
    fun iterativeFunctionBody() = """
        addsp 12
        lloadi -4
        cmpic 2
        lstorb 0
        lloadb 0
        constb 0
        lessb
        jnz ret1
        lloadb 0
        jz ret1
        lloadi -4
        lstori 8
    
        consti 1
        consti 1
        lstori 0
        lstori 4
        ->loop
        lloadi 8
        consti -1
        addi
        lstori 8
        lloadi 8
        itob
        jz finish
        lloadi 0
        lloadi 4
        addi
        lloadi 4
        lstori 0
        lstori 4
        jmp loop
        ->finish
        lloadi 0
        ret
        ->ret1
        consti 1
        ret
    """.trimIndent()
}
