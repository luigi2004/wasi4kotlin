package io.github.wasmparser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    
    @Test
    fun testMinimalModule() {
        // A minimal valid WebAssembly module (magic + version only)
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00   // version
        )
        
        val parser = Parser(wasmBytes)
        val payloads = parser.parse().toList()
        
        assertEquals(2, payloads.size)
        assertTrue(payloads[0] is Payload.Version)
        assertTrue(payloads[1] is Payload.End)
    }
    
    @Test
    fun testModuleWithTypeSection() {
        // Module with a type section containing one function type: () -> ()
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00,  // version
            0x01,                     // Type section ID
            0x04,                     // Section size
            0x01,                     // 1 type
            0x60,                     // func type
            0x00,                     // 0 params
            0x00                      // 0 results
        )
        
        val parser = Parser(wasmBytes)
        val payloads = parser.parse().toList()
        
        assertEquals(3, payloads.size)
        assertTrue(payloads[0] is Payload.Version)
        assertTrue(payloads[1] is Payload.TypeSection)
        assertTrue(payloads[2] is Payload.End)
        
        val typeSection = payloads[1] as Payload.TypeSection
        assertEquals(1, typeSection.types.size)
        assertEquals(0, typeSection.types[0].params.size)
        assertEquals(0, typeSection.types[0].results.size)
    }
    
    @Test
    fun testModuleWithFunctionType() {
        // Module with a type section containing one function type: (i32, i32) -> i32
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00,  // version
            0x01,                     // Type section ID
            0x07,                     // Section size
            0x01,                     // 1 type
            0x60,                     // func type
            0x02,                     // 2 params
            0x7F,                     // i32
            0x7F,                     // i32
            0x01,                     // 1 result
            0x7F                      // i32
        )
        
        val parser = Parser(wasmBytes)
        val payloads = parser.parse().toList()
        
        val typeSection = payloads[1] as Payload.TypeSection
        assertEquals(1, typeSection.types.size)
        
        val funcType = typeSection.types[0]
        assertEquals(2, funcType.params.size)
        assertEquals(ValType.I32, funcType.params[0])
        assertEquals(ValType.I32, funcType.params[1])
        assertEquals(1, funcType.results.size)
        assertEquals(ValType.I32, funcType.results[0])
    }
    
    @Test
    fun testModuleWithExportSection() {
        // Module with an export section
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00,  // version
            0x07,                     // Export section ID
            0x07,                     // Section size
            0x01,                     // 1 export
            0x03,                     // name length
            0x61, 0x64, 0x64,        // "add"
            0x00,                     // function kind
            0x00                      // function index 0
        )
        
        val parser = Parser(wasmBytes)
        val payloads = parser.parse().toList()
        
        assertTrue(payloads[1] is Payload.ExportSection)
        
        val exportSection = payloads[1] as Payload.ExportSection
        assertEquals(1, exportSection.exports.size)
        assertEquals("add", exportSection.exports[0].name)
        assertEquals(ExternalKind.Function, exportSection.exports[0].kind)
        assertEquals(0u, exportSection.exports[0].index)
    }
}
