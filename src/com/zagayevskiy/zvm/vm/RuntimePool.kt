package com.zagayevskiy.zvm.vm

import com.zagayevskiy.zvm.util.extensions.pop
import com.zagayevskiy.zvm.util.extensions.push

interface RuntimePool<Wrapper, Inner : Any> {

    fun obtain(value: Inner): Wrapper

    fun recycle(wrapper: Wrapper)

}

class RuntimePoolImpl<Wrapper, Inner : Any>(
        private val maxSize: Int,
        private val wrap: (Inner) -> Wrapper,
        private val mutate: (Wrapper, Inner) -> Unit) : RuntimePool<Wrapper, Inner> {
    private val pool = mutableListOf<Wrapper>()

    override fun obtain(value: Inner): Wrapper {
        if (pool.isEmpty()) return wrap(value)
        return pool.pop().also { mutate(it, value) }
    }

    override fun recycle(wrapper: Wrapper) {
        if (pool.size < maxSize) pool.push(wrapper)
    }
}