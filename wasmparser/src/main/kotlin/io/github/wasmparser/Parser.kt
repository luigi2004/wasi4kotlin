package io.github.wasmparser

/**
 * Payload types that can be returned by the parser.
 */
sealed class Payload {
    object Version : Payload() {
        const val WASM_VERSION = 1u
    }
    
    data class TypeSection(val types: List<FuncType>) : Payload()
    data class ImportSection(val imports: List<Import>) : Payload()
    data class FunctionSection(val typeIndices: List<UInt>) : Payload()
    data class TableSection(val tables: List<TableType>) : Payload()
    data class MemorySection(val memories: List<MemoryType>) : Payload()
    data class GlobalSection(val globals: List<GlobalEntry>) : Payload()
    data class ExportSection(val exports: List<Export>) : Payload()
    data class StartSection(val functionIndex: UInt) : Payload()
    data class ElementSection(val elements: List<ElementSegment>) : Payload()
    data class CodeSection(val functions: List<FunctionBody>) : Payload()
    data class DataSection(val data: List<DataSegment>) : Payload()
    data class DataCountSection(val count: UInt) : Payload()
    data class CustomSection(val name: String, val data: ByteArray) : Payload()
    object End : Payload()
}

/**
 * Global variable entry.
 */
data class GlobalEntry(
    val type: GlobalType,
    val initExpr: List<Operator>
)

/**
 * Element segment for table initialization.
 */
data class ElementSegment(
    val tableIndex: UInt,
    val offset: List<Operator>,
    val elements: List<UInt>
)

/**
 * Data segment for memory initialization.
 */
data class DataSegment(
    val memoryIndex: UInt,
    val offset: List<Operator>,
    val data: ByteArray
)

/**
 * Function body containing locals and code.
 */
data class FunctionBody(
    val locals: List<LocalEntry>,
    val code: ByteArray
)

/**
 * Local variable declaration.
 */
data class LocalEntry(
    val count: UInt,
    val type: ValType
)

/**
 * WebAssembly operators/instructions.
 */
sealed class Operator {
    object Unreachable : Operator()
    object Nop : Operator()
    data class Block(val blockType: BlockType) : Operator()
    data class Loop(val blockType: BlockType) : Operator()
    data class If(val blockType: BlockType) : Operator()
    object Else : Operator()
    object End : Operator()
    data class Br(val relativeDepth: UInt) : Operator()
    data class BrIf(val relativeDepth: UInt) : Operator()
    data class BrTable(val table: io.github.wasmparser.BrTable) : Operator()
    object Return : Operator()
    data class Call(val functionIndex: UInt) : Operator()
    data class CallIndirect(val typeIndex: UInt, val tableIndex: UInt) : Operator()
    object Drop : Operator()
    object Select : Operator()
    data class LocalGet(val localIndex: UInt) : Operator()
    data class LocalSet(val localIndex: UInt) : Operator()
    data class LocalTee(val localIndex: UInt) : Operator()
    data class GlobalGet(val globalIndex: UInt) : Operator()
    data class GlobalSet(val globalIndex: UInt) : Operator()
    data class I32Load(val memArg: MemArg) : Operator()
    data class I64Load(val memArg: MemArg) : Operator()
    data class F32Load(val memArg: MemArg) : Operator()
    data class F64Load(val memArg: MemArg) : Operator()
    data class I32Store(val memArg: MemArg) : Operator()
    data class I64Store(val memArg: MemArg) : Operator()
    data class F32Store(val memArg: MemArg) : Operator()
    data class F64Store(val memArg: MemArg) : Operator()
    data class I32Const(val value: Int) : Operator()
    data class I64Const(val value: Long) : Operator()
    data class F32Const(val value: Float) : Operator()
    data class F64Const(val value: Double) : Operator()
    // Add more operators as needed
}

/**
 * Event-driven parser for WebAssembly binary files.
 * 
 * This parser reads a WebAssembly binary and produces a sequence of payloads
 * representing different sections and components of the module.
 * 
 * Example usage:
 * ```kotlin
 * val wasmBytes = Files.readAllBytes(Paths.get("module.wasm"))
 * val parser = Parser(wasmBytes)
 * 
 * for (payload in parser.parse()) {
 *     when (payload) {
 *         is Payload.TypeSection -> println("Found ${payload.types.size} types")
 *         is Payload.FunctionSection -> println("Found ${payload.typeIndices.size} functions")
 *         // Handle other payloads...
 *         else -> {}
 *     }
 * }
 * ```
 */
