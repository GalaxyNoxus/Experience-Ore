package dev.galaxynoxus.xpore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, XpOreMod.MOD_ID);
    public static final RegistryObject<BlockItem> EXPERIENCE_ORE = ITEMS.register("experience_ore", () -> new BlockItem(ModBlocks.EXPERIENCE_ORE.get(), settings("experience_ore")));
    public static final RegistryObject<BlockItem> DEEPSLATE_EXPERIENCE_ORE = ITEMS.register("deepslate_experience_ore", () -> new BlockItem(ModBlocks.DEEPSLATE_EXPERIENCE_ORE.get(), settings("deepslate_experience_ore")));
    private ModItems() { }
    private static Item.Properties settings(String name) { return new Item.Properties(); }
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)) {
            event.getEntries().putAfter(new ItemStack(Items.DEEPSLATE_EMERALD_ORE), new ItemStack(EXPERIENCE_ORE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.getEntries().putAfter(new ItemStack(EXPERIENCE_ORE.get()), new ItemStack(DEEPSLATE_EXPERIENCE_ORE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
