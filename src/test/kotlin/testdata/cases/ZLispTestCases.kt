package testdata.cases

import com.zagayevskiy.zvm.common.preprocessing.JavaAssetsIncludesResolver
import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.zlisp.compiler.ZLispCompiler

internal object ZLispTestCases : MutableList<VmTestCase> by mutableListOf() {

    init {
        oneLiner("""
            (def! f (fn* (x) x))
            (f 3)
        """.trimIndent(), """
            lambda
            3
        """.trimIndent())
        oneLiner("nil", "nil")
        oneLiner("something", "nil")
        oneLiner("T", "T")
        oneLiner("()", "nil")

        oneLiner("(+ 11 22 33 44 55)", "${11 + 22 + 33 + 44 + 55}")
        oneLiner("(- 1 2 3 4 5)", "${1 - 2 - 3 - 4 - 5}")
        oneLiner("(- 100)", "-100")
        oneLiner("(* 1 2 3 4 5)", "120")

        oneLiner("(|| nil nil T)", "T")
        oneLiner("(|| nil nil 1 2 3)", "1")
        oneLiner("(|| nil nil 1 (panic! ||must-be-lazy))", "1")
        oneLiner("(|| nil (nil? 1) (number? nil) ())", "nil")
        oneLiner("(|| (> 1 2 3) (< 1 2 3))", "T")

        oneLiner("(&& T T)", "T")
        oneLiner("(&& nil T)", "nil")
        oneLiner("(&& T T T nil)", "nil")
        oneLiner("(&& 1 2 3)", "T")
        oneLiner("(&& 1 nil (panic! &&must-be-lazy))", "nil")
        oneLiner("(&& T 1000 (nil? nil) (number? 10) (< 1 10 100))", "T")
        oneLiner("(&& (> 1 2 3) (< 1 2 3))", "nil")

        oneLiner("(= T T)", "T")
        oneLiner("(= nil nil)", "T")
        oneLiner("(= nil T)", "nil")
        oneLiner("(= nil ())", "T")
        oneLiner("(= nil () (number? T) (nil? T))", "T")
        oneLiner("(= nil () (number? T) (nil? nil))", "nil")
        oneLiner("(= 4 (+ 1 1 2) (* 2 2))", "T")
        oneLiner("(= 1 2 1)", "nil")
        oneLiner("(= (quote (1 2 10)) (list 1 (+ 1 1) (* (+ 1 1) 5)))", "T")
        oneLiner("(= (list 1 2) (list 2 1))", "nil")
        oneLiner("(= 1 1 1 2 (panic! =must-ne-lazy))", "nil")

        oneLiner("(< 1 2)", "T")
        oneLiner("(< 2 1)", "nil")
        oneLiner("(< 1 2 (+ 1 2) (* 2 2) 5)", "T")
        oneLiner("(< 2 3 4 5 1)", "nil")
        oneLiner("(< 2 3 1 (panic! <must-be-lazy) 4 5 1)", "nil")

        oneLiner("(<= 1 2)", "T")
        oneLiner("(<= 1 1 1)", "T")
        oneLiner("(<= 2 2 1)", "nil")
        oneLiner("(<= 1 2 (+ 1 2) (* 2 2) 4 5)", "T")
        oneLiner("(<= 2 2 4 5 1)", "nil")
        oneLiner("(<= 2 2 3 1 (panic! <=must-be-lazy) 4 5 1)", "nil")

        oneLiner("(> 1 2)", "nil")
        oneLiner("(> 2 1)", "T")
        oneLiner("(> 5 (* 2 2) (+ 1 2) 2 1)", "T")
        oneLiner("(> 5 4 3 1 2)", "nil")
        oneLiner("(> 3 1 2 (panic! >must-be-lazy) 4 5 1)", "nil")

        oneLiner("(>= 1 2)", "nil")
        oneLiner("(>= 2 1 1)", "T")
        oneLiner("(>= 5 (* 2 2) (+ 1 1) 2 2 2 1)", "T")
        oneLiner("(>= 5 4 4 3 1 2)", "nil")
        oneLiner("(>= 3 1 1 2 (panic! >=must-be-lazy) 4 5 1)", "nil")

        oneLiner("(nil? nil)", "T")
        oneLiner("(nil? T)", "nil")
        oneLiner("(nil? 100)", "nil")

        oneLiner("(number? nil)", "nil")
        oneLiner("(number? ())", "nil")
        oneLiner("(number? T)", "nil")
        oneLiner("(number? 100)", "T")

        oneLiner("(cond (T 300))", "300")
        oneLiner("(cond (T . 300) (T 400))", "300")
        oneLiner("(cond (nil 300) (T 1000))", "1000")
        oneLiner("(cond (nil . 300) (T 1000))", "1000")
        oneLiner("(cond ((number? T) 1) ((nil? T) 2) (3 4) (5 6) (T 7))", "4")
        oneLiner("(cond (T 10) ((panic!) cond-must-be-lazy))", "10")

        oneLiner("(list 10 20 30 40 50)", "(10 . (20 . (30 . (40 . (50 . nil)))))")

        oneLiner("(car ())", "nil")
        oneLiner("(car (list 100000))", "100000")
        oneLiner("(car (list nil))", "nil")
        oneLiner("(car (list 1 2 3))", "1")
        oneLiner("(cdar (list 1 2 3))", "2")
        oneLiner("(cddar (list 1 2 3))", "3")
        oneLiner("(caar (list (list 100) 200))", "100")
        oneLiner("(caaar (list (list (list 10 11) 20 21) 30 31))", "10")
        oneLiner("(cadar (list (list (list 10 11) 20 21) 30 31))", "20")

        oneLiner("(cdr ())", "nil")
        oneLiner("(cdr (list 1))", "nil")
        oneLiner("(cdr (list 1 2 3))", "(2 . (3 . nil))")
        oneLiner("(cddr (list 1 2 3))", "(3 . nil)")
        oneLiner("(cdddr (list 1 2 3 4))", "(4 . nil)")

        oneLiner("(caddr (list (list (list 10 11) 20 30) 40 41))", "(30 . nil)")
        oneLiner("(cdadr (list 1 (list 2 3) 4 5))", "(3 . nil)")

        oneLiner("(quote (+ 1 2 3))", "(+ . (1 . (2 . (3 . nil))))")
        oneLiner("(quote (T . T))", "(T . T)")
        oneLiner("(quote (nil . T))", "(nil . T)")
        oneLiner("(quote (T . nil))", "(T . nil)")
        oneLiner("(quote (T))", "(T . nil)")

        oneLiner("(def! x 10000)", "10000")
        oneLiner("(def! x (quote y))", "y")
        oneLiner("(def! f (fn* (x) x))", "lambda")

        oneLiner("(let* (x 1010) x)", "1010")
        oneLiner("(let* (a 1 b 2 c (+ a b 3)) c)", "6")
        oneLiner("(let* (x (let* (x let*-must-not-modify-outer-env) 123)) x)", "123")
        oneLiner("(let* (x 3 y 2) (let* (a x b y) (* a b)))", "6")

        file("simple-fns.lisp", output = "110")
        file("simple-tail-call-fn.lisp", output = "1000")
        file("lists.lisp", output = "(3 . (2 . (1 . nil)))")
    }


