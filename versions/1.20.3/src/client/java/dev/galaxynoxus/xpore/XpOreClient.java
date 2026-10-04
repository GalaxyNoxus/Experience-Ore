package dev.galaxynoxus.xpore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.render.RenderLayer;

public final class XpOreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EXPERIENCE_ORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEEPSLATE_EXPERIENCE_ORE, RenderLayer.getCutout());
        ParticleFactoryRegistry.getInstance().register(ModParticles.XP_AURA, XpAuraParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.XP_TRAIL, XpTrailParticle.Factory::new);
    }
}
