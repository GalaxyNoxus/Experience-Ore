package dev.galaxynoxus.xpore;

import net.minecraft.world.level.levelgen.Heightmap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class CaveSurfacePlacementModifier extends PlacementFilter {
    public static final MapCodec<CaveSurfacePlacementModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("deepslate", false).forGetter(value -> value.deepslate)
    ).apply(instance, CaveSurfacePlacementModifier::new));
    private final boolean deepslate;
    private CaveSurfacePlacementModifier(boolean deepslate) { this.deepslate = deepslate; }
    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        int rarity = XpOreConfig.get().caveSurfaceRarity();
        return rarity > 0 && (!deepslate || XpOreConfig.get().enableDeepslateVariant()) && pos.getY() + 1 < context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) && random.nextInt(rarity) == 0;
    }
    @Override
    public PlacementModifierType<?> type() { return ModWorldGeneration.CAVE_SURFACE.get(); }
}
