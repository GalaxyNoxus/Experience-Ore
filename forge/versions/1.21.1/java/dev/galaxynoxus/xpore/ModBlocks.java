package dev.galaxynoxus.xpore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, XpOreMod.MOD_ID);
    public static final RegistryObject<Block> EXPERIENCE_ORE = BLOCKS.register("experience_ore", () -> new ExperienceOreBlock(settings("experience_ore", 3.0F)));
    public static final RegistryObject<Block> DEEPSLATE_EXPERIENCE_ORE = BLOCKS.register("deepslate_experience_ore", () -> new DeepslateExperienceOreBlock(settings("deepslate_experience_ore", 4.5F)));
    private ModBlocks() { }
    private static BlockBehaviour.Properties settings(String name, float hardness) { return BlockBehaviour.Properties.of().strength(hardness, 3.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops(); }
}