class Parser(private val data: ByteArray) {
    
    /**
     * Parse the WebAssembly module and return a sequence of payloads.
     */
    fun parse(): Sequence<Payload> = sequence {
        val reader = BinaryReader(data)
        
        // Check magic number
        val magic = reader.readBytes(4)
        if (!magic.contentEquals(byteArrayOf(0x00, 0x61, 0x73, 0x6d))) {
            throw WasmParserException("Invalid WebAssembly magic number")
        }
        
        // Check version
        val version = reader.readBytes(4)
        if (!version.contentEquals(byteArrayOf(0x01, 0x00, 0x00, 0x00))) {
            throw WasmParserException("Unsupported WebAssembly version")
        }
        
        yield(Payload.Version)
        
        // Parse sections
        while (reader.hasMore()) {
            val sectionId = reader.readVarUInt32().toInt()
            val sectionSize = reader.readVarUInt32().toInt()
            val sectionStart = reader.getPosition()
            
            val sectionCode = SectionCode.fromValue(sectionId)
            
            val payload = when (sectionCode) {
                SectionCode.Custom -> parseCustomSection(reader)
                SectionCode.Type -> parseTypeSection(reader)
                SectionCode.Import -> parseImportSection(reader)
                SectionCode.Function -> parseFunctionSection(reader)
                SectionCode.Table -> parseTableSection(reader)
                SectionCode.Memory -> parseMemorySection(reader)
                SectionCode.Global -> parseGlobalSection(reader)
                SectionCode.Export -> parseExportSection(reader)
                SectionCode.Start -> parseStartSection(reader)
                SectionCode.Element -> parseElementSection(reader)
                SectionCode.Code -> parseCodeSection(reader)
                SectionCode.Data -> parseDataSection(reader)
                SectionCode.DataCount -> parseDataCountSection(reader)
                null -> throw WasmParserException("Unknown section ID: $sectionId")
            }
            
            // Ensure we consumed exactly the expected amount of data
            val consumed = reader.getPosition() - sectionStart
            if (consumed != sectionSize) {
                throw WasmParserException(
                    "Section size mismatch: expected $sectionSize bytes, consumed $consumed bytes"
                )
            }
            
            yield(payload)
        }
        
        yield(Payload.End)
    }
    
    private fun parseCustomSection(reader: BinaryReader): Payload.CustomSection {
        val name = reader.readString()
        val remainingBytes = mutableListOf<Byte>()
        while (reader.hasMore()) {
            remainingBytes.add(reader.readByte())
        }
        return Payload.CustomSection(name, remainingBytes.toByteArray())
    }
    
    private fun parseTypeSection(reader: BinaryReader): Payload.TypeSection {
        val count = reader.readVarUInt32().toInt()
        val types = List(count) { reader.readFuncType() }
        return Payload.TypeSection(types)
    }
    
    private fun parseImportSection(reader: BinaryReader): Payload.ImportSection {
        val count = reader.readVarUInt32().toInt()
        val imports = mutableListOf<Import>()
        
        repeat(count) {
            val module = reader.readString()
            val name = reader.readString()
            val kindValue = reader.readUByte()
            val kind = ExternalKind.fromValue(kindValue)
                ?: throw WasmParserException("Invalid external kind: $kindValue")
            
            val typeIndex = reader.readVarUInt32()
            imports.add(Import(module, name, kind, typeIndex))
        }
        
        return Payload.ImportSection(imports)
    }
    
    private fun parseFunctionSection(reader: BinaryReader): Payload.FunctionSection {
        val count = reader.readVarUInt32().toInt()
        val typeIndices = List(count) { reader.readVarUInt32() }
        return Payload.FunctionSection(typeIndices)
    }
    
    private fun parseTableSection(reader: BinaryReader): Payload.TableSection {
        val count = reader.readVarUInt32().toInt()
        val tables = List(count) { reader.readTableType() }
        return Payload.TableSection(tables)
    }
    
    private fun parseMemorySection(reader: BinaryReader): Payload.MemorySection {
        val count = reader.readVarUInt32().toInt()
        val memories = List(count) { reader.readMemoryType() }
        return Payload.MemorySection(memories)
    }
    
