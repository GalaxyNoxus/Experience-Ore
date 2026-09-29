package dev.galaxynoxus.xpore;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public final class XpTrailParticle extends SpriteBillboardParticle {
    private final float initialAlpha;
    private final float initialScale;

    private XpTrailParticle(ClientWorld world, double x, double y, double z,
                           double alpha, double size, SpriteProvider sprites) {
        super(world, x, y, z);
        this.initialAlpha = (float) Math.clamp(alpha, 0.0, 0.6);
        this.initialScale = (float) Math.clamp(size, 0.01, 0.1);
        this.alpha = initialAlpha;
        this.scale = initialScale;
        this.maxAge = 12;
        this.collidesWithWorld = false;
        setSprite(sprites);
        setColor(1.0F, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        if (++age >= maxAge) {
            markDead();
            return;
        }
        float remaining = 1.0F - (float) age / maxAge;
        alpha = initialAlpha * remaining * remaining;
        scale = initialScale * (0.5F + 0.5F * remaining);
    }

    @Override
    public int getBrightness(float tint) { return 0xF000F0; }

    @Override
    public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }

    public static final class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;
        public Factory(SpriteProvider sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double alpha, double size, double ignored) {
            return new XpTrailParticle(world, x, y, z, alpha, size, sprites);
        }
    }
}
