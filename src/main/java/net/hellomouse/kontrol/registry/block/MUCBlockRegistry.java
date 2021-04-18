package net.hellomouse.kontrol.registry.block;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.block.microcontroller.*;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCMakerBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.CreativeMUCPortMakerBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCPortBlockEntity;
import net.hellomouse.kontrol.electrical.block.microcontroller.entity.MUCRedstonePortBlockEntity;
import net.hellomouse.kontrol.electrical.client.render.block.entity.CreativeMUCMakerBlockEntityRenderer;
import net.hellomouse.kontrol.electrical.client.render.block.entity.CreativeMUCPortMakerBlockEntityRenderer;
import net.hellomouse.kontrol.electrical.microcontroller.MUCStatic;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCMakerScreen;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCMakerScreenHandler;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCPortMakerScreen;
import net.hellomouse.kontrol.electrical.screen.CreativeMUCPortMakerScreenHandler;
import net.hellomouse.kontrol.registry.util.BlockWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;


public class MUCBlockRegistry extends AbstractBlockRegistry {
    // Blocks
    public static final Block MUC_PORT_BLOCK = new MUCPortBlock
            (FabricBlockSettings.of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)
             .luminance(state -> state.get(MUCPortBlock.ON) ? MUCPortBlockEntity.PORT_ON_BRIGHTNESS : 0));
    public static final Block MUC_PORT_CONNECTOR_BLOCK = new MUCPortConnectorBlock(FabricBlockSettings
            .of(Material.METAL).nonOpaque()
            .strength(3.5f, 3.5f));

    // Block entities
    public static BlockEntityType<MUCPortBlockEntity> MUC_PORT_ENTITY;
    public static BlockEntityType<MUCRedstonePortBlockEntity> MUC_REDSTONE_PORT_ENTITY;
    public static BlockEntityType<CreativeMUCPortMakerBlockEntity> MUC_PORT_MAKER_BLOCK_ENTITY;
    public static BlockEntityType<CreativeMUCMakerBlockEntity> MUC_MAKER_BLOCK_ENTITY;

    // Networking
    public static final Identifier MUC_PORT_MAKER_PACKET_ID = new Identifier(Kontrol.MOD_ID, "muc_port_maker");
    public static final Identifier MUC_MAKER_PACKET_ID = new Identifier(Kontrol.MOD_ID, "muc_maker");

    // Screens
    public static final ScreenHandlerType<CreativeMUCPortMakerScreenHandler> MUC_PORT_MAKER_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(new Identifier(Kontrol.MOD_ID, "muc_port_maker"), CreativeMUCPortMakerScreenHandler::new);
    public static final ScreenHandlerType<CreativeMUCMakerScreenHandler> MUC_MAKER_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(new Identifier(Kontrol.MOD_ID, "muc_maker"), CreativeMUCMakerScreenHandler::new);

    // Resources
    public static final String MUC_BLUEPRINTS = "muc_schematics";

    @SuppressWarnings("unchecked")
    public static void register() {
        // Resource listeners
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() { return new Identifier(Kontrol.MOD_ID, MUC_BLUEPRINTS); }

            @Override
            public void apply(ResourceManager manager) {
                MUCStatic.MUCBlueprints.clear();

                for (Identifier id : manager.findResources(MUC_BLUEPRINTS, path -> path.endsWith(".muc"))) {
                    try {
                        InputStream stream = manager.getResource(id).getInputStream();
                        Scanner scanner = new Scanner(stream);
                        String name = scanner.nextLine();
                        ArrayList<String[]> blueprint = new ArrayList<>();
                        int width = 0;

                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            String[] ports = line.split("\\s");

                            if (width > 0 && ports.length != width)
                                throw new IllegalStateException("MUC schematic must be rectangular");
                            width = ports.length;
                            blueprint.add(ports);
                        }

                        MUCStatic.MUCBlueprints.put(name, blueprint);
                    } catch(Exception e) {
                        Kontrol.LOG.error("Error occurred while loading resource json " + id.toString(), e);
                    }
                }
            }
        });

        // Blocks
        addBlock(new BlockWrapper()
                .name("muc_port")
                .block(MUC_PORT_BLOCK)
                .blockEntityName("muc_port_entity"));

        addBlock(new BlockWrapper()
                .name("c8051_core")
                .block(new C8051CoreBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)))
                .item(BlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.RARE)));

        addBlock(new BlockWrapper()
                .name("muc_port_maker")
                .block(new CreativeMUCPortMakerBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque()
                        .strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("muc_port_maker_entity")
                .item(BlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("muc_maker")
                .block(new CreativeMUCMakerBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque()
                        .strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("muc_maker_entity")
                .item(BlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("muc_port_connector")
                .block(MUC_PORT_CONNECTOR_BLOCK)
        );

        addBlock(new BlockWrapper()
                .name("muc_redstone_port_1")
                .block(new MUCRedstonePortBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque()
                        .strength(3.5f, 3.5f)
                        .luminance(state -> state.get(MUCRedstonePortBlock.POWERING) ? 7 : 0)))
                .blockEntityName("muc_redstone_port_entity")
        );

        // Block entities
        MUC_PORT_ENTITY = (BlockEntityType<MUCPortBlockEntity>)getRegisteredBlockEntity(
                "muc_port", "muc_port_entity", MUCPortBlockEntity::new);
        MUC_REDSTONE_PORT_ENTITY = (BlockEntityType<MUCRedstonePortBlockEntity>)getRegisteredBlockEntity(
                "muc_redstone_port", "muc_redstone_port_entity", MUCRedstonePortBlockEntity::new);
        MUC_PORT_MAKER_BLOCK_ENTITY = (BlockEntityType<CreativeMUCPortMakerBlockEntity>)getRegisteredBlockEntity(
                "muc_port_maker", "muc_port_maker_entity", CreativeMUCPortMakerBlockEntity::new);
        MUC_MAKER_BLOCK_ENTITY = (BlockEntityType<CreativeMUCMakerBlockEntity>)getRegisteredBlockEntity(
                "muc_maker", "muc_maker_entity", CreativeMUCMakerBlockEntity::new);

        //noinspection deprecation
        ServerSidePacketRegistry.INSTANCE.register(MUC_PORT_MAKER_PACKET_ID, (packetContext, attachedData) -> {
            try {
                BlockPos pos = attachedData.readBlockPos();
                int rotationIndex = attachedData.readInt();
                int sideLength = attachedData.readInt();
                int portLower = attachedData.readInt();
                int portUpper = attachedData.readInt();
                int currentMUC = attachedData.readInt();

                packetContext.getTaskQueue().execute(() -> {
                    BlockEntity blockEntity = packetContext.getPlayer().world.getBlockEntity(pos);
                    if (blockEntity instanceof CreativeMUCPortMakerBlockEntity && packetContext.getPlayer().world.canSetBlock(pos))
                        // Type checking done here
                        ((CreativeMUCPortMakerBlockEntity)blockEntity).writePacketData(rotationIndex, sideLength, portLower, portUpper, currentMUC);
                });
            }
            catch(IndexOutOfBoundsException ignored) {}
        });

        //noinspection deprecation
        ServerSidePacketRegistry.INSTANCE.register(MUC_MAKER_PACKET_ID, (packetContext, attachedData) -> {
            try {
                BlockPos pos = attachedData.readBlockPos();
                int rotationIndex = attachedData.readInt();
                int currentMUC = attachedData.readInt();

                packetContext.getTaskQueue().execute(() -> {
                    BlockEntity blockEntity = packetContext.getPlayer().world.getBlockEntity(pos);
                    if (blockEntity instanceof CreativeMUCMakerBlockEntity && packetContext.getPlayer().world.canSetBlock(pos))
                        // Type checking done here
                        ((CreativeMUCMakerBlockEntity)blockEntity).writePacketData(rotationIndex, currentMUC);
                });
            }
            catch(IndexOutOfBoundsException ignored) {}
        });
    }

    public static void registerClient() {
        ScreenRegistry.register(MUC_PORT_MAKER_SCREEN_HANDLER, CreativeMUCPortMakerScreen::new);
        BlockEntityRendererRegistry.INSTANCE.register(MUC_PORT_MAKER_BLOCK_ENTITY, CreativeMUCPortMakerBlockEntityRenderer::new);
        ScreenRegistry.register(MUC_MAKER_SCREEN_HANDLER, CreativeMUCMakerScreen::new);
        BlockEntityRendererRegistry.INSTANCE.register(MUC_MAKER_BLOCK_ENTITY, CreativeMUCMakerBlockEntityRenderer::new);
    }
}
