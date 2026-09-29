package dev.galaxynoxus.xpore;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ConfigRarityPlacementModifier extends PlacementFilter {
    public static final ConfigRarityPlacementModifier INSTANCE = new ConfigRarityPlacementModifier();
    public static final MapCodec<ConfigRarityPlacementModifier> CODEC = MapCodec.unit(INSTANCE);
    private ConfigRarityPlacementModifier() { }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return random.nextInt(XpOreConfig.get().oreGenerationRarity()) == 0;
    }
    @Override
    public PlacementModifierType<?> type() { return ModWorldGeneration.CONFIG_RARITY; }
}
