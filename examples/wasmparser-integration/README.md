# WebAssembly Parser Integration Example

This example demonstrates how to use the `wasmparser` module to analyze and inspect WebAssembly modules before loading them into a runtime.

## What It Does

The example:
1. Creates a simple WebAssembly module (an "add" function that takes two i32 parameters and returns their sum)
2. Validates the module using the `Validator` class
3. Parses the module to extract information about its structure
4. Displays a detailed analysis including:
   - Function types
   - Number of functions, imports, exports, tables, memories, and globals
   - Exported function names
   - Imported function names

## Running the Example

```bash
./gradlew :examples:wasmparser-integration:run
```

## Output

The example produces output like:

```
WebAssembly Module Analysis Tool
============================================================

Analyzing module...

1. Validation:
   ✓ Module is well-formed

2. Module Structure:
   - Function types: 1
   - Functions: 1
   - Imports: 0
   - Exports: 2
   - Tables: 0
   - Memories: 1
   - Globals: 0

3. Function Types:
   [0] (I32, I32) -> (I32)

5. Exported Functions:
   - add

6. Analysis Summary:
   - This module defines 1 function(s)
   - Exports 1 function(s)
   - Imports 0 function(s)
   - Uses memory

============================================================
```

## Use Cases

This type of analysis is useful for:

- **Security**: Inspecting modules before execution to understand what they do
- **Debugging**: Understanding module structure when things go wrong
- **Tooling**: Building developer tools that work with WebAssembly
- **Documentation**: Generating documentation from WebAssembly modules
- **Optimization**: Analyzing modules to identify optimization opportunities

## Code Structure

The main components are:

- `analyzeModule()`: Parses and analyzes a WebAssembly module
- `createSimpleWasmModule()`: Creates a test module for demonstration

The analysis uses both the `Validator` for validation and the `Parser` for extracting module information.
