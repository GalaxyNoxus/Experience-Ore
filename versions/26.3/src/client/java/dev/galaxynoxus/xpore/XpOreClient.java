package dev.galaxynoxus.xpore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public final class XpOreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleProviderRegistry.getInstance().register(ModParticles.XP_AURA, XpAuraParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.XP_TRAIL, XpTrailParticle.Factory::new);
    }
}
