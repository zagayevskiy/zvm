package testdata.sources.zc.vm.testsrc

import testdata.sources.zc.vm.src.includeBytecodeParser

internal val bytecodeLoading = """
    @include<std/mem.zc>
    @include<std/assert.zc>
    ${includeBytecodeParser()}

    fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        return parseBytecode(rawBytecode, rawBytecodeSize);
    }

""".trimIndent()