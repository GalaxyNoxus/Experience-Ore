package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public final class ModParticles {
    public static final SimpleParticleType XP_AURA = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, XpOreMod.id("xp_aura"), FabricParticleTypes.simple());
    public static final SimpleParticleType XP_TRAIL = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, XpOreMod.id("xp_trail"), FabricParticleTypes.simple());
    private ModParticles() { }
    public static void register() { }
}
