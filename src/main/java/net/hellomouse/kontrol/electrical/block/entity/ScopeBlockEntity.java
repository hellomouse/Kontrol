package net.hellomouse.kontrol.electrical.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hellomouse.kontrol.electrical.circuit.virtual.VirtualCircuit;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualCurrentSource;
import net.hellomouse.kontrol.electrical.circuit.virtual.components.VirtualResistor;
import net.hellomouse.kontrol.electrical.misc.ScopeState;
import net.hellomouse.kontrol.registry.block.ElectricalBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public class ScopeBlockEntity extends AbstractPolarizedElectricalBlockEntity implements BlockEntityClientSerializable {
    private ScopeState state = null;

    public static int T_BASIC, T_ADVANCED, T_MODERN, T_CREATIVE;
    static {
        T_BASIC = 0;
        T_ADVANCED = 1;
        T_MODERN = 2;
        T_CREATIVE = 3;
    }


    public static final ScopeTier BASIC, ADVANCED, MODERN, CREATIVE;
    static {
        BASIC = new ScopeTier(0,
                new ScopeState.ScopeGraphics(128, 96, new int[]{4, 4, 4, 4},
                        0xff000000, 0xff777777, 0xff6ed1ff, 0xff222222, 0xff506670,
                        10, 10)

                ,  1e6);

        ADVANCED = BASIC;
        MODERN = BASIC;

        CREATIVE = new ScopeTier(3,
                new ScopeState.ScopeGraphics(128, 96, new int[]{4, 4, 4, 4},
                        0xff000000, 0xff777777, 0xff6ed1ff, 0xff222222, 0xff506670,
                        10, 10),  1e12);
    }

    public static ScopeTier getTier(int tier) {
        switch(tier) {
            case 0: return BASIC;
            case 1: return ADVANCED;
            case 2: return MODERN;
            case 3: return CREATIVE;
        }
        throw new IllegalStateException("Tier " + tier + " is invalid!");
    }



    public ScopeTier tier;

    public ScopeBlockEntity() {
        super(ElectricalBlockRegistry.SCOPE_BLOCK_ENTITY);
        setRotate(true);
    }

    public ScopeBlockEntity tier(int tier) {
        this.tier = getTier(tier);
        return this;
    }

    @Override
    public boolean recomputeEveryTick() { return false; }

    @Override
    public void tick() {
        // TODO: delay based on time axis
        super.tick();

        if (nodalVoltages.size() != 2)
            return;

        if ((world.getTime() % (1 / state.getTimeScale())) != 0)
            return;

        if (internalCircuit.getComponents().size() > 0 && state != null) {
            // TODO Why this crash?? (dlete capacitor = crash)
            state.addReading(internalCircuit.getComponents().get(0).getVoltage());

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(state.getDataStart());
            buf.writeIntArray(state.getReadings());
            buf.writeBlockPos(pos);

;
            ServerPlayerEntity player = (ServerPlayerEntity) world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 64.0f, false);

            if (player != null)
                ServerPlayNetworking.send(player, new Identifier("data"), buf);

            markDirty();
        }
    }




    @Override
    public VirtualCircuit getInternalCircuit() {
        internalCircuit.clear();
        if (normalizedOutgoingNodes.size() == 2) {
            sortOutgoingNodesByPolarity();
            internalCircuit.addComponent(new VirtualResistor(tier.resistance), normalizedOutgoingNodes.get(0), normalizedOutgoingNodes.get(1));
        }
        return internalCircuit;
    }



    // TODO: take scope state depending on a type enum
    public void createScopeState(int maxReadings) {
        //  TODO: pos isnt set, use something else as ID
        String id = "scope-state" + Math.round(Math.random() * 100);
        state = new ScopeState(id, maxReadings, tier.graphics);
        state.setScale(1.0f / 12.0f, 1.0f);
    }

    public ScopeState getScopeState() { return state; }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        this.tier = getTier(tag.getInt("Tier"));

        if (this.state == null)
            this.createScopeState(128); // TODO: save scope state variables as something else idk in constructor?
        this.state.fromTag(tag.getCompound("ScopeState"));
        super.fromTag(state, tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.put("ScopeState", state.toTag(new CompoundTag()));
        tag.putInt("Tier", tier.id);
        return super.toTag(tag);
    }


    public void fromClientTag(CompoundTag tag) {
        this.tier = getTier(tag.getInt("Tier"));

        if (this.state == null)
            this.createScopeState(128); // TODO: save scope state variables as something else idk in constructor?
        this.state.fromTag(tag.getCompound("ScopeState"));
    }

    public CompoundTag toClientTag(CompoundTag tag) {
        tag.put("ScopeState", state.toTag(new CompoundTag()));
        tag.putInt("Tier", tier.id);
        return tag;
    }



    /**
     * Stores data for a tier of scope entity. Only the tier is stored to NBT,
     * when loaded properties are inferred from the tier data stored in this class
     * @author Bowserinator
     */
    public static class ScopeTier {
        // Rendering
        public final ScopeState.ScopeGraphics graphics;

        // Circuit solving
        public final double resistance;
        public final int id;

        /**
         * Construct a scope tier
         * @param id Id of the tier, should match the switch statement for getting tier from a value
         * @param graphics ScopeGraphics object for this tier
         * @param resistance If isIdeal is false, the effective resistance of the scope (should be large)
         */
        public ScopeTier(int id, ScopeState.ScopeGraphics graphics, double resistance) {
            this.id = id;
            this.graphics = graphics;
            this.resistance = resistance;
        }
    }
}
