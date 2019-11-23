package com.zagayevskiy.zvm

import com.zagayevskiy.zvm.vm.StackEntry
import com.zagayevskiy.zvm.vm.toStackEntry
import org.hamcrest.CoreMatchers
import org.junit.Assert

fun <T> assertEquals(actual: List<T>, expected: List<T>) {
    Assert.assertThat(actual, CoreMatchers.`is`(expected))
}

internal fun entries(vararg values: Int): List<StackEntry> {
    return values.map { it.toStackEntry() }
}

internal fun entries(vararg values: Byte): List<StackEntry> {
    return values.map { it.toStackEntry() }
}