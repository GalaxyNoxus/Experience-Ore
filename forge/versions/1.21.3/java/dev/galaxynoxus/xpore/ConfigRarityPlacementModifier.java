package dev.galaxynoxus.xpore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ConfigRarityPlacementModifier extends PlacementFilter {
    public static final MapCodec<ConfigRarityPlacementModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("deepslate", false).forGetter(value -> value.deepslate)
    ).apply(instance, ConfigRarityPlacementModifier::new));
    private final boolean deepslate;
    private ConfigRarityPlacementModifier(boolean deepslate) { this.deepslate = deepslate; }
    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return (!deepslate || XpOreConfig.get().enableDeepslateVariant()) && random.nextInt(XpOreConfig.get().oreGenerationRarity()) == 0;
    }
    @Override
    public PlacementModifierType<?> type() { return ModWorldGeneration.CONFIG_RARITY.get(); }
}
