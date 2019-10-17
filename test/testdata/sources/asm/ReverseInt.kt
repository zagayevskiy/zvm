package testdata.sources.asm

import testdata.cases.TestSource

object AsmReverse {
    //run with one argument(int) and reverse it bits
    internal val IntBits = TestSource("Reverse int bits", """
        .fun main: x: int
        pushfp
        consti x
        mloadi
        call reverse
        ret

        .fun reverse: x: int
        incspi
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

        decspi
        ret
    """.trimIndent())
    
    object IntBytes {
        //run with one argument(int) and reverse it bytes
        internal val ViaBitOps = TestSource("Reverse int bytes via bit ops", """
            .fun main: x: int
            lloadi x
            call reverse
            ret
        
            .fun reverse: x: int
            lloadi x
            consti 24
            shri
        
            lloadi x
            consti 8
            shri
            consti 65280
            andi
        
            lloadi x
            consti 8
            shli
            consti 16711680
            andi
        
            lloadi x
            consti 24
            shli
        
            ori
            ori
            ori
        
            ret
        
        
        """.trimIndent())

        //run with one argument(int) and reverse it bytes(via memory manipulations)
        internal val ViaMemory = TestSource("Reverse int bytes via memory", """
            .fun reverse: x: int
            addsp 4
            consti 4
            alloc
            lstori 0
            lloadi 0
            consti 0
            lloadi x
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
        
            .fun main: x: int
            lloadi x
            call reverse
            ret
        
        """.trimIndent())
    }
}

