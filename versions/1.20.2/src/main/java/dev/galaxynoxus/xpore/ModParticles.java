package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    public static final DefaultParticleType XP_AURA = Registry.register(
            Registries.PARTICLE_TYPE, XpOreMod.id("xp_aura"), FabricParticleTypes.simple());
    public static final DefaultParticleType XP_TRAIL = Registry.register(
            Registries.PARTICLE_TYPE, XpOreMod.id("xp_trail"), FabricParticleTypes.simple());
    private ModParticles() { }
    public static void register() { }
}
