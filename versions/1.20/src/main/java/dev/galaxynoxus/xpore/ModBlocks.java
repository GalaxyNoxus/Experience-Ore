package dev.galaxynoxus.xpore;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    public static final Block EXPERIENCE_ORE = Registry.register(Registries.BLOCK,
            XpOreMod.id("experience_ore"), new ExperienceOreBlock(registerRequiresToolSettings(3.0F)));

    public static final Block DEEPSLATE_EXPERIENCE_ORE = Registry.register(Registries.BLOCK,
            XpOreMod.id("deepslate_experience_ore"), new DeepslateExperienceOreBlock(registerRequiresToolSettings(4.5F)));

    private ModBlocks() { }

    private static AbstractBlock.Settings registerRequiresToolSettings(float hardness) {
        return AbstractBlock.Settings.create()
                .strength(hardness, 3.0F)
                .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                .requiresTool()
                .luminance(state -> 0);
    }

    public static void register() { }
}