    private fun parseGlobalSection(reader: BinaryReader): Payload.GlobalSection {
        val count = reader.readVarUInt32().toInt()
        val globals = mutableListOf<GlobalEntry>()
        
        repeat(count) {
            val type = reader.readGlobalType()
            val initExpr = readInitExpr(reader)
            globals.add(GlobalEntry(type, initExpr))
        }
        
        return Payload.GlobalSection(globals)
    }
    
    private fun parseExportSection(reader: BinaryReader): Payload.ExportSection {
        val count = reader.readVarUInt32().toInt()
        val exports = mutableListOf<Export>()
        
        repeat(count) {
            val name = reader.readString()
            val kindValue = reader.readUByte()
            val kind = ExternalKind.fromValue(kindValue)
                ?: throw WasmParserException("Invalid external kind: $kindValue")
            val index = reader.readVarUInt32()
            exports.add(Export(name, kind, index))
        }
        
        return Payload.ExportSection(exports)
    }
    
    private fun parseStartSection(reader: BinaryReader): Payload.StartSection {
        val functionIndex = reader.readVarUInt32()
        return Payload.StartSection(functionIndex)
    }
    
    private fun parseElementSection(reader: BinaryReader): Payload.ElementSection {
        val count = reader.readVarUInt32().toInt()
        val elements = mutableListOf<ElementSegment>()
        
        repeat(count) {
            val tableIndex = reader.readVarUInt32()
            val offset = readInitExpr(reader)
            val numElements = reader.readVarUInt32().toInt()
            val elementIndices = List(numElements) { reader.readVarUInt32() }
            elements.add(ElementSegment(tableIndex, offset, elementIndices))
        }
        
        return Payload.ElementSection(elements)
    }
    
    private fun parseCodeSection(reader: BinaryReader): Payload.CodeSection {
        val count = reader.readVarUInt32().toInt()
        val functions = mutableListOf<FunctionBody>()
        
        repeat(count) {
            val bodySize = reader.readVarUInt32().toInt()
            val bodyStart = reader.getPosition()
            
            val localCount = reader.readVarUInt32().toInt()
            val locals = List(localCount) {
                val count = reader.readVarUInt32()
                val type = reader.readValType()
                LocalEntry(count, type)
            }
            
            val codeSize = bodySize - (reader.getPosition() - bodyStart)
            val code = reader.readBytes(codeSize)
            
            functions.add(FunctionBody(locals, code))
        }
        
        return Payload.CodeSection(functions)
    }
    
    private fun parseDataSection(reader: BinaryReader): Payload.DataSection {
        val count = reader.readVarUInt32().toInt()
        val dataSegments = mutableListOf<DataSegment>()
        
        repeat(count) {
            val memoryIndex = reader.readVarUInt32()
            val offset = readInitExpr(reader)
            val dataSize = reader.readVarUInt32().toInt()
            val data = reader.readBytes(dataSize)
            dataSegments.add(DataSegment(memoryIndex, offset, data))
        }
        
        return Payload.DataSection(dataSegments)
    }
    
    private fun parseDataCountSection(reader: BinaryReader): Payload.DataCountSection {
        val count = reader.readVarUInt32()
        return Payload.DataCountSection(count)
    }
    
    private fun readInitExpr(reader: BinaryReader): List<Operator> {
        val operators = mutableListOf<Operator>()
        
        while (true) {
            val opcode = reader.readUByte()
            
            when (opcode) {
                0x0B -> { // end
                    operators.add(Operator.End)
                    break
                }
                0x41 -> { // i32.const
                    operators.add(Operator.I32Const(reader.readVarInt32()))
                }
                0x42 -> { // i64.const
                    operators.add(Operator.I64Const(reader.readVarInt64()))
                }
                0x43 -> { // f32.const
                    operators.add(Operator.F32Const(reader.readFloat32()))
                }
                0x44 -> { // f64.const
                    operators.add(Operator.F64Const(reader.readFloat64()))
                }
                0x23 -> { // global.get
                    operators.add(Operator.GlobalGet(reader.readVarUInt32()))
                }
                else -> throw WasmParserException("Invalid opcode in init expression: 0x${opcode.toString(16)}")
            }
        }
        
        return operators
    }
}
