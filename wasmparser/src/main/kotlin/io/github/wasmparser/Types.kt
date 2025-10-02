package io.github.wasmparser

/**
 * Types of sections in a WebAssembly module.
 */
enum class SectionCode(val value: Int) {
    Custom(0),
    Type(1),
    Import(2),
    Function(3),
    Table(4),
    Memory(5),
    Global(6),
    Export(7),
    Start(8),
    Element(9),
    Code(10),
    Data(11),
    DataCount(12);

    companion object {
        fun fromValue(value: Int): SectionCode? {
            return values().find { it.value == value }
        }
    }
}

/**
 * Value types in WebAssembly.
 */
enum class ValType(val value: Int) {
    I32(0x7F),
    I64(0x7E),
    F32(0x7D),
    F64(0x7C),
    V128(0x7B),
    FuncRef(0x70),
    ExternRef(0x6F);

    companion object {
        fun fromValue(value: Int): ValType? {
            return values().find { it.value == value }
        }
    }
}

/**
 * External kind for imports and exports.
 */
enum class ExternalKind(val value: Int) {
    Function(0),
    Table(1),
    Memory(2),
    Global(3);

    companion object {
        fun fromValue(value: Int): ExternalKind? {
            return values().find { it.value == value }
        }
    }
}

/**
 * Block type for control flow instructions.
 */
sealed class BlockType {
    object Empty : BlockType()
    data class Value(val type: ValType) : BlockType()
    data class TypeIndex(val index: UInt) : BlockType()
}

/**
 * Function type signature.
 */
data class FuncType(
    val params: List<ValType>,
    val results: List<ValType>
)

/**
 * Memory limits.
 */
data class Limits(
    val min: UInt,
    val max: UInt? = null
)

/**
 * Table type.
 */
data class TableType(
    val elementType: ValType,
    val limits: Limits
)

/**
 * Memory type.
 */
data class MemoryType(
    val limits: Limits
)

/**
 * Global type.
 */
data class GlobalType(
    val contentType: ValType,
    val mutable: Boolean
)

/**
 * Import descriptor.
 */
data class Import(
    val module: String,
    val name: String,
    val kind: ExternalKind,
    val typeIndex: UInt
)

/**
 * Export descriptor.
 */
data class Export(
    val name: String,
    val kind: ExternalKind,
    val index: UInt
)

/**
 * Memory argument for memory instructions.
 */
data class MemArg(
    val align: UInt,
    val offset: UInt
)

/**
 * Branch table for br_table instruction.
 */
data class BrTable(
    val targets: List<UInt>,
    val default: UInt
)
