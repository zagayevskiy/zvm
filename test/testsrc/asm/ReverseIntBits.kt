package testsrc.asm

import com.zagayevskiy.zvm.vm.Source

object AsmReverse {
    //run with one argument(int) and reverse it bits
    internal val IntBits = Source("Reverse int bits","""
        .fun main: x: int;
        pushfp
        consti x
        mloadi
        call reverse
        ret

        .fun reverse: x: int, locals = 2
        consti 0
        lstori 0

        consti 0
        ->loop
        pushfp
        consti x
        mloadi
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
    """.trimIndent())
}

