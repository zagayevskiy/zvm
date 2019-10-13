package com.zagayevskiy.zvm.util.extensions


typealias Stack<T> = MutableList<T>

fun <T> Stack<T>.push(value: T) = add(value)
fun <T> Stack<T>.pop(): T = removeAt(size - 1)
fun <T> Stack<T>.peek(): T = get(size - 1)
fun <T> Stack<T>.peekOrNull(): T? = takeIf { it.isNotEmpty() }?.peek()

fun <T> stack(): Stack<T> = mutableListOf()

fun <T> Iterable<T>.zipWithCondition(other: Iterable<T>, condition: (T, T) -> Boolean): Boolean {
    val iter1 = iterator()
    val iter2 = other.iterator()
    if (!iter1.hasNext() && !iter2.hasNext()) return true

    do {
        if (!condition(iter1.next(), iter2.next())) return false
    } while (iter1.hasNext() && iter2.hasNext())

    return !iter1.hasNext() && !iter2.hasNext()
}