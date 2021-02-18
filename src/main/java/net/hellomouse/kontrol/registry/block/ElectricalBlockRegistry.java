package net.hellomouse.kontrol.registry.block;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.hellomouse.kontrol.Kontrol;
import net.hellomouse.kontrol.electrical.block.*;
import net.hellomouse.kontrol.electrical.block.entity.*;
import net.hellomouse.kontrol.electrical.client.render.block.entity.CapacitorEntityRenderer;
import net.hellomouse.kontrol.electrical.client.render.block.entity.ResistorEntityRenderer;
import net.hellomouse.kontrol.electrical.client.render.block.entity.ScopeEntityRenderer;
import net.hellomouse.kontrol.electrical.items.ElectricalBlockItem;
import net.hellomouse.kontrol.electrical.screen.BoxScreen;
import net.hellomouse.kontrol.electrical.screen.BoxScreenHandler;
import net.hellomouse.kontrol.registry.util.BlockWrapper;
import net.hellomouse.kontrol.registry.util.ColorData;
import net.hellomouse.kontrol.util.specific.ResistorUtil;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;



public class ElectricalBlockRegistry extends AbstractBlockRegistry {
    // Block entities
    public static BlockEntityType<FurnaceGeneratorEntity> FURNACE_GENERATOR_ENTITY;
    public static BlockEntityType<CableBlockEntity> WIRE_BLOCK_ENTITY;
    public static BlockEntityType<ElectricalGroundEntity> ELECTRICAL_GROUND_ENTITY;
    public static BlockEntityType<CapacitorBlockEntity> CAPACITOR_BLOCK_ENTITY;
    public static BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY;
    public static BlockEntityType<ResistorBlockEntity> RESISTOR_BLOCK_ENTITY;
    public static BlockEntityType<DiodeBlockEntity> DIODE_BLOCK_ENTITY;
    public static BlockEntityType<SwitchBlockEntity> SWITCH_BLOCK_ENTITY;
    public static BlockEntityType<InductorBlockEntity> INDUCTOR_BLOCK_ENTITY;
    public static BlockEntityType<ScopeBlockEntity> SCOPE_BLOCK_ENTITY;
    public static BlockEntityType<SuperconductingCableBlockEntity> SUPERCONDUCTING_WIRE_BLOCK_ENTITY;
    public static BlockEntityType<PushButtonBlockEntity> PUSH_BUTTON_BLOCK_ENTITY;
    public static BlockEntityType<LightBlockEntity> LIGHT_BLOCK_ENTITY;
    public static BlockEntityType<LEDBlockEntity> LED_BLOCK_ENTITY;


    // Screen handlers
    public static ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER;


    @SuppressWarnings("unchecked")
    public static void register() {
        // Colored blocks
        for (ColorData.COLOR_STRING color : ColorData.COLOR_STRING.values()) {
            addBlock(new BlockWrapper()
                .name(color + "_basic_cable")
                .block(new BasicCableBlock(FabricBlockSettings
                    .of(Material.WOOL).nonOpaque().breakByHand(true)
                    .strength(0.1f, 0.1f)
                    .materialColor(ColorData.nameToMaterialColor(color)),
                        color))
                .color(color)
                .blockEntityName("cable_block_entity"));

            addBlock(new BlockWrapper()
                    .name(color + "_creative_cable")
                    .block(new CreativeCableBlock(FabricBlockSettings
                            .of(Material.METAL).nonOpaque()
                            .strength(-1.0f, 3600000.0f).dropsNothing()
                            .materialColor(ColorData.nameToMaterialColor(color)),
                            color))
                    .color(color)
                    .blockEntityName("superconducting_cable_block_entity"));
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
            .blockEntityName("electrical_ground_entity"));

        addBlock(new BlockWrapper()
            .name("basic_capacitor")
            .block(new BasicCapacitorBlock(FabricBlockSettings
                .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)))
            .blockEntityName("capacitor_block_entity")
        );

