package net.hellomouse.kontrol.electrical.mixin;

import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class CircuitTickMixin {
    @Inject(at = @At("TAIL"), method = "tick(Ljava/util/function/BooleanSupplier;)V")
    private void init(CallbackInfo info) {
        // gO THROUGH ALL CIRCUITS
        // if (dirty()) - resolve
        // if (invalid) - mark invalid, delete all references from blockEntities
        // solving should be on another thread that is synced at end of tick?
        // (also a start tick thread that begins solve)
        // note: keep track of profiler
        ElectricalBlockRegistry.CIRCUIT_MANAGER.postTick();
    }
}