    private fun source(source: TestSource, block: ZLispRunBuilder.() -> Unit) {
        ZLispRunBuilder(source).block()
    }

    private fun oneLiner(source: String, output: String) {
        source(TestSource(name = "Lisp one-liner $source", text = source)) {
            run(args = emptyList(), prints = listOf(output))
        }
    }


    private fun file(path: String, output: String) {
        ZLispFileRunBuilder(path).run(args = emptyList(), prints = output)
    }
}

private class ZLispFileRunBuilder(private val path: String) {
        private val bytecodeProvider = CachingBytecodeProvider {
                val basePath = "/test/lisp"
                val text = JavaAssetsIncludesResolver(basePath).resolve(path) ?: throw IllegalArgumentException("file $path not found at resource:$basePath")
                ZLispCompiler().compile(text)
        }
        var heapSize: Int = VmTestCase.DefaultHeapSize

        fun run(args: List<StackEntry>, prints: String, crashCode: Int? = null) {
                ZLispTestCases.add(PrintVmTestCase(
                    """file: $path ${(args.map { it.toString() }.takeIf { it.isNotEmpty() } ?: "")}"""",
                    bytecodeProvider,
                    runArgs = args,
                    expectPrinted = listOf(prints),
                    expectCrashCode = crashCode,
                    joinOutput = false,
                    dropLastEmptyLines = true,
                    takeOnlyLastLine = true
                ))
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