package com.zagayevskiy.zvm.common.preprocessing

import com.zagayevskiy.zvm.util.extensions.asSequence

class JavaAssetsIncludesResolver : AbsIncludesResolver() {
    override fun doResolve(path: String): Sequence<Char> {
        return javaClass.getResourceAsStream(path)?.asSequence()?.flatMap { it.asSequence() } ?: emptySequence()
    }
}