package testdata.sources.zc.lisp.src

fun lispMain() = """

    fn main() {

        val runtime = create_runtime(65536);

        free_runtime(runtime);

    }

""".trimIndent()