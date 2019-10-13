package testdata.sources.zc

import testdata.sources.zc.includes.includeCrash
import testdata.sources.zc.includes.includeStdMem
import testdata.sources.zc.vm.includeBytecodeParser

internal val bytecodeLoading = """
    ${includeStdMem()}
    ${includeCrash()}
    ${includeBytecodeParser()}

    fn main(rawBytecode: [byte], rawBytecodeSize: int): ProgramInfo {
        return parseBytecode(rawBytecode, rawBytecodeSize);
    }

""".trimIndent()