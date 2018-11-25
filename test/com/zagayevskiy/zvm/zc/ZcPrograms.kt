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