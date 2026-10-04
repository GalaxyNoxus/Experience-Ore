package dev.galaxynoxus.xpore;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public final class ConfigRarityPlacementModifier extends AbstractConditionalPlacementModifier {
    public static final ConfigRarityPlacementModifier INSTANCE = new ConfigRarityPlacementModifier();
    public static final Codec<ConfigRarityPlacementModifier> CODEC = Codec.unit(INSTANCE);
    private ConfigRarityPlacementModifier() { }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        return random.nextInt(XpOreConfig.get().oreGenerationRarity()) == 0;
    }
    @Override
    public PlacementModifierType<?> getType() { return ModWorldGeneration.CONFIG_RARITY; }
}
