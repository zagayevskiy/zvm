package com.zagayevskiy.zvm.util.extensions

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun InputStream.asSequence() = object : Sequence<String> {
    override fun iterator() = object : Iterator<String> {
        private val reader = BufferedReader(InputStreamReader(this@asSequence))
        private var next: String? = reader.readLine()
        private var readingStarted = false
        override fun hasNext(): Boolean {
            if (!readingStarted) {
                startReading()
            }
            return next != null
        }

        override fun next(): String {
            if (!readingStarted) {
                startReading()
            }
            return next?.also { nextLine() } ?: throw NoSuchElementException()
        }

        private fun startReading() {
            readingStarted = true
            nextLine()
        }
        private fun nextLine() {
            next = reader.readLine()
        }
    }
}