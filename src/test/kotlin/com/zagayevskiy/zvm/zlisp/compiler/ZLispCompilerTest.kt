package com.zagayevskiy.zvm.zlisp.compiler

import com.zagayevskiy.zvm.memory.BitTableMemory
import com.zagayevskiy.zvm.vm.BytecodeLoader
import com.zagayevskiy.zvm.vm.LoadingResult
import com.zagayevskiy.zvm.vm.VirtualMachine
import com.zagayevskiy.zvm.zlisp.compiler.ZLispCompiler
import org.junit.Test

class ZLispCompilerTest {

    @Test
    fun test() {
        /*



 something


         */
        val program = """
            number?
               (* (+ 1 2 3 (* 2 3 (+ 10 20))) 4 5)
             T
            nil
            (nil? T)
            (nil? nil)

            (number? T)
            (number? nil)
            (number? ())
            (number? 100500)
            (number? ())

            (quote (something 123))
            something
            (def! something (+ 111 222))
            (* something 3)


            (quote (1 2 (3 3) 4 5))
            (car (list 1 2 4 4))
            (cdr (list 1 2 4 4))
           
        """.trimIndent()
        //(defun test (x) (+ x 1))

        val bytecode = ZLispCompiler().compile(program)
        val loader = BytecodeLoader(bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, 10000, BitTableMemory(1024*1024))
        vm.run(emptyList())
    }

}