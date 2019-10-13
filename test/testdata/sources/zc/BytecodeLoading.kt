package testdata.sources.zc

import testdata.sources.zc.vm.includeBytecodeParser

internal val bytecodeLoading = """
    ${includeBytecodeParser()}

    fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        return parseBytecode(rawBytecode, rawBytecodeSize);
    }

""".trimIndent()