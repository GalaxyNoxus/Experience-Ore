package dev.galaxynoxus.xpore;

import net.minecraft.world.level.levelgen.Heightmap;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public final class CaveSurfacePlacementModifier implements PlacementFilter {
    public static final CaveSurfacePlacementModifier INSTANCE = new CaveSurfacePlacementModifier();
    public static final MapCodec<CaveSurfacePlacementModifier> CODEC = MapCodec.unit(INSTANCE);
    private CaveSurfacePlacementModifier() { }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        int rarity = XpOreConfig.get().caveSurfaceRarity();
        return rarity > 0 && pos.getY() + 1 < context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) && random.nextInt(rarity) == 0;
    }
    @Override
    public MapCodec<CaveSurfacePlacementModifier> codec() { return CODEC; }
}
