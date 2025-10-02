package com.example

import io.github.wasmparser.*

/**
 * Example showing how to analyze and inspect WebAssembly modules before loading them.
 * This demonstrates using wasmparser to understand module structure.
 */
fun main() {
    println("WebAssembly Module Analysis Tool")
    println("=" .repeat(60))
    
    // Analyze the simple add module
    val wasmBytes = createSimpleWasmModule()
    
    println("\nAnalyzing module...")
    analyzeModule(wasmBytes)
    
    println("\n" + "=".repeat(60))
}

/**
 * Analyzes a WebAssembly module and prints detailed information about its structure.
 */
fun analyzeModule(wasmBytes: ByteArray) {
    // First, validate the module
    val validator = Validator()
    val validationResult = validator.validate(wasmBytes)
    
    println("\n1. Validation:")
    if (validationResult.isValid) {
        println("   ✓ Module is well-formed")
    } else {
        println("   ✗ Module has errors:")
        validationResult.errors.forEach { error ->
            println("      - $error")
        }
        return
    }
    
    // Parse and analyze the module
    val parser = Parser(wasmBytes)
    var functionCount = 0
    var importCount = 0
    var exportCount = 0
    var memoryCount = 0
    var tableCount = 0
    var globalCount = 0
    val exportedFunctions = mutableListOf<String>()
    val importedFunctions = mutableListOf<String>()
    var typeCount = 0
    val functionTypes = mutableListOf<FuncType>()
    
    for (payload in parser.parse()) {
        when (payload) {
            is Payload.TypeSection -> {
                typeCount = payload.types.size
                functionTypes.addAll(payload.types)
            }
            is Payload.ImportSection -> {
                importCount = payload.imports.size
                payload.imports.forEach { import ->
                    if (import.kind == ExternalKind.Function) {
                        importedFunctions.add("${import.module}.${import.name}")
                    }
                }
            }
            is Payload.FunctionSection -> {
                functionCount = payload.typeIndices.size
            }
            is Payload.TableSection -> {
                tableCount = payload.tables.size
            }
            is Payload.MemorySection -> {
                memoryCount = payload.memories.size
            }
            is Payload.GlobalSection -> {
                globalCount = payload.globals.size
            }
            is Payload.ExportSection -> {
                exportCount = payload.exports.size
                payload.exports.forEach { export ->
                    if (export.kind == ExternalKind.Function) {
                        exportedFunctions.add(export.name)
                    }
                }
            }
            else -> {}
        }
    }
    
    println("\n2. Module Structure:")
    println("   - Function types: $typeCount")
    println("   - Functions: $functionCount")
    println("   - Imports: $importCount")
    println("   - Exports: $exportCount")
    println("   - Tables: $tableCount")
    println("   - Memories: $memoryCount")
    println("   - Globals: $globalCount")
    
    if (functionTypes.isNotEmpty()) {
        println("\n3. Function Types:")
        functionTypes.forEachIndexed { index, funcType ->
            val params = funcType.params.joinToString(", ")
            val results = funcType.results.joinToString(", ")
            println("   [$index] ($params) -> ($results)")
        }
    }
    
    if (importedFunctions.isNotEmpty()) {
        println("\n4. Imported Functions:")
        importedFunctions.forEach { name ->
            println("   - $name")
        }
    }
    
    if (exportedFunctions.isNotEmpty()) {
        println("\n5. Exported Functions:")
        exportedFunctions.forEach { name ->
            println("   - $name")
        }
    }
    
    println("\n6. Analysis Summary:")
    println("   - This module defines $functionCount function(s)")
    println("   - Exports ${exportedFunctions.size} function(s)")
    println("   - Imports ${importedFunctions.size} function(s)")
    
    if (memoryCount > 0) {
        println("   - Uses memory")
    }
    if (tableCount > 0) {
        println("   - Uses tables")
    }
}

/**
 * Creates a simple WebAssembly module with an add function.
 */
fun createSimpleWasmModule(): ByteArray {
    return byteArrayOf(
        // Magic number
        0x00, 0x61, 0x73, 0x6d,
        // Version
        0x01, 0x00, 0x00, 0x00,
        
        // Type section
        0x01, 0x07, 
        0x01,           // 1 type
        0x60,           // func type
        0x02,           // 2 params
        0x7F, 0x7F,     // i32, i32
        0x01,           // 1 result
        0x7F,           // i32
        
        // Function section
        0x03, 0x02,
        0x01,           // 1 function
        0x00,           // type index 0
        
        // Memory section
        0x05, 0x03,
        0x01,           // 1 memory
        0x00, 0x01,     // flags=0, min=1
        
        // Export section
        0x07, 0x10,     // Section size: 1 + (1+3+1+1) + (1+6+1+1) = 1 + 6 + 9 = 16
        0x02,           // 2 exports
        0x03, 0x61, 0x64, 0x64,  // "add" (len=3)
        0x00, 0x00,     // function kind, index 0
        0x06, 0x6D, 0x65, 0x6D, 0x6F, 0x72, 0x79,  // "memory" (len=6)
        0x02, 0x00,     // memory kind, index 0
        
        // Code section
        0x0A, 0x09,
        0x01,           // 1 function body
        0x07,           // body size
        0x00,           // 0 locals
        0x20, 0x00,     // local.get 0
        0x20, 0x01,     // local.get 1
        0x6A,           // i32.add
        0x0B            // end
    )
}
