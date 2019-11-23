package com.zagayevskiy.zvm.util

fun <T> grabIf(condition: Boolean, grab: () -> T): T? = if (condition) {
    grab()
} else {
    null
}