package dev.galaxynoxus.xpore;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    public static final Block EXPERIENCE_ORE = Registry.register(Registries.BLOCK,
            XpOreMod.id("experience_ore"), new ExperienceOreBlock(registerRequiresToolSettings("experience_ore", 3.0F)));

    public static final Block DEEPSLATE_EXPERIENCE_ORE = Registry.register(Registries.BLOCK,
            XpOreMod.id("deepslate_experience_ore"), new DeepslateExperienceOreBlock(registerRequiresToolSettings("deepslate_experience_ore", 4.5F)));

    private ModBlocks() { }

    private static AbstractBlock.Settings registerRequiresToolSettings(String name, float hardness) {
        return AbstractBlock.Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, XpOreMod.id(name)))
                .strength(hardness, 3.0F)
                .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                .requiresTool()
                .luminance(state -> 0);
    }

    public static void register() { }
}
