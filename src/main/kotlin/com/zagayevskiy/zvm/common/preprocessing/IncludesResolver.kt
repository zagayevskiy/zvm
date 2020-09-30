package com.zagayevskiy.zvm.common.preprocessing

interface IncludesResolver {
    fun resolve(path: String): String?
}

abstract class AbsIncludesResolver : IncludesResolver {
    private val alreadyIncluded = mutableSetOf<String>()

    final override fun resolve(path: String): String? {
        if (alreadyIncluded.contains(path)) return ""
        return doResolve(path).also {
            alreadyIncluded.add(path)
        }
    }

    protected abstract fun doResolve(path: String): String?
}

class CompositeIncludesResolver(private val resolvers: List<IncludesResolver>): AbsIncludesResolver() {
    override fun doResolve(path: String): String? {
        resolvers.forEach { resolver ->
            val resolved = resolver.resolve(path)
            if (resolved != null) return resolved
        }

        return null
    }
}