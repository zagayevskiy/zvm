package com.zagayevskiy.zvm.common.preprocessing

interface IncludesResolver {
    fun resolve(path: String): Sequence<Char>
}

abstract class AbsIncludesResolver : IncludesResolver {
    private val alreadyIncluded = mutableSetOf<String>()

    final override fun resolve(path: String): Sequence<Char> {
        if (alreadyIncluded.contains(path)) return emptySequence()
        return doResolve(path).also {
            alreadyIncluded.add(path)
        }
    }

    protected abstract fun doResolve(path: String): Sequence<Char>
}

class CompositeIncludesResolver(private val resolvers: List<IncludesResolver>): AbsIncludesResolver() {
    override fun doResolve(path: String): Sequence<Char> {
        resolvers.forEach {
            val seq = it.resolve(path)
            val iter = seq.iterator()
            if (iter.hasNext()) return iter.asSequence()
        }

        throw IllegalStateException("can not resolve $path")
    }
}