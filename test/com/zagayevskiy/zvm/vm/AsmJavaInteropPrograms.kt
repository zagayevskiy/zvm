package com.zagayevskiy.zvm.vm

internal val javaCasts = """
    .fun main: args = 1

    aloadi 0
    itoj
    pop

    consti 1234567
    itoj
    pop

    ${storeStringWithSize("Hello! Привет! Сәлем! Γεια σας!")}
    stoj
    pop

    constb -17
    btoj
    pop

    consti -1234567
    itoj

""".trimIndent()

internal data class EmptyConstructorData(val i: Int = 0)
internal val simpleNewInstance = """
    .fun main
    ${storeClassNameWithSize<EmptyConstructorData>()}
    jnew 0
""".trimIndent()

internal data class DataClass(val i: Int, val b: Byte, val s: String)
internal val constructorWithArguments = """
    .fun main: args = 2

    aloadi 0
    itoj

    aloadb 1
    btoj

    ${storeStringWithSize("Some String ზოგიერთი სიმებიანი")}
    stoj

    ${storeClassNameWithSize<DataClass>()}

    jnew 3

""".trimIndent()

internal data class OverloadDataClass @JvmOverloads constructor(val i: Int = 0, val b: Byte = 0, val s: String = "", val cls: EmptyConstructorData = EmptyConstructorData())
internal val overloadedConstructor = """
    .fun main: locals = 3
    ${storeClassNameWithSize<OverloadDataClass>()}
    lstori 1
    lstori 0

    lloadi 0
    lloadi 1
    jnew 0
    pop

    consti 111
    itoj
    lloadi 0
    lloadi 1
    jnew 1
    pop

    consti 222
    itoj
    constb -22
    btoj
    lloadi 0
    lloadi 1
    jnew 2
    pop

    consti -333
    itoj
    constb 33
    btoj
    ${storeStringWithSize("안녕하세요 낯선")}
    stoj
    dup
    lstori 2

    lloadi 0
    lloadi 1
    jnew 3
    pop

    consti 444
    itoj
    constb 44
    btoj
    lloadi 2

    consti 100000
    itoj
    ${storeClassNameWithSize<EmptyConstructorData>()}
    jnew 1

    lloadi 0
    lloadi 1
    jnew 4


""".trimIndent()

internal data class DataClassFactory(val i: Int) {
    fun create(b: Byte, plus: Byte, minus: Int) = DataClass(i - minus, (b + plus).toByte(), "හෙලෝ ආගන්තුක")
}
internal val javaCall = """
    .fun main: args = 2, locals = 1

    aloadb 1
    btoj
    constb 35
    btoj
    consti 3141592
    itoj

    ${storeStringWithSize("create")}

    aloadi 0
    itoj
    ${storeClassNameWithSize<DataClassFactory>()}
    jnew 1

    jcall 3

""".trimIndent()

private inline fun <reified T> storeClassNameWithSize(): String = storeStringWithSize(T::class.java.name)

private fun storeStringWithSize(s: String): String {
    val bytes = s.toByteArray(Charsets.UTF_8)
    return """
    consti ${bytes.size}
    dup
    alloc
    ${bytes.mapIndexed { index, byte ->
        listOf(
                "dup",
                "consti $index",
                "constb $byte",
                "mstorb")
    }
            .flatten()
            .joinToString(separator = "\n    ")
    }
    """.trimIndent()
}
