package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {
    public static final Item EXPERIENCE_ORE = Registry.register(Registries.ITEM,
            XpOreMod.id("experience_ore"), new BlockItem(ModBlocks.EXPERIENCE_ORE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, XpOreMod.id("experience_ore"))).useBlockPrefixedTranslationKey()));
    public static final Item DEEPSLATE_EXPERIENCE_ORE = Registry.register(Registries.ITEM,
            XpOreMod.id("deepslate_experience_ore"), new BlockItem(ModBlocks.DEEPSLATE_EXPERIENCE_ORE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, XpOreMod.id("deepslate_experience_ore"))).useBlockPrefixedTranslationKey()));

    private ModItems() { }
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("natural_blocks"))).register(entries -> {
            entries.addAfter(Items.DEEPSLATE_EMERALD_ORE, EXPERIENCE_ORE, DEEPSLATE_EXPERIENCE_ORE);
        });
    }
}
