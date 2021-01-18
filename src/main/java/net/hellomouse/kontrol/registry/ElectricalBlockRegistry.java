package net.hellomouse.kontrol.registry;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.blocks.electrical.BasicWireBlock;
import net.hellomouse.kontrol.blocks.electrical.ElectricalGroundBlock;
import net.hellomouse.kontrol.blocks.electrical.FurnaceGenerator;
import net.hellomouse.kontrol.entity.electrical.WireBlockEntity;
import net.hellomouse.kontrol.blocks.electrical.screen.BoxScreenHandler;
import net.hellomouse.kontrol.entity.electrical.FurnaceGeneratorEntity;
import net.hellomouse.kontrol.blocks.electrical.screen.BoxScreen;
import net.hellomouse.kontrol.registry.util.BlockWrapper;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


public class ElectricalBlockRegistry extends AbstractBlockRegistry {
    public static BlockEntityType<FurnaceGeneratorEntity> FURNACE_GENERATOR_ENTITY;
    public static BlockEntityType<WireBlockEntity> WIRE_BLOCK_ENTITY;

    public static ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER;


    @SuppressWarnings("unchecked")
    public static void register() {
        // Colored blocks
        for (ColorData.COLOR_STRING color : ColorData.COLOR_STRING.values()) {
            addBlock(new BlockWrapper()
                .name(color + "_basic_wire")
                .block(new BasicWireBlock(FabricBlockSettings
                    .of(Material.WOOL).nonOpaque().breakByHand(true)
                    .strength(0.1f, 0.1f)
                    .materialColor(ColorData.nameToMaterialColor(color))))
                .color(color)
                .blockEntityName("wire_block_entity"));
        }

        // Blocks
        addBlock(new BlockWrapper()
            .name("furnace_generator")
            .block(new FurnaceGenerator(FabricBlockSettings
                .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)
                .luminance(blockState -> blockState.get(FurnaceGenerator.LIT) ? 13 : 0)))
            .blockEntityName("furnace_generator_entity"));

        addBlock(new BlockWrapper()
            .name("electrical_ground")
            .block(new ElectricalGroundBlock(
                    FabricBlockSettings.of(Material.SOIL).nonOpaque().strength(0.2f, 0.2f)))
            .blockEntityName("wire_block_entity"));


        // Block entities
        FURNACE_GENERATOR_ENTITY = (BlockEntityType<FurnaceGeneratorEntity>)getRegisteredBlockEntity(
                "furnace_generator", "furnace_generator_entity", FurnaceGeneratorEntity::new);
        WIRE_BLOCK_ENTITY = (BlockEntityType<WireBlockEntity>)getRegisteredBlockEntity(
                "wire_block", "wire_block_entity", WireBlockEntity::new);


        // Screen Handlers
        BOX_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Kontrol.MOD_ID, "box_block"), BoxScreenHandler::new);

        // Goes at end, register blocks added
        AbstractBlockRegistry.register();
    }

    public static void registerClient() {
        AbstractBlockRegistry.registerClient();
        ScreenRegistry.register(BOX_SCREEN_HANDLER, BoxScreen::new);
    }
}
