package dev.galaxynoxus.xpore;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public final class ModBlocks {
    public static final Block EXPERIENCE_ORE = register("experience_ore", 3.0F, ExperienceOreBlock::new);
    public static final Block DEEPSLATE_EXPERIENCE_ORE = register("deepslate_experience_ore", 4.5F, DeepslateExperienceOreBlock::new);

    private ModBlocks() { }

    private static Block register(String name, float hardness, Function<BlockBehaviour.Properties, Block> factory) {
        var id = XpOreMod.id(name);
        var key = ResourceKey.create(Registries.BLOCK, id);
        var properties = BlockBehaviour.Properties.of().setId(key)
                .strength(hardness, 3.0F).sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops().lightLevel(state -> 0);
        return Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
    }

    public static void register() { }
}
