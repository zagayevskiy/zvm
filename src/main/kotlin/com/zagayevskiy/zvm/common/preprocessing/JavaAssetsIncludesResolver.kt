package com.zagayevskiy.zvm.common.preprocessing

import com.zagayevskiy.zvm.util.extensions.asSequence

class JavaAssetsIncludesResolver(private val basePath: String) : AbsIncludesResolver() {
    override fun doResolve(path: String): Sequence<Char> {
        return javaClass.getResourceAsStream("$basePath/$path")?.asSequence()?.flatMap { it.asSequence() } ?: emptySequence()
    }
}