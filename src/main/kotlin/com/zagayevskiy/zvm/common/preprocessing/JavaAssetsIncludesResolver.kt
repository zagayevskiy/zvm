package com.zagayevskiy.zvm.common.preprocessing

import com.zagayevskiy.zvm.util.extensions.asSequence

class JavaAssetsIncludesResolver(private val basePath: String) : AbsIncludesResolver() {
    override fun doResolve(path: String): String? {
        return javaClass.getResourceAsStream("$basePath/$path")?.asSequence()?.joinToString(separator = System.lineSeparator())
    }
}