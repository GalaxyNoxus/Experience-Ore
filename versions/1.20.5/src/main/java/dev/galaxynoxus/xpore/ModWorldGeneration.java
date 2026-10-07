package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public final class ModWorldGeneration {
    public static final PlacementModifierType<ConfigRarityPlacementModifier> CONFIG_RARITY = registerRarityType();
    public static final PlacementModifierType<CaveSurfacePlacementModifier> CAVE_SURFACE = registerCaveSurfaceType();
    public static final RegistryKey<ConfiguredFeature<?, ?>> EXPERIENCE_ORE_CONFIGURED =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, XpOreMod.id("experience_ore"));
    public static final RegistryKey<ConfiguredFeature<?, ?>> DEEPSLATE_EXPERIENCE_ORE_CONFIGURED =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, XpOreMod.id("deepslate_experience_ore"));
    public static final RegistryKey<PlacedFeature> EXPERIENCE_ORE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, XpOreMod.id("experience_ore"));
    public static final RegistryKey<PlacedFeature> EXPERIENCE_ORE_CAVE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, XpOreMod.id("experience_ore_cave"));
    public static final RegistryKey<PlacedFeature> DEEPSLATE_EXPERIENCE_ORE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, XpOreMod.id("deepslate_experience_ore"));
    public static final RegistryKey<PlacedFeature> DEEPSLATE_EXPERIENCE_ORE_CAVE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, XpOreMod.id("deepslate_experience_ore_cave"));

    private static PlacementModifierType<ConfigRarityPlacementModifier> registerRarityType() {
        PlacementModifierType<ConfigRarityPlacementModifier> type = () -> ConfigRarityPlacementModifier.CODEC;
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, XpOreMod.id("config_rarity"), type);
    }
    private static PlacementModifierType<CaveSurfacePlacementModifier> registerCaveSurfaceType() {
        PlacementModifierType<CaveSurfacePlacementModifier> type = () -> CaveSurfacePlacementModifier.CODEC;
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, XpOreMod.id("cave_surface"), type);
    }
    private ModWorldGeneration() { }
    public static void register() {

        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES, EXPERIENCE_ORE_PLACED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES, EXPERIENCE_ORE_CAVE_PLACED);
        if (XpOreConfig.get().enableDeepslateVariant()) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                    GenerationStep.Feature.UNDERGROUND_ORES, DEEPSLATE_EXPERIENCE_ORE_PLACED);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                    GenerationStep.Feature.UNDERGROUND_ORES, DEEPSLATE_EXPERIENCE_ORE_CAVE_PLACED);
        }
    }
}
