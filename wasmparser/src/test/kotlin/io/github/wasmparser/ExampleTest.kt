package io.github.wasmparser

import io.github.wasmparser.examples.createSimpleAddModule
import io.github.wasmparser.examples.main as exampleMain
import kotlin.test.Test

class ExampleTest {
    @Test
    fun testExample() {
        // Just ensure the example runs without errors
        exampleMain()
    }
    
    @Test
    fun testSimpleAddModule() {
        val wasmBytes = createSimpleAddModule()
        val parser = Parser(wasmBytes)
        val payloads = parser.parse().toList()
        
        // Should have at least Version, Type, Function, Export, Code, and End
        assert(payloads.size >= 6)
    }
}
