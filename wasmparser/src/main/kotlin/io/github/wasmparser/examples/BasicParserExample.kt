package io.github.wasmparser.examples

import io.github.wasmparser.*

/**
 * Example demonstrating basic usage of the wasmparser.
 * 
 * This example parses a simple WebAssembly module and prints
 * information about its sections.
 */
fun main() {
    // Create a simple WebAssembly module with:
    // - A type section with one function type: (i32, i32) -> i32
    // - A function section with one function
    // - An export section exporting the function as "add"
    val wasmBytes = createSimpleAddModule()
    
    println("Parsing WebAssembly module...")
    println("=" .repeat(60))
    
    val parser = Parser(wasmBytes)
    
    for (payload in parser.parse()) {
        when (payload) {
            is Payload.Version -> {
                println("✓ WebAssembly version: ${Payload.Version.WASM_VERSION}")
            }
            
            is Payload.TypeSection -> {
                println("\nType Section:")
                println("  Found ${payload.types.size} function type(s)")
                payload.types.forEachIndexed { index, funcType ->
                    val params = funcType.params.joinToString(", ")
                    val results = funcType.results.joinToString(", ")
                    println("  [$index] ($params) -> ($results)")
                }
            }
            
            is Payload.FunctionSection -> {
                println("\nFunction Section:")
                println("  Found ${payload.typeIndices.size} function(s)")
                payload.typeIndices.forEachIndexed { index, typeIndex ->
                    println("  [$index] uses type #$typeIndex")
                }
            }
            
            is Payload.ExportSection -> {
                println("\nExport Section:")
                println("  Found ${payload.exports.size} export(s)")
                payload.exports.forEach { export ->
                    println("  \"${export.name}\" -> ${export.kind} #${export.index}")
                }
            }
            
            is Payload.CodeSection -> {
                println("\nCode Section:")
                println("  Found ${payload.functions.size} function body/bodies")
                payload.functions.forEachIndexed { index, func ->
                    println("  [$index] ${func.locals.size} local group(s), ${func.code.size} bytes of code")
                }
            }
            
            is Payload.ImportSection -> {
                println("\nImport Section:")
                println("  Found ${payload.imports.size} import(s)")
                payload.imports.forEach { import ->
                    println("  ${import.module}.${import.name} (${import.kind})")
                }
            }
            
            is Payload.MemorySection -> {
                println("\nMemory Section:")
                println("  Found ${payload.memories.size} memory/memories")
                payload.memories.forEach { memory ->
                    val max = memory.limits.max?.let { "max=$it" } ?: "no max"
                    println("  min=${memory.limits.min}, $max")
                }
            }
            
            is Payload.TableSection -> {
                println("\nTable Section:")
                println("  Found ${payload.tables.size} table(s)")
                payload.tables.forEach { table ->
                    val max = table.limits.max?.let { "max=$it" } ?: "no max"
                    println("  element type=${table.elementType}, min=${table.limits.min}, $max")
                }
            }
            
            is Payload.GlobalSection -> {
                println("\nGlobal Section:")
                println("  Found ${payload.globals.size} global(s)")
                payload.globals.forEach { global ->
                    val mut = if (global.type.mutable) "mut" else "const"
                    println("  $mut ${global.type.contentType}")
                }
            }
            
            is Payload.CustomSection -> {
                println("\nCustom Section:")
                println("  Name: \"${payload.name}\"")
                println("  Data: ${payload.data.size} bytes")
            }
            
            is Payload.StartSection -> {
                println("\nStart Section:")
                println("  Start function: #${payload.functionIndex}")
            }
            
            is Payload.ElementSection -> {
                println("\nElement Section:")
                println("  Found ${payload.elements.size} element segment(s)")
            }
            
            is Payload.DataSection -> {
                println("\nData Section:")
                println("  Found ${payload.data.size} data segment(s)")
            }
            
            is Payload.DataCountSection -> {
                println("\nData Count Section:")
                println("  Count: ${payload.count}")
            }
            
            is Payload.End -> {
                println("\n" + "=".repeat(60))
                println("✓ Finished parsing module")
            }
        }
    }
}

/**
 * Creates a simple WebAssembly module that exports an "add" function.
 * The module has the signature: (i32, i32) -> i32
 */
fun createSimpleAddModule(): ByteArray {
    return byteArrayOf(
        // Magic number
        0x00, 0x61, 0x73, 0x6d,
        // Version
        0x01, 0x00, 0x00, 0x00,
        
        // Type section
        0x01,           // Section ID: Type
        0x07,           // Section size
        0x01,           // 1 type
        0x60,           // func type
        0x02,           // 2 params
        0x7F,           // i32
        0x7F,           // i32
        0x01,           // 1 result
        0x7F,           // i32
        
        // Function section
        0x03,           // Section ID: Function
        0x02,           // Section size
        0x01,           // 1 function
        0x00,           // type index 0
        
        // Export section
        0x07,           // Section ID: Export
        0x07,           // Section size
        0x01,           // 1 export
        0x03,           // name length
        0x61, 0x64, 0x64,  // "add"
        0x00,           // export kind: function
        0x00,           // function index 0
        
        // Code section
        0x0A,           // Section ID: Code
        0x09,           // Section size
        0x01,           // 1 function body
        0x07,           // body size
        0x00,           // 0 local declarations
        0x20, 0x00,     // local.get 0
        0x20, 0x01,     // local.get 1
        0x6A,           // i32.add
        0x0B            // end
    )
}
