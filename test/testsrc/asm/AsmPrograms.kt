package testsrc.asm

//run with two int arguments (x, y) and get x*y as result.
internal val asmMulByRecursiveAdd = """

    .fun main: args=2
    aloadi 1
    jz retzero
    aloadi 0
    jz retzero
    aloadi 0
    consti 0
    greqi
    jnz arg0_already_positive
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
    consti 0
    greqi
    jnz finish
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


internal val countersInGlobals = """
    globals = 2

    .fun counterInt0
    gloadi 0
    inci
    gstori 0

    consti 0
    ret

    .fun counterByte1
    gloadb 1
    constb 2
    addb
    gstorb 1

    consti 0
    ret

    .fun main
    call init
    pop

    call counterInt0
    call counterByte1
    pop
    pop

    call counterInt0
    call counterByte1
    pop
    pop

    call counterInt0
    pop
    call counterByte1
    pop

    gloadb 1
    btoi
    gloadi 0
    muli
    ret

    .fun init
    constb 1
    consti 1
    gstori 0
    gstorb 1

    consti 0
    ret

""".trimIndent()