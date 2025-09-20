package com.example.wasm;

import io.github.kawamuray.wasmtime.Engine;
import io.github.kawamuray.wasmtime.Store;
import io.github.kawamuray.wasmtime.Module;
import io.github.kawamuray.wasmtime.Instance;
import io.github.kawamuray.wasmtime.Func;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

// Java example using wasmtime-java to load and call a function from a Wasm module.
public class WasmtimeExample {
    public static void main(String[] args) throws IOException {
        byte[] wasmBytes = Files.readAllBytes(Path.of("examples/java-jvm/example.wasm"));
        try (Engine engine = new Engine()) {
            Store store = new Store(engine);
            Module module = Module.fromBinary(engine, wasmBytes);
            Instance instance = new Instance(store, module, java.util.Collections.emptyList());

            Func add = instance.getFunc("add");
            if (add == null) throw new RuntimeException("exported function 'add' not found");

            // For convenience this example invokes raw function using the API; for typed calls
            // consult wasmtime-java docs for the typed helpers or wrapper code.
            Object[] params = new Object[]{3, 5};
            Object[] results = new Object[1];
            add.call(store, params, results);

            System.out.println("3 + 5 = " + results[0]);
        }
    }
}