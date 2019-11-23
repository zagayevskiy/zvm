package com.zagayevskiy.zvm.common.preprocessing

interface IncludesResolver {
    fun resolve(path: String): Sequence<Char>
}

abstract class AbsIncludesResolver : IncludesResolver {
    private val alreadyIncluded = setOf<String>()

    final override fun resolve(path: String): Sequence<Char> {
        if (alreadyIncluded.contains(path)) return emptySequence()
        return doResolve(path)
    }

    protected abstract fun doResolve(path: String): Sequence<Char>
}

class CompositeIncludesResolver(private val resolvers: List<IncludesResolver>): AbsIncludesResolver() {
    override fun doResolve(path: String) = resolvers.asSequence().flatMap { it.resolve(path) }
}