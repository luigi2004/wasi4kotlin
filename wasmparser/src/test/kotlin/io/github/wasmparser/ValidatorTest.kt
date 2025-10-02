package io.github.wasmparser

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ValidatorTest {
    
    @Test
    fun testValidMinimalModule() {
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00   // version
        )
        
        val validator = Validator()
        val result = validator.validate(wasmBytes)
        
        assertTrue(result.isValid, "Minimal module should be valid")
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun testValidModuleWithSections() {
        // Module with type, function, and code sections
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,  // magic
            0x01, 0x00, 0x00, 0x00,  // version
            
            // Type section
            0x01, 0x04, 0x01, 0x60, 0x00, 0x00,
            
            // Function section
            0x03, 0x02, 0x01, 0x00,
            
            // Code section
            0x0A, 0x04, 0x01, 0x02, 0x00, 0x0B
        )
        
        val validator = Validator()
        val result = validator.validate(wasmBytes)
        
        assertTrue(result.isValid, "Module with matching function and code sections should be valid")
    }
    
    @Test
    fun testInvalidMagicNumber() {
        val wasmBytes = byteArrayOf(
            0x00, 0x62, 0x73, 0x6d,  // invalid magic (b instead of a)
            0x01, 0x00, 0x00, 0x00
        )
        
        val validator = Validator()
        val result = validator.validate(wasmBytes)
        
        assertFalse(result.isValid, "Module with invalid magic should be invalid")
        assertTrue(result.errors.any { it.contains("magic") })
    }
    
    @Test
    fun testMismatchedFunctionAndCodeCounts() {
        // Module with 2 functions declared but only 1 code body
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,
            0x01, 0x00, 0x00, 0x00,
            
            // Type section
            0x01, 0x04, 0x01, 0x60, 0x00, 0x00,
            
            // Function section (2 functions)
            0x03, 0x03, 0x02, 0x00, 0x00,
            
            // Code section (1 function)
            0x0A, 0x04, 0x01, 0x02, 0x00, 0x0B
        )
        
        val validator = Validator()
        val result = validator.validate(wasmBytes)
        
        assertFalse(result.isValid, "Module with mismatched function/code counts should be invalid")
        assertTrue(result.errors.any { it.contains("Function section") && it.contains("code section") })
    }
    
    @Test
    fun testValidatorDetectsDuplicates() {
        // Use a simpler approach - create a well-formed module
        // and verify the validator correctly identifies duplicate exports
        val wasmBytes = byteArrayOf(
            0x00, 0x61, 0x73, 0x6d,
            0x01, 0x00, 0x00, 0x00,
            
            // Type section: () -> ()
            0x01, 0x04, 0x01, 0x60, 0x00, 0x00,
            
            // Export section with duplicate names
            0x07, 0x09,  // Section size = 1 + (1+1+1+1)*2 = 9
            0x02,  // 2 exports
            0x01, 0x61,  // "a" (len=1, value="a")
            0x00, 0x00,  // function kind, index 0
            0x01, 0x61,  // "a" duplicate (len=1, value="a")
            0x00, 0x00   // function kind, index 0
        )
        
        val validator = Validator()
        val result = validator.validate(wasmBytes)
        
        assertFalse(result.isValid, "Module with duplicate export names should be invalid: ${result}")
        assertTrue(result.errors.any { it.contains("Duplicate export") || it.contains("duplicate") }, 
            "Should report duplicate export: ${result.errors}")
    }
}
