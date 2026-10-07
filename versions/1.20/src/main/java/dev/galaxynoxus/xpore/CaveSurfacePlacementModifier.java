package dev.galaxynoxus.xpore;

import net.minecraft.world.Heightmap;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public final class CaveSurfacePlacementModifier extends AbstractConditionalPlacementModifier {
    public static final CaveSurfacePlacementModifier INSTANCE = new CaveSurfacePlacementModifier();
    public static final Codec<CaveSurfacePlacementModifier> CODEC = Codec.unit(INSTANCE);
    private CaveSurfacePlacementModifier() { }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        int rarity = XpOreConfig.get().caveSurfaceRarity();
        return rarity > 0 && pos.getY() + 1 < context.getTopY(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) && random.nextInt(rarity) == 0;
    }
    @Override
    public PlacementModifierType<?> getType() { return ModWorldGeneration.CAVE_SURFACE; }
}
