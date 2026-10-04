package dev.galaxynoxus.xpore;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, XpOreMod.MOD_ID);
    public static final RegistryObject<SimpleParticleType> XP_AURA = PARTICLES.register("xp_aura", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> XP_TRAIL = PARTICLES.register("xp_trail", () -> new SimpleParticleType(false));
    private ModParticles() { }
}
