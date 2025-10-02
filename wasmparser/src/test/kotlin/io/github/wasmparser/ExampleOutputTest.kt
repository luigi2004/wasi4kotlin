package io.github.wasmparser

import io.github.wasmparser.examples.createSimpleAddModule
import kotlin.test.Test

class ExampleOutputTest {
    @Test
    fun testParserOutput() {
        val wasmBytes = createSimpleAddModule()
        val parser = Parser(wasmBytes)
        
        println("\nTesting wasmparser output:")
        println("=" .repeat(60))
        
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
                }
                is Payload.End -> {
                    println("\n" + "=".repeat(60))
                    println("✓ Finished parsing module")
                }
                else -> {}
            }
        }
    }
}
