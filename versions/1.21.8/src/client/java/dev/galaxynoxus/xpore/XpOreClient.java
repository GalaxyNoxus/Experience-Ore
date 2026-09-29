package dev.galaxynoxus.xpore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.render.BlockRenderLayer;

public final class XpOreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.EXPERIENCE_ORE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.DEEPSLATE_EXPERIENCE_ORE, BlockRenderLayer.CUTOUT);
        ParticleFactoryRegistry.getInstance().register(ModParticles.XP_AURA, XpAuraParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.XP_TRAIL, XpTrailParticle.Factory::new);
    }
}
