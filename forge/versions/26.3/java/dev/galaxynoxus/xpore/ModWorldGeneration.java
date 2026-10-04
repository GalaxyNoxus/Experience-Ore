package dev.galaxynoxus.xpore;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModWorldGeneration {
    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, XpOreMod.MOD_ID);
    public static final RegistryObject<MapCodec<? extends PlacementModifier>> CONFIG_RARITY = PLACEMENT_TYPES.register("config_rarity", () -> ConfigRarityPlacementModifier.CODEC);
    private ModWorldGeneration() { }
}
