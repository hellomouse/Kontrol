package net.hellomouse.kontrol.registry.block;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.block.microcontroller.C8051CoreBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.CreativeMUCMakerBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.MUCPortBlock;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCMakerBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCPortBlockEntity;
import net.hellomouse.kontrol.electrical.client.render.block.entity.CreativeMUCMakerBlockEntityRenderer;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCMakerScreen;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCMakerScreenHandler;
import net.hellomouse.kontrol.registry.util.BlockWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;


public class MUCBlockRegistry extends AbstractBlockRegistry {
    // Blocks
    public static final Block MUC_PORT_BLOCK = new MUCPortBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().strength(3.5f, 3.5f));

    // Block entities
    public static BlockEntityType<MUCPortBlockEntity> MUC_PORT_ENTITY;
    public static BlockEntityType<CreativeMUCMakerBlockEntity> MUC_MAKER_BLOCK_ENTITY;

    // Networking
    public static final Identifier MUC_MAKER_PACKET_ID = new Identifier(Kontrol.MOD_ID, "muc_maker");

    // Screens
    public static final ScreenHandlerType<CreativeMUCMakerScreenHandler> MUC_MAKER_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(new Identifier(Kontrol.MOD_ID, "muc_maker"), CreativeMUCMakerScreenHandler::new);

    @SuppressWarnings("unchecked")
    public static void register() {
        // Blocks
        addBlock(new BlockWrapper()
                .name("muc_port")
                .block(MUC_PORT_BLOCK)
                .blockEntityName("muc_port_entity"));

        addBlock(new BlockWrapper()
                .name("c8051_core")
                .block(new C8051CoreBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().strength(3.5f, 3.5f))));

        addBlock(new BlockWrapper()
                .name("muc_maker")
                .block(new CreativeMUCMakerBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque()
                        .strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("muc_maker_entity")
                .item(BlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        // Block entities
        MUC_PORT_ENTITY = (BlockEntityType<MUCPortBlockEntity>)getRegisteredBlockEntity(
                "muc_port", "muc_port_entity", MUCPortBlockEntity::new);
        MUC_MAKER_BLOCK_ENTITY = (BlockEntityType<CreativeMUCMakerBlockEntity>)getRegisteredBlockEntity(
                "muc_maker", "muc_maker_entity", CreativeMUCMakerBlockEntity::new);

        //noinspection deprecation
        ServerSidePacketRegistry.INSTANCE.register(MUC_MAKER_PACKET_ID, (packetContext, attachedData) -> {
            BlockPos pos = attachedData.readBlockPos();
            int rotationIndex = attachedData.readInt();
            int sideLength = attachedData.readInt();
            int portLower = attachedData.readInt();
            int portUpper = attachedData.readInt();
            int currentMUC = attachedData.readInt();

            packetContext.getTaskQueue().execute(() -> {
                BlockEntity blockEntity = packetContext.getPlayer().world.getBlockEntity(pos);
                if (blockEntity instanceof CreativeMUCMakerBlockEntity && packetContext.getPlayer().world.canSetBlock(pos))
                    // Type checking done here
                    ((CreativeMUCMakerBlockEntity)blockEntity).writePacketData(rotationIndex, sideLength, portLower, portUpper, currentMUC);
            });
        });

        // Goes at end, register blocks added
        AbstractBlockRegistry.register();
    }

    public static void registerClient() {
        AbstractBlockRegistry.registerClient();
        ScreenRegistry.register(MUC_MAKER_SCREEN_HANDLER, CreativeMUCMakerScreen::new);
        BlockEntityRendererRegistry.INSTANCE.register(MUC_MAKER_BLOCK_ENTITY, CreativeMUCMakerBlockEntityRenderer::new);
    }
}
