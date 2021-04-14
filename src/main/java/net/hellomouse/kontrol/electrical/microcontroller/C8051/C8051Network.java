package net.hellomouse.kontrol.electrical.microcontroller.C8051;

import net.hellomouse.kontrol.electrical.microcontroller.AbstractMUCNetwork;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class C8051Network extends AbstractMUCNetwork {
    public final C8051Interpreter interpreter;

    public C8051Network(int id) {
        super(id);
        interpreter = new C8051Interpreter();
    }

    @Override
    public void tick() {
        // Update ports
        interpreter.state.updatePorts(this);

        // Interpreter tick
        try { doInterpreterLoop().get(); }
        catch (InterruptedException | ExecutionException ignored) {}
    }

    public Future<Void> doInterpreterLoop() throws InterruptedException {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            interpreter.interpreter.interpret(1);
            completableFuture.complete(null);
            return null;
        });
        return completableFuture;
    }
}
