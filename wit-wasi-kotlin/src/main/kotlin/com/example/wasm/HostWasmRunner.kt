package com.example.wasm

import io.github.kawamuray.wasmtime.Engine
import io.github.kawamuray.wasmtime.Instance
import io.github.kawamuray.wasmtime.Module
import io.github.kawamuray.wasmtime.Store
import java.nio.file.Files
import java.nio.file.Paths

class HostWasmRunner(private val wasmPath: String) {
    fun add(a: Int, b: Int): Int {
        val wasmBytes = Files.readAllBytes(Paths.get(wasmPath))
        Engine().use { engine ->
            val store = Store(engine)
            val module = Module.fromBinary(engine, wasmBytes)
            val instance = Instance(store, module, listOf())
            val addFunc = instance.getFunc("add") ?: error("exported function 'add' not found")
            val typed = addFunc.typed(store, Int::class.java, Int::class.java, Int::class.java)
            return typed.call(store, a, b)
        }
    }
}