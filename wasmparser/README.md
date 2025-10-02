# wasmparser

A Kotlin implementation of a WebAssembly binary parser, inspired by the Rust [wasmparser](https://github.com/bytecodealliance/wasm-tools/tree/main/crates/wasmparser) crate from the Bytecode Alliance.

## Overview

This library provides an event-driven parser for WebAssembly binary files. It reports events as they happen and only stores parsing information for a brief period of time, making it fast and memory-efficient.

## Features

- **Event-driven parsing**: Process WebAssembly modules section by section
- **Low memory footprint**: Only stores information temporarily during parsing
- **Full section support**: Parses all standard WebAssembly sections (Type, Import, Function, Table, Memory, Global, Export, Start, Element, Code, Data, DataCount, Custom)
- **Type-safe**: Uses sealed classes and data classes for type safety
- **Idiomatic Kotlin**: Written in idiomatic Kotlin with immutable data structures

## Usage

### Basic Example

```kotlin
import io.github.wasmparser.*
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    // Read a WebAssembly binary file
    val wasmBytes = Files.readAllBytes(Paths.get("module.wasm"))
    
    // Create a parser
    val parser = Parser(wasmBytes)
    
    // Parse and process sections
    for (payload in parser.parse()) {
        when (payload) {
            is Payload.Version -> {
                println("WebAssembly version: ${Payload.Version.WASM_VERSION}")
            }
            is Payload.TypeSection -> {
                println("Found ${payload.types.size} function types")
                payload.types.forEachIndexed { index, funcType ->
                    println("  Type $index: ${funcType.params} -> ${funcType.results}")
                }
            }
            is Payload.FunctionSection -> {
                println("Found ${payload.typeIndices.size} functions")
            }
            is Payload.ExportSection -> {
                println("Exports:")
                payload.exports.forEach { export ->
                    println("  ${export.name}: ${export.kind} #${export.index}")
                }
            }
            is Payload.ImportSection -> {
                println("Imports:")
                payload.imports.forEach { import ->
                    println("  ${import.module}.${import.name}: ${import.kind}")
                }
            }
            is Payload.CodeSection -> {
                println("Found ${payload.functions.size} function bodies")
            }
            is Payload.End -> {
                println("Finished parsing module")
            }
            else -> {
                // Handle other sections as needed
            }
        }
    }
}
```

### Analyzing Module Structure

```kotlin
fun analyzeModule(wasmBytes: ByteArray) {
    val parser = Parser(wasmBytes)
    
    var functionCount = 0
    var importCount = 0
    var exportCount = 0
    var memoryCount = 0
    var tableCount = 0
    
    for (payload in parser.parse()) {
        when (payload) {
            is Payload.FunctionSection -> functionCount = payload.typeIndices.size
            is Payload.ImportSection -> importCount = payload.imports.size
            is Payload.ExportSection -> exportCount = payload.exports.size
            is Payload.MemorySection -> memoryCount = payload.memories.size
            is Payload.TableSection -> tableCount = payload.tables.size
            else -> {}
        }
    }
    
    println("""
        Module Statistics:
        - Functions: $functionCount
        - Imports: $importCount
        - Exports: $exportCount
        - Memories: $memoryCount
        - Tables: $tableCount
    """.trimIndent())
}
```

## API Overview

### Core Classes

- **`Parser`**: Main entry point for parsing WebAssembly binaries
  - `fun parse(): Sequence<Payload>` - Parses the module and returns a sequence of payloads

- **`Payload`**: Sealed class representing different sections and events
  - `Payload.Version` - Module version
  - `Payload.TypeSection` - Function type declarations
  - `Payload.ImportSection` - Imported functions, tables, memories, and globals
  - `Payload.FunctionSection` - Function type indices
  - `Payload.TableSection` - Table declarations
  - `Payload.MemorySection` - Memory declarations
  - `Payload.GlobalSection` - Global variable declarations
  - `Payload.ExportSection` - Exported functions, tables, memories, and globals
  - `Payload.StartSection` - Start function index
  - `Payload.ElementSection` - Element segments for table initialization
  - `Payload.CodeSection` - Function bodies
  - `Payload.DataSection` - Data segments for memory initialization
  - `Payload.DataCountSection` - Number of data segments
  - `Payload.CustomSection` - Custom sections
  - `Payload.End` - End of module

- **`BinaryReader`**: Low-level reader for WebAssembly binary encoding
  - Supports LEB128 encoding (variable-length integers)
  - Reads primitive types (i32, i64, f32, f64)
  - Reads WebAssembly-specific types (ValType, Limits, etc.)

### Type Definitions

- **`ValType`**: WebAssembly value types (I32, I64, F32, F64, V128, FuncRef, ExternRef)
- **`FuncType`**: Function signatures with parameters and results
- **`Limits`**: Memory/table limits (min and optional max)
- **`ExternalKind`**: Import/export kinds (Function, Table, Memory, Global)
- **`BlockType`**: Control flow block types

## Comparison with Rust wasmparser

This Kotlin implementation provides similar functionality to the Rust wasmparser crate:

- **Event-driven parsing**: Like the Rust version, this parser is event-driven and doesn't build a full in-memory representation
- **Low overhead**: Minimal memory usage by processing sections as they're encountered
- **Standard compliance**: Supports the WebAssembly 1.0 specification

**Differences:**
- Written in idiomatic Kotlin rather than Rust
- Uses Kotlin's sequence API instead of Rust's iterator trait
- Sealed classes for type-safe payload handling
- Some advanced features from the Rust version may not be implemented yet

## Building and Testing

Build the module:
```bash
./gradlew :wasmparser:build
```

Run tests:
```bash
./gradlew :wasmparser:test
```

## License

This project is part of the wasi4kotlin repository. See the main repository for license information.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## References

- [WebAssembly Specification](https://webassembly.github.io/spec/)
- [Rust wasmparser crate](https://github.com/bytecodealliance/wasm-tools/tree/main/crates/wasmparser)
- [Bytecode Alliance](https://bytecodealliance.org/)
