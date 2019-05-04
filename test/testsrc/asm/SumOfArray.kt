package testsrc.asm

//run with one int argument(size), creates array of ints (size in bytes = size*4), fill it with indices, and get sum
internal val asmSumOfArray = """
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
    lessi
    jnz body
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
     greqi
     jnz finish
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