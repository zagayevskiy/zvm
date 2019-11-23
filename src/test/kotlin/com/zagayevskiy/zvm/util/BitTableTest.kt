package com.zagayevskiy.zvm.util

import com.zagayevskiy.zvm.util.extensions.fill
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitTableTest {

    private lateinit var table: BitTable

    @Before
    fun setUp() {
        table = BitTable(5000)
    }

    @Test
    fun initiallyEmpty() {
        table.iterator()
        assert(table.all { !it })
    }

    @Test
    fun settingTrueToAll_leadsTo_GettingTrueAtAll() {
        for (i in 0 until table.size) {
            table[i] = true
        }

        assert(table.all { it })
    }

    @Test
    fun settingTrueToOdd_leadsTo_gettingTrueAtOddAndFalseAtEven() {
        for (i in 1 until table.size step 2) {
            table[i] = true
        }

        assert(table.mapIndexed { index, bit -> index.odd() == bit }.all { it })
    }

    @Test
    fun settingTrueToEven_leadsTo_gettingFalseAtOddAndTrueAtEven() {
        for (i in 0 until table.size step 2) {
            table[i] = true
        }

        assert(table.mapIndexed { index, bit -> index.even() == bit }.all { it })
    }

    @Test
    fun settingFalseAfterTrue_leadTo_gettingFalse() {
        for (i in 0 until table.size) {
            table[i] = true
        }

        for (i in 0 until table.size step 3) {
            table[i] = false
        }

        assert(table.mapIndexed { index, bit -> (index % 3 != 0) == bit }.all { it })
    }

    @Test
    fun fill_leadsTo_fillingOnlySelectedPiece() {
        val from = table.size/4
        val to = table.size/3*2
        table.fill(from, to, true)

        ((0 until from) + (to until table.size)).forEach {
            assertFalse (table[it], "error at $it")
        }

        (from until to).forEach {
            assertTrue(table[it], "error at $it")
        }
    }

    //region Cardinality tests
    @Test
    fun emptyTableCardinality_is_Zero() {
        assertEquals(0, table.cardinality())
    }

    @Test
    fun settingTrue_leadsTo_cardinalityChanging() {
        for (i in 0 until table.size) {
            table[i] = true
            assertEquals(i + 1, table.cardinality())
        }
    }

    @Test
    fun settingFalseAfterTrue_leadsTo_cardinalityChanging() {
        table.fill(0, table.size, true)

        for (i in 0 until table.size) {
            table[i] = false
            assertEquals(table.size - i - 1, table.cardinality())
        }
    }

    //endregion Cardinality tests

}

private fun Int.even() = this % 2 == 0
private fun Int.odd() = this % 2 == 1