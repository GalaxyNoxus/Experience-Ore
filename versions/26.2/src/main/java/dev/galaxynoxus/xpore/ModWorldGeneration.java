package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ModWorldGeneration {
    public static final PlacementModifierType<ConfigRarityPlacementModifier> CONFIG_RARITY = registerRarityType();
    public static final PlacementModifierType<CaveSurfacePlacementModifier> CAVE_SURFACE = registerCaveSurfaceType();
    public static final ResourceKey<ConfiguredFeature<?, ?>> EXPERIENCE_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, XpOreMod.id("experience_ore"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> DEEPSLATE_EXPERIENCE_ORE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, XpOreMod.id("deepslate_experience_ore"));
    public static final ResourceKey<PlacedFeature> EXPERIENCE_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, XpOreMod.id("experience_ore"));
    public static final ResourceKey<PlacedFeature> EXPERIENCE_ORE_CAVE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, XpOreMod.id("experience_ore_cave"));
    public static final ResourceKey<PlacedFeature> DEEPSLATE_EXPERIENCE_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, XpOreMod.id("deepslate_experience_ore"));
    public static final ResourceKey<PlacedFeature> DEEPSLATE_EXPERIENCE_ORE_CAVE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, XpOreMod.id("deepslate_experience_ore_cave"));

    private static PlacementModifierType<ConfigRarityPlacementModifier> registerRarityType() {
        PlacementModifierType<ConfigRarityPlacementModifier> type = () -> ConfigRarityPlacementModifier.CODEC;
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, XpOreMod.id("config_rarity"), type);
    }
    private static PlacementModifierType<CaveSurfacePlacementModifier> registerCaveSurfaceType() {
        PlacementModifierType<CaveSurfacePlacementModifier> type = () -> CaveSurfacePlacementModifier.CODEC;
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, XpOreMod.id("cave_surface"), type);
    }
    private ModWorldGeneration() { }
    public static void register() {

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, EXPERIENCE_ORE_PLACED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, EXPERIENCE_ORE_CAVE_PLACED);
        if (XpOreConfig.get().enableDeepslateVariant()) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.UNDERGROUND_ORES, DEEPSLATE_EXPERIENCE_ORE_PLACED);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.UNDERGROUND_ORES, DEEPSLATE_EXPERIENCE_ORE_CAVE_PLACED);
        }
    }
}
