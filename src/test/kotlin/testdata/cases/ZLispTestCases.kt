package testdata.cases

import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.zlisp.compiler.ZLispCompiler

internal object ZLispTestCases : MutableList<VmTestCase> by mutableListOf() {

    init {
        oneLiner("nil", "nil")
        oneLiner("something", "nil")
        oneLiner("T", "T")
        oneLiner("()", "nil")

        oneLiner("(+ 1 2 3 4 5)", "15")
        oneLiner("(* 1 2 3 4 5)", "120")

        oneLiner("(nil? nil)", "T")
        oneLiner("(nil? T)", "nil")
        oneLiner("(nil? 100)", "nil")

        oneLiner("(number? nil)", "nil")
        oneLiner("(number? ())", "nil")
        oneLiner("(number? T)", "nil")
        oneLiner("(number? 100)", "T")

        oneLiner("(list 10 20 30 40 50)", "(10 . (20 . (30 . (40 . (50 . nil)))))")

        oneLiner("(car ())", "nil")
        oneLiner("(car (list 100000))", "100000")
        oneLiner("(car (list nil))", "nil")
        oneLiner("(car (list 1 2 3))", "1")
        oneLiner("(cdar (list 1 2 3))", "2")
        oneLiner("(cddar (list 1 2 3))", "3")
        oneLiner("(caar (list (list 100) 200))","100")
        oneLiner("(caaar (list (list (list 10 11) 20 21) 30 31))","10")
        oneLiner("(cadar (list (list (list 10 11) 20 21) 30 31))","20")

        oneLiner("(cdr ())", "nil")
        oneLiner("(cdr (list 1))", "nil")
        oneLiner("(cdr (list 1 2 3))", "(2 . (3 . nil))")
        oneLiner("(cddr (list 1 2 3))", "(3 . nil)")
        oneLiner("(cdddr (list 1 2 3 4))", "(4 . nil)")

        oneLiner("(caddr (list (list (list 10 11) 20 30) 40 41))","(30 . nil)")
        oneLiner("(cdadr (list 1 (list 2 3) 4 5))","(3 . nil)")

        oneLiner("(quote (+ 1 2 3))", "(+ . (1 . (2 . (3 . nil))))")
        oneLiner("(def! x 10000)", "10000")
        oneLiner("(def! x (quote y))", "y")
        oneLiner("(def! f (fn* (x) x))", "lambda")

        oneLiner("""
            (def! plus (fn* (x y) (+ x y)))
            (def! mul (fn* (x y) (* x y)))
            (plus 123 (mul 1 2))
        """.trimIndent(), """
            lambda
            lambda
            125
        """.trimIndent())
    }


    private fun source(source: TestSource, block: ZLispRunBuilder.() -> Unit) {
        ZLispRunBuilder(source).block()
    }

    private fun oneLiner(source: String, output: String) {
        source(TestSource(name = "Lisp one-liner $source", text = source)) {
            run(args = emptyList(), prints = listOf(output))
        }
    }
}


private class ZLispRunBuilder(private val source: TestSource) {
    private val bytecodeProvider = CachingBytecodeProvider {
        ZLispCompiler().compile(source.text)
    }
    var heapSize: Int = VmTestCase.DefaultHeapSize

    fun run(args: List<StackEntry>, prints: List<String>, crashCode: Int? = null) {
        ZLispTestCases.add(PrintVmTestCase(
                """${source.name} ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "")}"""",
                bytecodeProvider,
                runArgs = args,
                expectPrinted = prints,
                expectCrashCode = crashCode,
                joinOutput = true,
                dropLastEmptyLines = true
        ))
    }
}