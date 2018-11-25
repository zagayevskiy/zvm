package com.zagayevskiy.zvm.zc

//run with one int argument (n) and get n's Fibonacci number
val zcFibonacchiByLoop = """
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