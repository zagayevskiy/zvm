package testsrc.asm

//run with one int argument(n) and get n! (computed by recursive way)
internal val asmFactorialRecursive = """
    .fun main: args = 1
    aloadi 0
    consti 0
    greati
    jnz recursion
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