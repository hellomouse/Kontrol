package net.hellomouse.kontrol.registry;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
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
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ElectricalBlockRegistry extends AbstractBlockRegistry {

    public static BlockEntityType<FurnaceGeneratorEntity> FURNACE_GENERATOR_ENTITY;
    public static BlockEntityType<WireBlockEntity> WIRE_BLOCK_ENTITY;

    public static ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER;


    public static void register() {

        // Colored blocks
        for (ColorData.COLOR_STRING color : ColorData.COLOR_STRING.values()) {
            addBlock(new BlockWrapper()
                .name(color + "_basic_wire")
                .block(new BasicWireBlock(FabricBlockSettings
                    .of(Material.WOOL).nonOpaque().breakByHand(true)
                    .strength(0.1f, 0.1f)
                    .materialColor(ColorData.nameToMaterialColor(color.toString()))))
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


        // FURNACE_GENERATOR_ENTITY = (BlockEntityType<FurnaceGeneratorEntity>) AbstractBlockRegistry.getRegisteredBlockEntity("furnace_generator", "furnace_generator_entity", FurnaceGeneratorEntity::new);
        WIRE_BLOCK_ENTITY = (BlockEntityType<WireBlockEntity>) AbstractBlockRegistry.getRegisteredBlockEntity("wire_block", "wire_block_entity", WireBlockEntity::new);

        // Block entities
        // TODO it block entity way
        // TODO turn tis into a method
        FURNACE_GENERATOR_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Kontrol.MOD_ID, "furnace_generator"),
                BlockEntityType.Builder.create(FurnaceGeneratorEntity::new, getBlock("furnace_generator")).build(null));

        //WIRE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Kontrol.MOD_ID, "wire_block"),
        //        BlockEntityType.Builder.create(WireBlockEntity::new, blockEntityMap.get("wire_block_entity").toArray(new Block[0])).build(null));

        // Screen Handlers
        BOX_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Kontrol.MOD_ID, "box_block"), BoxScreenHandler::new);


        AbstractBlockRegistry.register();
    }
}
