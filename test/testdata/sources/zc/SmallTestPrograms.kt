package testdata.sources.zc

import testdata.sources.asm.AsmFibonacci
import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStack
import testdata.sources.zc.includes.includeStdMem

fun simpleBinaryInt(operation: String) = """
    fn main(left: int, right: int): int {
        return left $operation right;
    }
""".trimIndent()







internal val zcIsPrime = """
    fn main(i: int): bool {
        var divider = 2;
        val true = 0 == 0;
        val false = 0 != 0;
        for(;divider < i;) {
            if ((i % divider) < 1) {return false;}
            divider = divider + 1;
        }
        return true;
    }
""".trimIndent()

internal val whenTest = """
    fn main(b: byte): int {
        when(b) {
            0 -> return 0;
            1 -> return 100;
            2 -> return 200;
            10 -> return 1000;
            5 -> return 500;
            -6 -> return -600;
            else -> return b * 123;
        }
    }
""".trimIndent()

//run with two arguments (int) - size of array and multiplier. Allocate int[size] array, fill it with index*multiplier and then sum all elements/   
internal val zcSumArrayOverAsmInsert = """
    ${intArrayAsmInserts()}

    fn main(size: int, multiplier: int): int {
        val array = createIntArray(size);
        for(var i = 0; i < size; i = i + 1) {
            put(array, i, i*multiplier);
        }
        var result = 0;
        for(var j = 0; j < size; j = j + 1) {
            result = result + get(array, j);
        }
        free(array);

        return result;
    }
""".trimIndent()

private fun intArrayAsmInserts() = """
    fn createIntArray(size: int): int {
        asm{"
            aloadi 0
            consti 4
            muli
            alloc
            ret
        "}
    }

    fn get(array: int, index: int): int {
        asm{"
            aloadi 0
            aloadi 1
            consti 4
            muli
            mloadi
            ret
        "}
    }

    fn put(array: int, index: int, value: int): int {
        asm{"
            aloadi 0
            aloadi 1
            consti 4
            muli
            aloadi 2
            mstori
            consti 0
            ret
        "}
    }

    fn free(array: int): int {
        asm{"
            aloadi 0
            free
            consti 0
            ret
        "}
    }

""".trimIndent()
