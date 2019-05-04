package testsrc.asm

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

//run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
internal val asmFibonacciRecursive = """

    .fun main: args = 1
    consti 1
    consti 1
    aloadi 0
    call fib
    ret

    .fun fib: args = 3
    aloadi 2
    cmpic 1
    jz finish

    aloadi 1
    aloadi 0
    aloadi 1
    addi
    aloadi 2
    consti -1
    addi
    call fib
    ret
    ->finish
    aloadi 0
    ret

""".trimIndent()