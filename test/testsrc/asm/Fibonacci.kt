package testsrc.asm

import com.zagayevskiy.zvm.vm.Source

object AsmFibonacci {
    //run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
    internal val Recursive = Source("fibonacci recursive","""
        .fun main: number: int;
        consti 1
        consti 1
        pushfp
        consti number
        mloadi
        call fib
        ret

        .fun fib: f0: int, f1: int, number: int;
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
}

//Function body. Expects 1 arg and 3 locals declared. Return n's Fibonacci number
internal val asmFibonacciIterativeFunctionBody = """
    aloadi 0
    cmpic 2
    lstorb 0
    lloadb 0
    constb 0
    lessb
    jnz ret1
    lloadb 0
    jz ret1
    aloadi 0
    lstori 2

    consti 1
    consti 1
    lstori 0
    lstori 1
    ->loop
    lloadi 2
    consti -1
    addi
    lstori 2
    lloadi 2
    jz finish
    lloadi 0
    lloadi 1
    addi
    lloadi 1
    lstori 0
    lstori 1
    jmp loop
    ->finish
    lloadi 0
    ret
    ->ret1
    consti 1
    ret
""".trimIndent()

//run with one int argument (n) and get n's Fibonacci number
internal val asmFibonacciIterative = """
    .fun main: args=1, locals=3
    $asmFibonacciIterativeFunctionBody
""".trimIndent()
