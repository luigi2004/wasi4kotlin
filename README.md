```markdown
# wasi4kotlin

This repository collects examples and guidance for using WASI and WebAssembly Interface Types (WIT) from Kotlin and Java (JVM).

## Modules

- **wasmparser**: A Kotlin implementation of a WebAssembly binary parser, inspired by the Rust [wasmparser](https://github.com/bytecodealliance/wasm-tools/tree/main/crates/wasmparser) crate. This module provides event-driven parsing of WebAssembly binary files with low memory overhead. See [wasmparser/README.md](wasmparser/README.md) for details.
- **wit-wasi-kotlin**: Kotlin library for working with WebAssembly modules
- **wit-wasi-java**: Java library for working with WebAssembly modules
- **examples**: Example projects demonstrating WebAssembly usage
  - **kotlin-jvm**: Basic Kotlin/JVM example using wasmtime-java
  - **java-jvm**: Basic Java example using wasmtime-java
  - **wasmparser-integration**: Example showing module analysis using wasmparser

Quickstart

1. Build the example wasm modules (requires wat2wasm):
   - wat2wasm examples/kotlin-jvm/example.wat -o examples/kotlin-jvm/example.wasm
   - wat2wasm examples/java-jvm/example.wat   -o examples/java-jvm/example.wasm

2. Run the tests and build everything:
   - ./gradlew -s build

3. Run the Kotlin JVM example (after compiling .wasm as above, optional):
   - ./gradlew :wit-wasi-kotlin:test

4. Run the Java JVM example (after compiling .wasm as above, optional):
   - ./gradlew :wit-wasi-java:test

5. Run the wasmparser integration example:
   - ./gradlew :examples:wasmparser-integration:run

Notes
- This repository includes .wat sources (text format). Compile to .wasm using wat2wasm from the WebAssembly Binary Toolkit (wabt).
- The examples use wasmtime-java as the embedded runtime. The Gradle files reference io.github.kawamuray:wasmtime:0.40.0 as an example; please verify the latest stable version when updating dependencies.

See ISSUE.md for roadmap and next tasks.
```