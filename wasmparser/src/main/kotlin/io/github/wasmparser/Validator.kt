package io.github.wasmparser

/**
 * Validator for WebAssembly modules.
 * 
 * This class provides basic validation of WebAssembly modules to ensure
 * they are well-formed and follow the WebAssembly specification.
 */
class Validator {
    private val errors = mutableListOf<String>()
    
    /**
     * Validates a WebAssembly module.
     * 
     * @param data The WebAssembly binary data to validate
     * @return A ValidationResult containing any errors found
     */
    fun validate(data: ByteArray): ValidationResult {
        errors.clear()
        
        try {
            val parser = Parser(data)
            val payloads = parser.parse().toList()
            
            var hasTypeSection = false
            var hasFunctionSection = false
            var hasCodeSection = false
            var functionCount = 0
            var codeCount = 0
            
            for (payload in payloads) {
                when (payload) {
                    is Payload.Version -> {
                        // Version is always valid if we got here
                    }
                    is Payload.TypeSection -> {
                        if (hasTypeSection) {
                            errors.add("Duplicate type section")
                        }
                        hasTypeSection = true
                        validateTypeSection(payload)
                    }
                    is Payload.FunctionSection -> {
                        if (hasFunctionSection) {
                            errors.add("Duplicate function section")
                        }
                        hasFunctionSection = true
                        functionCount = payload.typeIndices.size
                    }
                    is Payload.CodeSection -> {
                        if (hasCodeSection) {
                            errors.add("Duplicate code section")
                        }
                        hasCodeSection = true
                        codeCount = payload.functions.size
                    }
                    is Payload.ExportSection -> {
                        validateExportSection(payload)
                    }
                    is Payload.MemorySection -> {
                        validateMemorySection(payload)
                    }
                    is Payload.TableSection -> {
                        validateTableSection(payload)
                    }
                    else -> {
                        // Other sections are valid by default if parsed successfully
                    }
                }
            }
            
            // Validate function and code sections match
            if (hasFunctionSection && hasCodeSection) {
                if (functionCount != codeCount) {
                    errors.add(
                        "Function section declares $functionCount functions but code section has $codeCount bodies"
                    )
                }
            } else if (hasFunctionSection && !hasCodeSection && functionCount > 0) {
                errors.add("Function section declares functions but no code section present")
            } else if (!hasFunctionSection && hasCodeSection && codeCount > 0) {
                errors.add("Code section present but no function section")
            }
            
        } catch (e: WasmParserException) {
            errors.add("Parse error: ${e.message}")
        }
        
        return ValidationResult(errors.isEmpty(), errors.toList())
    }
    
    private fun validateTypeSection(section: Payload.TypeSection) {
        section.types.forEachIndexed { index, funcType ->
            // Check for reasonable parameter and result counts
            if (funcType.params.size > 1000) {
                errors.add("Type $index has too many parameters: ${funcType.params.size}")
            }
            if (funcType.results.size > 1000) {
                errors.add("Type $index has too many results: ${funcType.results.size}")
            }
        }
    }
    
    private fun validateExportSection(section: Payload.ExportSection) {
        val names = mutableSetOf<String>()
        section.exports.forEach { export ->
            if (export.name in names) {
                errors.add("Duplicate export name: ${export.name}")
            }
            names.add(export.name)
        }
    }
    
    private fun validateMemorySection(section: Payload.MemorySection) {
        section.memories.forEach { memory ->
            validateLimits(memory.limits, "memory")
        }
    }
    
    private fun validateTableSection(section: Payload.TableSection) {
        section.tables.forEach { table ->
            validateLimits(table.limits, "table")
        }
    }
    
    private fun validateLimits(limits: Limits, context: String) {
        if (limits.max != null && limits.max < limits.min) {
            errors.add("$context: max limit (${limits.max}) is less than min limit (${limits.min})")
        }
        
        // WebAssembly spec limits
        val maxPages = 65536u
        if (limits.min > maxPages) {
            errors.add("$context: min limit (${limits.min}) exceeds maximum ($maxPages)")
        }
        if (limits.max != null && limits.max > maxPages) {
            errors.add("$context: max limit (${limits.max}) exceeds maximum ($maxPages)")
        }
    }
}

/**
 * Result of module validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) {
    override fun toString(): String {
        return if (isValid) {
            "Module is valid"
        } else {
            "Module is invalid:\n" + errors.joinToString("\n") { "  - $it" }
        }
    }
}
