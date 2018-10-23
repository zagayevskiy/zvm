package com.zagayevskiy.zvm.util.extensions


typealias Stack<T> = MutableList<T>

fun <T> Stack<T>.push(value: T) = add(value)
fun <T> Stack<T>.pop(): T = removeAt(size - 1)
fun <T> Stack<T>.peek(): T = get(size - 1)

fun <T> stack(): Stack<T> = mutableListOf()