package com.zagayevskiy.zvm.zc

//run with one int argument (n) and get n's Fibonacci number
internal val zcFibonacciIterative = """
    fn main(n: int): int {
        if (n <= 1) return 1;
        var prevStep = 1;
        var currentStep = 1;
        for (var step = n - 2, var temp = 0; step > 0; step = step - 1) {
            temp = prevStep + currentStep;
            prevStep = currentStep;
            currentStep = temp;
        }
        return currentStep;
    }
""".trimIndent()

//run with one int argument (n) and get n's Fibonacci number (computed by recursive way)
internal val zcFibonacciRecursive = """
    fn fibonacci(prevStep: int, currentStep: int, counter: int): int {
        if (counter <= 0)
            return currentStep;
        else
            return fibonacci(currentStep, prevStep + currentStep, counter - 1);
    }

    fn main(n: int): int {
        return fibonacci(1, 1, n - 2);
    }
""".trimIndent()

//run with one int argument (n) and get n's Fibonacci number (computed by iterative way)
internal val zcFactorialIterative = """
    fn main(n: int): int {
        var accum = 1;
        for(var i = 0; i < n; i = i + 1, accum = accum * i) {}
        return accum;
    }
""".trimIndent()

//run with one int argument(n) and get n! (computed by recursive way)
internal val zcFactorialRecursive = """
    fn main(n: int): int {
        if (n <= 1) return 1;
        return main(n - 1)*n;
    }
""".trimIndent()

//run with one argument(int) and reverse it bytes
internal val zcReverseIntBytesViaBitManipulations = """
    fn main(x: int): int {
        return ((x >> 24) & 255) | ((x >> 8) & 65280) | ((x & 255) << 24) | ((x & 65280) << 8);
    }
""".trimIndent()

//run with one argument(int) and reverse it bits
internal val zcReverseIntBits = """
    fn main(x: int): int {
        var result = 0;
        for (var i = 0; i < 32; i = i + 1) result = result | (((x >> i) & 1) << (31 - i));
        return result;
    }
""".trimIndent()