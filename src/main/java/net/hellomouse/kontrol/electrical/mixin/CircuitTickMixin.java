package net.hellomouse.kontrol.electrical.mixin;

import net.hellomouse.kontrol.electrical.circuit.CircuitManager;
import net.hellomouse.kontrol.electrical.circuit.IHasCircuitManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Injects a CircuitManager into ServerWorld, makes the circuit manager
 * solve all the circuits after ticking all block entities
 * @author Bowserinator
 */
@Mixin(ServerWorld.class)
public abstract class CircuitTickMixin implements IHasCircuitManager {
    @Unique
    public CircuitManager circuitManager = new CircuitManager();

    @Override
    public CircuitManager getCircuitManager() {
        return circuitManager;
    }

    // TODO: maybe inject after method was called, not in method itself
    @Inject(at = @At("TAIL"), method = "tick(Ljava/util/function/BooleanSupplier;)V")
    private void tick(CallbackInfo info) {
        // gO THROUGH ALL CIRCUITS
        // if (dirty()) - resolve
        // if (invalid) - mark invalid, delete all references from blockEntities
        // solving should be on another thread that is synced at end of tick?
        // (also a start tick thread that begins solve)
        // TODO keep track of profiler
        circuitManager.postTick();
    }
}
