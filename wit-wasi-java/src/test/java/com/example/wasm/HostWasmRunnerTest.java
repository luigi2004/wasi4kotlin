package com.example.wasm;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.io.IOException;

public class HostWasmRunnerTest {
    @Test
    public void testAdd() throws IOException {
        HostWasmRunner runner = new HostWasmRunner("examples/java-jvm/example.wasm");
        assertEquals(10, runner.add(4, 6));
    }
}