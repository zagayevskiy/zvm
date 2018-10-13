package com.zagayevskiy.zvm

import org.hamcrest.CoreMatchers
import org.junit.Assert

fun <T> assertEquals(actual: List<T>, expected: List<T>) {
    Assert.assertThat(actual, CoreMatchers.`is`(expected))
}