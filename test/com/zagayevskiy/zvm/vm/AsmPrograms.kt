package com.zagayevskiy.zvm.vm

//run with two int arguments (x, y) and get x*y as result.
internal val mulByRecursiveAdd = """

    .fun main: args=2
    aloadi 1
    jz retzero
    aloadi 0
    jz retzero
    aloadi 0
    jpos arg0_already_positive
    aloadi 0
    consti -1
    muli
    jmp arg0_positive_now
    ->arg0_already_positive
    aloadi 0
    ->arg0_positive_now
    aloadi 1
    call mulByRecursiveAdd
    aloadi 0
    jpos finish
    consti -1
    muli
    ->finish
    ret
    ->retzero
    consti 0
    ret

    .fun mulByRecursiveAdd: args=2
    aloadi 0
    jnz continue
    consti 0
    ret
    ->continue
    aloadi 0
    consti -1
    addi
    aloadi 1
    call mulByRecursiveAdd
    aloadi 1
    addi
    ret

""".trimIndent()

//run with one int argument (n) and get n's Fibonacci number
internal val fibonacci = """
    .fun main: args=1, locals=3
    aloadi 0
    cmpic 2
    lstori 0
    lloadi 0
    jneg ret1
    lloadi 0
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

//run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
internal val fibonacciRecursive = """

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

//run with one int argument(n) and get n!
internal val factorial = """
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

//run with one int argument(n) and get n! (computed by recursive way)
internal val factorialRecursive = """
    .fun main: args = 1
    aloadi 0
    jpos recursion
    consti 1
    ret
    ->recursion
    aloadi 0
    deci
    call main
    aloadi 0
    muli
    ret
""".trimIndent()

//run with one int argument(size), creates array of ints (size in bytes = size*4), fill it with indices, and get sum
internal val sumOfArray = """
    .fun createArray: args = 1, locals = 2
    aloadi 0
    consti 4
    muli
    alloc
    lstori 0
    consti 0
    lstori 1

    ->loop
    lloadi 1
    aloadi 0
    cmpi
    jneg body
    jmp finish
    ->body
    lloadi 0
    lloadi 1
    consti 4
    muli
    lloadi 1
    mstori
    lloadi 1
    inci
    lstori 1
    jmp loop
    ->finish
    lloadi 0
    ret

    .fun sumArray: args = 2, locals = 1
     consti 0
     lstori 0

     consti 0
     ->loop
     lloadi 0
     aloadi 1
     cmpi
     jpos finish
     aloadi 0
     lloadi 0
     consti 4
     muli
     mloadi
     addi
     lloadi 0
     inci
     lstori 0
     jmp loop
     ->finish
     ret

     .fun main: args = 1, locals = 1
     aloadi 0
     call createArray
     lstori 0
     lloadi 0
     aloadi 0
     call sumArray
     lloadi 0
     free
     ret

""".trimIndent()

//run with one argument(int) and reverse it bytes(via memory manipulations)
internal val reverseIntBytesViaMemory = """
    .fun reverse: args = 1, locals = 1
    consti 4
    alloc
    lstori 0
    lloadi 0
    consti 0
    aloadi 0
    mstori

    lloadi 0
    consti 0
    lloadi 0
    consti 3
    mloadb

    lloadi 0
    consti 3
    lloadi 0
    consti 0
    mloadb

    lloadi 0
    consti 1
    lloadi 0
    consti 2
    mloadb

    lloadi 0
    consti 2
    lloadi 0
    consti 1
    mloadb

    mstorb
    mstorb
    mstorb
    mstorb

    lloadi 0
    consti 0
    mloadi

    lloadi 0
    free
    ret

    .fun main: args=1
    aloadi 0
    call reverse
    ret

""".trimIndent()