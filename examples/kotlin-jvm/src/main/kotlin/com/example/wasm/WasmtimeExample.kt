package com.example.wasm

// JVM example using wasmtime-java to instantiate a Wasm module and call an exported function.
// Requires the wasmtime-java dependency (see build.gradle.kts).
// This example assumes a Wasm module (example.wasm) that exports a function `add(i32,i32)->i32`.

import io.github.kawamuray.wasmtime.* // wasmtime-java package
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val wasmPath = Paths.get("examples/kotlin-jvm/example.wasm")
    val wasmBytes = Files.readAllBytes(wasmPath)

    Engine().use { engine ->
        val store = Store(engine)
        val module = Module.fromBinary(engine, wasmBytes)
        val instance = Instance(store, module, listOf())

        // Get exported function named "add"
        val add = instance.getFunc("add") ?: error("exported function 'add' not found")
        val typedAdd = add.typed(store, Int::class.java, Int::class.java, Int::class.java)

        val result = typedAdd.call(store, 3, 5)
        println("3 + 5 = $result")
    }
}