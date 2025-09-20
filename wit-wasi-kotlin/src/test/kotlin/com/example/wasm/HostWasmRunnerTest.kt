package com.example.wasm

import kotlin.test.Test
import kotlin.test.assertEquals

class HostWasmRunnerTest {
    @Test
    fun testAdd() {
        val runner = HostWasmRunner("examples/kotlin-jvm/example.wasm")
        assertEquals(5, runner.add(2, 3))
    }
}