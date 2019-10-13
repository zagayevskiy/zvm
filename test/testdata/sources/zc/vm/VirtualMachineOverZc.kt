package testdata.sources.zc.vm

import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStack
import testdata.sources.zc.includes.includeStdMem


internal val vmOverZc = """
    ${includeCrash()}
    ${includeStdMem()}
    ${includeStack()}
    ${includeBytecodeParser()}
    ${includeContext()}

    struct StackFrame {
        var framePointer: [void];
        var previousStackPointer: [void];
        var returnAddress: int;
    }

    fn main(rawBytecode: [byte], rawBytecodeSize: int, mainArgs: [void], mainArgsBytesSize: int): int {
        val programInfo = parseBytecode(rawBytecode, rawBytecodeSize);
        val context = createContext(programInfo);
        pushAll(context.operandsStack, mainArgs, mainArgsBytesSize);

        call(context, programInfo.mainIndex);
        loop(context);

        return popInt(context.operandsStack);
    }

    fn loop(context: Context): int {
        val bytecodeSize = context.bytecodeSize;
        while(context.ip < bytecodeSize) {
            val code = nextByte(context);
            when(code) {
                1 -> call(context, nextInt(context));
                2 -> {
                    if(context.callStack.size == 1) return popInt(context.operandsStack);
                    ret(context);
                }
                3 -> jump(context, nextInt(context));
                4 -> jz(context);
                5 -> jnz(context);

                12 -> pop(context);
                13 -> dup(context);

                21 -> itob(context);
                22 -> btoi(context);
                23 -> itoj(context);
                24 -> btoj(context);
                25 -> stoj(context);

                41 -> lstori(context);
                42 -> lloadi(context);
                43 -> mstori(context);
                44 -> mloadi(context);
                45 -> consti(context);
                46 -> addi(context);
                47 -> subi(context);
                48 -> inci(context);
                49 -> deci(context);
                50 -> muli(context);
                51 -> divi(context);
                52 -> modi(context);
                53 -> xori(context);
                54 -> andi(context);
                55 -> ori(context);
                56 -> noti(context);
                57 -> shli(context);
                58 -> shri(context);
                59 -> cmpi(context);
                60 -> cmpic(context);
                61 -> lessi(context);
                62 -> leqi(context);
                63 -> greati(context);
                64 -> greqi(context);
                65 -> eqi(context);
                66 -> neqi(context);
                67 -> rndi(context);
                68 -> gloadi(context);
                69 -> gstori(context);

                -41 -> lstorb(context);
                -42 -> lloadb(context);
                -43 -> mstorb(context);
                -44 -> mloadb(context);
                -45 -> constb(context);
                -46 -> addb(context);
                -47 -> subb(context);
                -48 -> mulb(context);
                -49 -> divb(context);
                -50 -> modb(context);
                -51 -> xorb(context);
                -52 -> andb(context);
                -53 -> orb(context);
                -54 -> notb(context);
                -55 -> cmpb(context);
                -56 -> cmpbc(context);
                -57 -> lessb(context);
                -58 -> leqb(context);
                -59 -> greatb(context);
                -60 -> greqb(context);
                -61 -> eqb(context);
                -62 -> neqb(context);
                -63 -> lnotb(context);
                -64 -> gloadb(context);
                -65 -> gstorb(context);

                -11 -> doAlloc(context);
                -12 -> doFree(context);
                else -> return -1;
            }
        }
        return 0;
    }

    fn call(context: Context, functionIndex: int): int {
        val function = context.functions[functionIndex];
        val sp = context.sp;
        val argsMemorySize = function.argsMemorySize;
        context.sp = sp + argsMemorySize;
        copy(context.operandsStack.stack, sp, argsMemorySize);

        pushStackFrame(context.callStack, createStackFrame(context.sp, sp, context.ip));
        context.ip = function.address;

        return 0;
    }

    fn ret(context: Context): int {
        val frame = popStackFrame(context.callStack);
        context.sp = frame.previousStackPointer;
        context.ip = frame.returnAddress;
        freeStackFrame(frame);
        return 0;
    }

    fn jump(context: Context, address: int): int {
        context.ip = address;

        return 0;
    }

    fn jz(context: Context): int {
        val address = nextInt(context);
        val argument = popInt(context.operandsStack);
        if (argument == 0) jump(context, address);
        return 0;
    }
    fn jnz(context: Context): int {
        val address = nextInt(context);
        val argument = popInt(context.operandsStack);
        if (argument != 0) jump(context, address);
        return 0;
    }
    fn pop(context: Context): int { return popInt(context.operandsStack); }
    fn dup(context: Context): int { return pushInt(context.operandsStack, peekInt(context.operandsStack)); }

    fn itob(context: Context): int {
        return pushByte(context.operandsStack, cast<byte>(popInt(context.operandsStack)));
    }
    fn btoi(context: Context): int { return 0; }

    fn itoj(context: Context): int { return 0; }
    fn btoj(context: Context): int { return 0; }
    fn stoj(context: Context): int { return 0; }

    fn lstori(context: Context): int {

        return 0;
    }
    fn lloadi(context: Context): int {
        return 0;
    }
    fn mstori(context: Context): int {
        return 0;
    }
    fn mloadi(context: Context): int { return 0; }
    fn consti(context: Context): int { return pushInt(context.operandsStack, nextInt(context)); }
    fn addi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) + popInt(stack));
    }
    fn subi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, left - right);
    }
    fn inci(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) + 1);
    }
    fn deci(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) - 1);
    }
    fn muli(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) * popInt(stack));
    }
    fn divi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, left / right);
    }
    fn modi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, left % right);
    }
    fn xori(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) ^ popInt(stack));
    }
    fn andi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) & popInt(stack));
    }
    fn ori(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) | popInt(stack));
    }
    fn noti(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, !popInt(stack));
    }

    fn shli(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, left << right);
    }
    fn shri(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, left >> right);
    }
    fn cmpi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, compareInts(left, right));
    }
    fn cmpic(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, compareInts(popInt(stack), nextInt(context)));
    }
    fn lessi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left < right));
    }
    fn leqi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left <= right));
    }
    fn greati(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left > right));
    }
    fn greqi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left >= right));
    }
    fn eqi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left == right));
    }
    fn neqi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushByte(stack, cast<byte>(left != right));
    }
    fn rndi(context: Context): int {
        asm {"
            rndi
            ret
        "}
    }

    fn gloadi(context: Context): int { return 0; }
    fn gstori(context: Context): int { return 0; }

    fn lstorb(context: Context): int {
        return 0;
    }
    fn lloadb(context: Context): int {
        return 0;
    }
    fn mstorb(context: Context): int { return 0; }
    fn mloadb(context: Context): int { return 0; }
    fn constb(context: Context): int {
        return pushByte(context.operandsStack, nextByte(context));
    }
    fn addb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) + popByte(stack));
    }
    fn subb(context: Context): int {
        val stack = context.operandsStack;
        val right = popByte(stack);
        val left = popByte(stack);
        return pushByte(stack, left - right);
    }
    fn mulb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) * popByte(stack));
    }
    fn divb(context: Context): int {
        val stack = context.operandsStack;
        val right = popByte(stack);
        val left = popByte(stack);
        return pushByte(stack, left / right);
    }
    fn modb(context: Context): int {
        val stack = context.operandsStack;
        val right = popByte(stack);
        val left = popByte(stack);
        return pushByte(stack, left % right);
    }
    fn xorb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) ^ popByte(stack));
    }
    fn andb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) & popByte(stack));
    }
    fn orb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) | popByte(stack));
    }
    fn notb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, !popByte(stack));
    }
    fn cmpb(context: Context): int {
        val stack = context.operandsStack;
        val right = popByte(stack);
        val left = popByte(stack);
        return pushInt(stack, compareBytes(left, right));
    }
    fn cmpbc(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, compareBytes(popByte(stack), nextByte(context)));
    }
    fn lessb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) > popByte(stack)));
    }
    fn leqb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) >= popByte(stack)));
    }
    fn greatb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) < popByte(stack)));
    }
    fn greqb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) <= popByte(stack)));
    }
    fn eqb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) == popByte(stack)));
    }
    fn neqb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(popByte(stack) != popByte(stack)));
    }
    fn lnotb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, cast<byte>(!popByte(stack)));
    }
    fn gloadb(context: Context): int { return 0; }
    fn gstorb(context: Context): int { return 0; }

    fn doAlloc(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, cast<int>(alloc(popInt(stack))));
    }
    fn doFree(context: Context): int {
        return free(cast<[void]>(popInt(context.operandsStack)));
    }

    fn nextInt(context: Context): int {
        val bytecode = context.bytecode;
        val ip = context.ip;
        val result = cast<[int]>(bytecode + ip)[0];
        context.ip = ip + 4;
        return result;
    }

    fn nextByte(context: Context): byte {
        val value = context.bytecode[context.ip];
        context.ip = context.ip + 1;
        return value;
    }

    fn createStackFrame(framePointer: [void], previousStackPointer: [void], returnAddress: int): StackFrame {
        val frame: StackFrame = alloc(sizeof<StackFrame>);
        frame.framePointer = framePointer;
        frame.previousStackPointer = previousStackPointer;
        frame.returnAddress = returnAddress;
        return frame;
    }

    fn print(arg: [void]): int {
        asm{"
            lloadi arg
            out
        "}
        return 0;
    }

    fn freeStackFrame(frame: StackFrame): int {
        return free(frame);
    }

    fn pushStackFrame(stack: Stack, frame: StackFrame): int {
        return pushInt(stack, cast<int>(frame));
    }

    fn popStackFrame(stack: Stack): StackFrame {
        return cast<StackFrame>(popInt(stack));
    }

    fn peekStackFrame(stack: Stack): StackFrame {
        return cast<StackFrame>(peekInt(stack));
    }

    fn compareBytes(left: byte, right: byte): byte {
        if (left == right) {
            return 0;
        } else if (left < right) {
            return -1;
        } else {
            return 1;
        }
    }

    fn compareInts(left: int, right: int): int {
        if (left == right) {
            return 0;
        } else if (left < right) {
            return -1;
        } else {
            return 1;
        }
    }

"""


