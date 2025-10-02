package io.github.wasmparser

/**
 * Exception thrown when there's an error parsing WebAssembly binary.
 */
class WasmParserException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Binary reader for WebAssembly binary format.
 * 
 * This class provides methods to read various data types from a byte array
 * according to the WebAssembly binary encoding specification.
 */
class BinaryReader(private val data: ByteArray) {
    private var position: Int = 0

    /**
     * Returns the current position in the byte array.
     */
    fun getPosition(): Int = position

    /**
     * Returns true if there are more bytes to read.
     */
    fun hasMore(): Boolean = position < data.size

    /**
     * Returns the number of bytes remaining.
     */
    fun remaining(): Int = data.size - position

    /**
     * Reads a single byte.
     */
    fun readByte(): Byte {
        if (position >= data.size) {
            throw WasmParserException("Unexpected end of data")
        }
        return data[position++]
    }

    /**
     * Reads an unsigned byte.
     */
    fun readUByte(): Int {
        return readByte().toInt() and 0xFF
    }

    /**
     * Reads multiple bytes into a byte array.
     */
    fun readBytes(count: Int): ByteArray {
        if (position + count > data.size) {
            throw WasmParserException("Not enough data to read $count bytes")
        }
        val result = data.copyOfRange(position, position + count)
        position += count
        return result
    }

    /**
     * Reads an unsigned LEB128 encoded integer (max 32-bit).
     */
    fun readVarUInt32(): UInt {
        var result = 0u
        var shift = 0
        
        while (true) {
            if (shift > 28) {
                throw WasmParserException("VarUInt32 too large")
            }
            
            val byte = readUByte()
            result = result or ((byte and 0x7F).toUInt() shl shift)
            
            if ((byte and 0x80) == 0) {
                break
            }
            shift += 7
        }
        
        return result
    }

    /**
     * Reads a signed LEB128 encoded integer (max 32-bit).
     */
    fun readVarInt32(): Int {
        var result = 0
        var shift = 0
        var byte: Int
        
        do {
            if (shift > 28) {
                throw WasmParserException("VarInt32 too large")
            }
            
            byte = readUByte()
            result = result or ((byte and 0x7F) shl shift)
            shift += 7
        } while ((byte and 0x80) != 0)
        
        // Sign extend if needed
        if (shift < 32 && (byte and 0x40) != 0) {
            result = result or ((-1) shl shift)
        }
        
        return result
    }

    /**
     * Reads an unsigned LEB128 encoded integer (max 64-bit).
     */
    fun readVarUInt64(): ULong {
        var result = 0uL
        var shift = 0
        
        while (true) {
            if (shift > 63) {
                throw WasmParserException("VarUInt64 too large")
            }
            
            val byte = readUByte()
            result = result or ((byte and 0x7F).toULong() shl shift)
            
            if ((byte and 0x80) == 0) {
                break
            }
            shift += 7
        }
        
        return result
    }

    /**
     * Reads a signed LEB128 encoded integer (max 64-bit).
     */
    fun readVarInt64(): Long {
        var result = 0L
        var shift = 0
        var byte: Int
        
        do {
            if (shift > 63) {
                throw WasmParserException("VarInt64 too large")
            }
            
            byte = readUByte()
            result = result or ((byte and 0x7F).toLong() shl shift)
            shift += 7
        } while ((byte and 0x80) != 0)
        
        // Sign extend if needed
        if (shift < 64 && (byte and 0x40) != 0) {
            result = result or ((-1L) shl shift)
        }
        
        return result
    }

    /**
     * Reads a 32-bit float.
     */
    fun readFloat32(): Float {
        val bytes = readBytes(4)
        val bits = ((bytes[3].toInt() and 0xFF) shl 24) or
                   ((bytes[2].toInt() and 0xFF) shl 16) or
                   ((bytes[1].toInt() and 0xFF) shl 8) or
                   (bytes[0].toInt() and 0xFF)
        return Float.fromBits(bits)
    }

    /**
     * Reads a 64-bit float.
     */
    fun readFloat64(): Double {
        val bytes = readBytes(8)
        val bits = ((bytes[7].toLong() and 0xFF) shl 56) or
                   ((bytes[6].toLong() and 0xFF) shl 48) or
                   ((bytes[5].toLong() and 0xFF) shl 40) or
                   ((bytes[4].toLong() and 0xFF) shl 32) or
                   ((bytes[3].toLong() and 0xFF) shl 24) or
                   ((bytes[2].toLong() and 0xFF) shl 16) or
                   ((bytes[1].toLong() and 0xFF) shl 8) or
                   (bytes[0].toLong() and 0xFF)
        return Double.fromBits(bits)
    }

    /**
     * Reads a UTF-8 encoded string.
     */
    fun readString(): String {
        val length = readVarUInt32().toInt()
        val bytes = readBytes(length)
        return bytes.decodeToString()
    }

    /**
     * Reads a value type.
     */
    fun readValType(): ValType {
        val value = readByte().toInt()
        return ValType.fromValue(value) 
            ?: throw WasmParserException("Invalid value type: 0x${value.toString(16)}")
    }

    /**
     * Reads limits (min and optional max).
     */
    fun readLimits(): Limits {
        val flags = readVarUInt32()
        val min = readVarUInt32()
        val max = if (flags and 1u != 0u) readVarUInt32() else null
        return Limits(min, max)
    }

    /**
     * Reads a function type.
     */
    fun readFuncType(): FuncType {
        val tag = readByte()
        if (tag != 0x60.toByte()) {
            throw WasmParserException("Expected function type tag 0x60, got 0x${tag.toString(16)}")
        }
        
        val paramCount = readVarUInt32().toInt()
        val params = List(paramCount) { readValType() }
        
        val resultCount = readVarUInt32().toInt()
        val results = List(resultCount) { readValType() }
        
        return FuncType(params, results)
    }

    /**
     * Reads a memory type.
     */
    fun readMemoryType(): MemoryType {
        return MemoryType(readLimits())
    }

    /**
     * Reads a table type.
     */
    fun readTableType(): TableType {
        val elementType = readValType()
        val limits = readLimits()
        return TableType(elementType, limits)
    }

    /**
     * Reads a global type.
     */
    fun readGlobalType(): GlobalType {
        val contentType = readValType()
        val mutability = readVarUInt32()
        return GlobalType(contentType, mutability != 0u)
    }

    /**
     * Reads a block type.
     */
    fun readBlockType(): BlockType {
        val value = readByte().toInt()
        return when (value) {
            0x40 -> BlockType.Empty
            else -> {
                if (value < 0) {
                    val valType = ValType.fromValue(value)
                    if (valType != null) {
                        BlockType.Value(valType)
                    } else {
                        throw WasmParserException("Invalid block type: 0x${value.toString(16)}")
                    }
                } else {
                    // Type index - we need to back up and read as var uint
                    position--
                    BlockType.TypeIndex(readVarUInt32())
                }
            }
        }
    }

    /**
     * Reads a memory argument.
     */
    fun readMemArg(): MemArg {
        val align = readVarUInt32()
        val offset = readVarUInt32()
        return MemArg(align, offset)
    }
}
