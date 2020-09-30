package com.zagayevskiy.zvm.zc

import com.zagayevskiy.zvm.common.preprocessing.IncludesResolver

class ZcPreprocessor(private val programText: String, private val includesResolver: IncludesResolver) {
    fun preprocess(): String {
        var prev: String
        var current = programText
        do {
            prev = current
            current = current.replace(Regex("@include<(.+)>")) { matchResult ->
                val path = matchResult.groups[1]!!.value
                includesResolver.resolve(path) ?: throw IllegalStateException("Can not resolve include path $path")
            }
        } while (current != prev)

        return current
    }
}