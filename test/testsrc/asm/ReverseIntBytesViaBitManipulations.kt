package testsrc.asm

//run with one argument(int) and reverse it bytes
internal val asmReverseIntBytesViaBitManipulations = """
    .fun main: args = 1
    aloadi 0
    call reverse
    ret

    .fun reverse: args = 1
    aloadi 0
    consti 24
    shri

    aloadi 0
    consti 8
    shri
    consti 65280
    andi

    aloadi 0
    consti 8
    shli
    consti 16711680
    andi

    aloadi 0
    consti 24
    shli

    ori
    ori
    ori

    ret


""".trimIndent()