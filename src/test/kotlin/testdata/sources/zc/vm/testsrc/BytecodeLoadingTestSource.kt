package testdata.sources.zc.vm.testsrc

internal val bytecodeLoading = """
    @include<std/mem.zc>
    @include<std/assert.zc>
    @include<zvm/bytecode_parser.zc>

    fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        return parseBytecode(rawBytecode, rawBytecodeSize);
    }

""".trimIndent()