package testsrc.asm


//run with one argument(int) and reverse it bytes(via memory manipulations)
internal val asmReverseIntBytesViaMemoryManipulations = """
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