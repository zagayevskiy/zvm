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
        val program = """
            (* (+ 1 2 3 (* 2 3 (+ 2 2))) 4 5)
            
        """.trimIndent()
        //(defun test (x) (+ x 1))

        val bytecode = ZLispCompiler().compile(program)
        val loader = BytecodeLoader(bytecode)
        val info = (loader.load() as LoadingResult.Success).info
        val vm = VirtualMachine(info, 10000, BitTableMemory(1024*1024))
        vm.run(emptyList())
    }

}