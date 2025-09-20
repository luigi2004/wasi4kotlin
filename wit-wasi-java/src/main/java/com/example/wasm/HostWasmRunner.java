package com.example.wasm;

import io.github.kawamuray.wasmtime.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class HostWasmRunner {
    private final String wasmPath;
    public HostWasmRunner(String wasmPath) { this.wasmPath = wasmPath; }

    public int add(int a, int b) throws IOException {
        byte[] wasmBytes = Files.readAllBytes(Path.of(wasmPath));
        try (Engine engine = new Engine()) {
            Store store = new Store(engine);
            Module module = Module.fromBinary(engine, wasmBytes);
            Instance instance = new Instance(store, module, java.util.Collections.emptyList());
            Func add = instance.getFunc("add");
            if (add == null) throw new RuntimeException("exported function 'add' not found");
            Object[] params = new Object[]{a, b};
            Object[] results = new Object[1];
            add.call(store, params, results);
            return ((Number)results[0]).intValue();
        }
    }
}