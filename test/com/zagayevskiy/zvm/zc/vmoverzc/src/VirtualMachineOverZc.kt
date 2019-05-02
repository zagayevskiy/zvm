package com.zagayevskiy.zvm.zc.vmoverzc.src

import com.zagayevskiy.zvm.zc.includes.includeStack
import com.zagayevskiy.zvm.zc.includes.includeStdMem



internal val vmOverZc = """
    ${includeStdMem()}
    ${includeStack()}
    ${includeBytecodeParser()}
    ${includeContext()}

    struct StackFrame {
        var args: [void];
        var locals: [void];
        var returnAddress: int;
    }

    fn main(rawBytecode: [byte], rawBytecodeSize: int): int {
        val programInfo = parseBytecode(rawBytecode, rawBytecodeSize);
        val context = createContext(programInfo);

        call(context, programInfo.serviceInfo.mainIndex);
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
                6 -> jpos(context);
                7 -> jneg(context);

                11 -> push(context);
                12 -> pop(context);
                13 -> dup(context);

                21 -> itob(context);
                22 -> btoi(context);
                23 -> itoj(context);
                24 -> btoj(context);
                25 -> stoj(context);

                40 -> aloadi(context);
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

                -40 -> aloadb(context);
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
        pushStackFrame(context.callStack, createStackFrame(context.ip));
        context.ip = function.address;

        return 0;
    }

    fn ret(context: Context): int {
        popStackFrame(context.callStack);
        return popInt(context.operandsStack);
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
    fn jpos(context: Context): int { return 0; }
    fn jneg(context: Context): int { return 0; }
    fn push(context: Context): int { return 0; }
    fn pop(context: Context): int { return popInt(context.operandsStack); }
    fn dup(context: Context): int { return pushInt(context.operandsStack, peekInt(context.operandsStack)); }

    fn itob(context: Context): int {
        return pushByte(context.operandsStack, cast<byte>(popInt(context.operandsStack)));
    }
    fn btoi(context: Context): int { return 0; }

    fn itoj(context: Context): int { return 0; }
    fn btoj(context: Context): int { return 0; }
    fn stoj(context: Context): int { return 0; }

    fn aloadi(context: Context): int { return 0; }
    fn lstori(context: Context): int { return 0; }
    fn lloadi(context: Context): int { return 0; }
    fn mstori(context: Context): int { return 0; }
    fn mloadi(context: Context): int { return 0; }
    fn consti(context: Context): int { return pushInt(context.operandsStack, nextInt(context)); }
    fn addi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) + popInt(stack));
    }
    fn subi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) - popInt(stack));
    }
    fn inci(context: Context): int { return 0; }
    fn deci(context: Context): int { return 0; }
    fn muli(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) * popInt(stack));
    }
    fn divi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) / popInt(stack));
    }
    fn modi(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) % popInt(stack));
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
        return pushInt(stack, popInt(stack) << popInt(stack));
    }
    fn shri(context: Context): int {
        val stack = context.operandsStack;
        return pushInt(stack, popInt(stack) >> popInt(stack));
    }
    fn cmpi(context: Context): int {
        val stack = context.operandsStack;
        val right = popInt(stack);
        val left = popInt(stack);
        return pushInt(stack, popInt(stack) >> popInt(stack));
    }
    fn cmpic(context: Context): int { return 0; }
    fn lessi(context: Context): int { return 0; }
    fn leqi(context: Context): int { return 0; }
    fn greati(context: Context): int { return 0; }
    fn greqi(context: Context): int { return 0; }
    fn eqi(context: Context): int { return 0; }
    fn neqi(context: Context): int { return 0; }
    fn rndi(context: Context): int { return 0; }
    fn gloadi(context: Context): int { return 0; }
    fn gstori(context: Context): int { return 0; }
    fn aloadb(context: Context): int { return 0; }
    fn lstorb(context: Context): int { return 0; }
    fn lloadb(context: Context): int { return 0; }
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
        return pushByte(stack, popByte(stack) - popByte(stack));
    }
    fn mulb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) * popByte(stack));
    }
    fn divb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) / popByte(stack));
    }
    fn modb(context: Context): int {
        val stack = context.operandsStack;
        return pushByte(stack, popByte(stack) % popByte(stack));
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
    fn cmpb(context: Context): int { return 0; }
    fn cmpbc(context: Context): int { return 0; }
    fn lessb(context: Context): int { return 0; }
    fn leqb(context: Context): int { return 0; }
    fn greatb(context: Context): int { return 0; }
    fn greqb(context: Context): int { return 0; }
    fn eqb(context: Context): int { return 0; }
    fn neqb(context: Context): int { return 0; }
    fn lnotb(context: Context): int { return 0; }
    fn gloadb(context: Context): int { return 0; }
    fn gstorb(context: Context): int { return 0; }

    fn doAlloc(context: Context): int { return 0; }
    fn doFree(context: Context): int { return 0; }

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

    fn createStackFrame(returnAddress: int): StackFrame {
        val frame: StackFrame = alloc(sizeof<StackFrame>);
        frame.returnAddress = returnAddress;
        return frame;
    }

    fn deleteStackFrame(frame: StackFrame): int {
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


