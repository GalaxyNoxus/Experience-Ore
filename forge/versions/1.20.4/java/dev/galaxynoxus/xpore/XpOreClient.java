package dev.galaxynoxus.xpore;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

@Mod.EventBusSubscriber(modid = XpOreMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class XpOreClient {
    private XpOreClient() { }
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.XP_AURA.get(), XpAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.XP_TRAIL.get(), XpTrailParticle.Factory::new);
    }
}
