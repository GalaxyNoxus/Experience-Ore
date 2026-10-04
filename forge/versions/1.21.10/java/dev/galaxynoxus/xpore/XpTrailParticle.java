package dev.galaxynoxus.xpore;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public final class XpTrailParticle extends SingleQuadParticle {
    private final float initialAlpha;
    private final float initialScale;

    private XpTrailParticle(ClientLevel level, double x, double y, double z,
                           double alpha, double size, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(level.getRandom()));
        this.initialAlpha = (float) Math.clamp(alpha, 0.0, 0.6);
        this.initialScale = (float) Math.clamp(size, 0.01, 0.1);
        this.alpha = initialAlpha;
        this.quadSize = initialScale;
        this.lifetime = 12;
        this.hasPhysics = false;
        setSprite(sprites.get(level.getRandom()));
        setColor(1.0F, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (++age >= lifetime) {
            remove();
            return;
        }
        float remaining = 1.0F - (float) age / lifetime;
        alpha = initialAlpha * remaining * remaining;
        quadSize = initialScale * (0.5F + 0.5F * remaining);
    }

    @Override
    public int getLightColor(float tint) { return 0xF000F0; }

    @Override
    public Layer getLayer() { return Layer.TRANSLUCENT; }

    public static final class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Factory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double alpha, double size, double ignored, net.minecraft.util.RandomSource random) {
            return new XpTrailParticle(level, x, y, z, alpha, size, sprites);
        }
    }
}
