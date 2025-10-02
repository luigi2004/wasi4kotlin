package io.github.wasmparser.examples

import io.github.wasmparser.*

/**
 * Example demonstrating the use of the Validator to check WebAssembly modules.
 */
fun main() {
    println("WebAssembly Module Validation Examples")
    println("=" .repeat(60))
    
    // Example 1: Valid minimal module
    println("\n1. Validating a minimal module...")
    val minimalModule = byteArrayOf(
        0x00, 0x61, 0x73, 0x6d,  // magic
        0x01, 0x00, 0x00, 0x00   // version
    )
    validateAndPrint(minimalModule, "Minimal module")
    
    // Example 2: Valid module with exports
    println("\n2. Validating a module with exports...")
    val moduleWithExports = createSimpleAddModule()
    validateAndPrint(moduleWithExports, "Module with exports")
    
    // Example 3: Invalid module (bad magic number)
    println("\n3. Validating an invalid module (bad magic)...")
    val badMagic = byteArrayOf(
        0x00, 0x62, 0x73, 0x6d,  // wrong magic
        0x01, 0x00, 0x00, 0x00
    )
    validateAndPrint(badMagic, "Module with bad magic")
    
    // Example 4: Module with mismatched function/code sections
    println("\n4. Validating a module with mismatched sections...")
    val mismatchedModule = byteArrayOf(
        0x00, 0x61, 0x73, 0x6d,
        0x01, 0x00, 0x00, 0x00,
        // Type section
        0x01, 0x04, 0x01, 0x60, 0x00, 0x00,
        // Function section (2 functions)
        0x03, 0x03, 0x02, 0x00, 0x00,
        // Code section (only 1 function)
        0x0A, 0x04, 0x01, 0x02, 0x00, 0x0B
    )
    validateAndPrint(mismatchedModule, "Module with mismatched function/code")
    
    println("\n" + "=".repeat(60))
}

fun validateAndPrint(wasmBytes: ByteArray, description: String) {
    val validator = Validator()
    val result = validator.validate(wasmBytes)
    
    println("  Module: $description")
    if (result.isValid) {
        println("  ✓ Valid")
    } else {
        println("  ✗ Invalid")
        result.errors.forEach { error ->
            println("    - $error")
        }
    }
}
