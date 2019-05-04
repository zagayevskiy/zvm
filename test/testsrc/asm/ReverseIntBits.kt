package testsrc.asm

//run with one argument(int) and reverse it bits
internal val asmReverseIntBits = """
    .fun main: args = 1
    aloadi 0
    call reverse
    ret

    .fun reverse: args = 1, locals = 2
    consti 0
    lstori 0

    consti 0
    ->loop
    aloadi 0
    lloadi 0
    shri
    consti 1
    andi
    consti 31
    lloadi 0
    subi
    shli
    ori

    lloadi 0
    inci
    lstori 0
    lloadi 0
    cmpic 32
    jnz loop

    ret
""".trimIndent()