        addBlock(new BlockWrapper()
                .name("basic_push_button")
                .block(new BasicPushButtonBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)))
                .blockEntityName("push_button_block_entity")
        );

        addBlock(new BlockWrapper()
                .name("basic_switch")
                .block(new BasicSwitchBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)))
                .blockEntityName("switch_block_entity")
        );

        addBlock(new BlockWrapper()
                .name("basic_led")
                .block(new BasicLEDBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(3.5f, 3.5f)
                        .luminance(blockState -> blockState.get(BasicLightBlock.BRIGHTNESS))))
                .blockEntityName("led_block_entity")
        );


        addBlock(new BlockWrapper()
                .name("creative_battery")
                .block(new CreativeBatteryBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("battery_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("creative_resistor")
                .block(new CreativeResistorBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("resistor_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("creative_capacitor")
                .block(new CreativeCapacitorBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("capacitor_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("creative_inductor")
                .block(new CreativeInductorBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("inductor_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("creative_switch")
                .block(new CreativeSwitchBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("switch_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );

        addBlock(new BlockWrapper()
                .name("creative_scope")
                .block(new CreativeScopeBlock(FabricBlockSettings
                        .of(Material.METAL).nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()))
                .blockEntityName("scope_block_entity")
                .item(ElectricalBlockItem::new, new FabricItemSettings().group(ItemGroup.REDSTONE).rarity(Rarity.EPIC))
        );


//        BASIC_LIGHT_BLOCK = new BasicLightBlock(FabricBlockSettings
//                .of(Material.GLASS).nonOpaque().strength(1.5f, 0.5f)
//                .luminance(blockState -> blockState.get(BasicLightBlock.BRIGHTNESS)));
//        addBlock(new BlockWrapper()
//                .name("basic_light")
//                .block(BASIC_LIGHT_BLOCK) // TODO: settings & color and shit
//                .blockEntityName("light_block_entity")
//        );



        // Block entities
        FURNACE_GENERATOR_ENTITY = (BlockEntityType<FurnaceGeneratorEntity>)getRegisteredBlockEntity(
                "furnace_generator", "furnace_generator_entity", FurnaceGeneratorEntity::new);
        WIRE_BLOCK_ENTITY = (BlockEntityType<CableBlockEntity>)getRegisteredBlockEntity(
                "cable_block", "cable_block_entity", CableBlockEntity::new);
        ELECTRICAL_GROUND_ENTITY = (BlockEntityType<ElectricalGroundEntity>)getRegisteredBlockEntity(
            "electrical_ground", "electrical_ground_entity", ElectricalGroundEntity::new);
        CAPACITOR_BLOCK_ENTITY = (BlockEntityType<CapacitorBlockEntity>)getRegisteredBlockEntity(
                "capacitor_block", "capacitor_block_entity", CapacitorBlockEntity::new);
        // LIGHT_BLOCK_ENTITY = (BlockEntityType<LightBlockEntity>)getRegisteredBlockEntity(
        //        "light_source", "light_block_entity", LightBlockEntity::new);
        RESISTOR_BLOCK_ENTITY = (BlockEntityType<ResistorBlockEntity>)getRegisteredBlockEntity(
                "resistor_block", "resistor_block_entity", ResistorBlockEntity::new);
        BATTERY_BLOCK_ENTITY = (BlockEntityType<BatteryBlockEntity>)getRegisteredBlockEntity(
                "battery_block", "battery_block_entity", BatteryBlockEntity::new);
        SWITCH_BLOCK_ENTITY = (BlockEntityType<SwitchBlockEntity>)getRegisteredBlockEntity(
                "switch_block", "switch_block_entity", SwitchBlockEntity::new);
        INDUCTOR_BLOCK_ENTITY = (BlockEntityType<InductorBlockEntity>)getRegisteredBlockEntity(
                "inductor_block", "inductor_block_entity", InductorBlockEntity::new);
        SCOPE_BLOCK_ENTITY = (BlockEntityType<ScopeBlockEntity>)getRegisteredBlockEntity(
                "scope_block", "scope_block_entity", ScopeBlockEntity::new);
        PUSH_BUTTON_BLOCK_ENTITY = (BlockEntityType<PushButtonBlockEntity>)getRegisteredBlockEntity(
                "push_button_block", "push_button_block_entity", PushButtonBlockEntity::new);
        LIGHT_BLOCK_ENTITY = (BlockEntityType<LightBlockEntity>)getRegisteredBlockEntity(
                "light_block", "light_block_entity", LightBlockEntity::new);
        LED_BLOCK_ENTITY = (BlockEntityType<LEDBlockEntity>)getRegisteredBlockEntity(
                "led_block", "led_block_entity", LEDBlockEntity::new);
        DIODE_BLOCK_ENTITY = (BlockEntityType<DiodeBlockEntity>)getRegisteredBlockEntity(
                "diode_block", "diode_block_entity", DiodeBlockEntity::new);

        SUPERCONDUCTING_WIRE_BLOCK_ENTITY = (BlockEntityType<SuperconductingCableBlockEntity>)getRegisteredBlockEntity(
                "superconducting_cable_block", "superconducting_cable_block_entity", SuperconductingCableBlockEntity::new);


        // Screen Handlers
        BOX_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(Kontrol.MOD_ID, "box_block"), BoxScreenHandler::new);

        // Goes at end, register blocks added
        AbstractBlockRegistry.register();
    }

    public static void registerClient() {
        AbstractBlockRegistry.registerClient();

        // TODO: color by proper color
        //  ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ColorData.DYEABLE_COLORS.get(color), wrapper.getItem());
        // ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> ColorData.darken(BasicLightBlock.LIGHT_COLOR, (1 - state.get(BasicLightBlock.BRIGHTNESS)  / 15.0f) / 2.0f), BASIC_LIGHT_BLOCK);

        // TODO: for loop to iterate all resistors

        ColorProviderRegistry.BLOCK.register(ResistorUtil::getColorForBlock, lookup("creative_resistor").getBlock());
        ColorProviderRegistry.ITEM.register(ResistorUtil::getColorForItemStack, lookup("creative_resistor").getItem());


       // BlockRenderLayerMap.INSTANCE.putBlock(BASIC_LIGHT_BLOCK, RenderLayer.getTranslucent());

        BlockEntityRendererRegistry.INSTANCE.register(RESISTOR_BLOCK_ENTITY, ResistorEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(CAPACITOR_BLOCK_ENTITY, CapacitorEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(SCOPE_BLOCK_ENTITY, ScopeEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier("data"), (client, handler, buf, responseSender) -> {
            int data = buf.readInt();
            int[] readings = buf.readIntArray();
            BlockPos pos = buf.readBlockPos();

            if (client.world != null) {
                BlockEntity e = client.world.getBlockEntity(pos);
                if (e != null && e instanceof ScopeBlockEntity) {
                    ((ScopeBlockEntity) e).getScopeState().forceUpdate(data, readings);
                }
            }
        });

        ScreenRegistry.register(BOX_SCREEN_HANDLER, BoxScreen::new);
    }
}
