# Add WASI + WIT (WebAssembly Interface Types) support for Kotlin & Java

## Summary
Add support for running and interacting with WebAssembly modules from Kotlin and Java with first-class support for WASI and WebAssembly Interface Types (WIT). Provide example code, bindings or runtime wrappers, CI, and documentation for how to compile, run, and interoperate with wasm modules safely and reproducibly.

## Goals
- Provide a clear set of strategies and reference implementations for using WIT/WASI from:
  - Kotlin (JVM and/or Kotlin Multiplatform where feasible)
  - Java (JVM)
- Offer at least one runnable reference:
  - A Kotlin JVM module that instantiates a wasm module in a host runtime (e.g., Wasmtime) and calls exported/imported functions using interface types where available.
  - A Java example showing the same using a Java embedding of a Wasm runtime.
- Document how to generate or wire WIT bindings (tools/approaches) and the tradeoffs per platform.
- Add CI that builds the examples and runs the JVM tests. Document how to run wasm examples locally (wasmtime, wasmer, etc.)

## Non-goals
- Implementing or maintaining a full kotlin-native wasm toolchain. We will document interoperability and provide examples but not full toolchain support for all WASI hosts.
- Running a full JVM in WASI; instead, focus on using the JVM to host wasm modules (embedding runtime) and producing wasm for Kotlin where reasonable.

## Acceptance criteria
- A README describing approaches and how to run the examples.
- A Kotlin/JVM example that:
  - Loads a wasm module in a host runtime (Wasmtime) and invokes a function with typed inputs/outputs.
  - Shows at least one example using a generated/handwritten adapter to simulate WIT interface types.
- A Java example that does the equivalent.
- Unit tests that run on the JVM CI.
- A section documenting how to target wasm32 (Kotlin MPP or Kotlin/Native) and the current limitations.

## Implementation tasks
- [ ] Create repository skeleton and modules (wit-wasi-kotlin, wit-wasi-java, examples)
- [ ] Add Kotlin/JVM example (using wasmtime-java)
- [ ] Add Java example (using wasmtime-java)
- [ ] Research and document tooling for generating WIT bindings (wit-bindgen, host toolchains) and include recommended commands
- [ ] Add CI (GitHub Actions) to run JVM tests
- [ ] Add documentation for running wasm examples on popular runtimes (wasmtime, wasmer)
- [ ] Add issue templates and contribution notes
