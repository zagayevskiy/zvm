package testsrc.asm

import com.zagayevskiy.zvm.asm.IntAdd
import com.zagayevskiy.zvm.asm.Opcode
import com.zagayevskiy.zvm.vm.Source


object AsmSimple {

    val JustRet0 = Source("just ret 0", """
        .fun main
        consti 0
        ret
    """.trimIndent())

    val JustRetArg = Source("just ret arg", """
        .fun main: x: int;
        pushfp
        consti x
        mloadi
        ret
    """.trimIndent())

    val Sum = Source("x+y", IntAdd.binarySource())

    private fun Opcode.binarySource(): String = """
        .fun main: left: int, right: int;
        lloadi left
        lloadi right
        $name
        ret
    """.trimIndent()
}
