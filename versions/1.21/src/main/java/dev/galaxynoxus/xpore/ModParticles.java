package dev.galaxynoxus.xpore;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    public static final SimpleParticleType XP_AURA = Registry.register(
            Registries.PARTICLE_TYPE, XpOreMod.id("xp_aura"), FabricParticleTypes.simple());
    public static final SimpleParticleType XP_TRAIL = Registry.register(
            Registries.PARTICLE_TYPE, XpOreMod.id("xp_trail"), FabricParticleTypes.simple());
    private ModParticles() { }
    public static void register() { }
}
