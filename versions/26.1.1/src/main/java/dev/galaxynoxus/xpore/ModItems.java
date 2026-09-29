package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static final Item EXPERIENCE_ORE = register("experience_ore", ModBlocks.EXPERIENCE_ORE);
    public static final Item DEEPSLATE_EXPERIENCE_ORE = register("deepslate_experience_ore", ModBlocks.DEEPSLATE_EXPERIENCE_ORE);

    private ModItems() { }

    private static Item register(String name, Block block) {
        var id = XpOreMod.id(name);
        var key = ResourceKey.create(Registries.ITEM, id);
        var properties = new Item.Properties().setId(key).useBlockDescriptionPrefix();
        return Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, properties));
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, net.minecraft.resources.Identifier.withDefaultNamespace("natural_blocks"))).register(entries -> {
            entries.insertAfter(Items.DEEPSLATE_EMERALD_ORE, EXPERIENCE_ORE, DEEPSLATE_EXPERIENCE_ORE);
        });
    }
}
