package dev.galaxynoxus.xpore;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class XpAuraParticle extends SpriteBillboardParticle {
    private final double centerX;
    private final double centerZ;
    private final double centerY;
    private final BlockPos source;
    private final int slot;
    private long keepAliveUntil;
    private int fadeTicks = -1;
    private float fadeStartAlpha;

    private XpAuraParticle(ClientWorld world, double centerX, double centerY, double centerZ,
                          int slot, SpriteProvider sprites) {
        super(world, centerX, centerY, centerZ);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.source = BlockPos.ofFloored(centerX, centerY, centerZ);
        this.slot = slot;
        this.maxAge = Integer.MAX_VALUE;
        this.scale = 0.075F;
        this.alpha = 0.0F;
        this.collidesWithWorld = false;
        this.keepAliveUntil = world.getTime() + 80;
        OrbitMath.Offset offset = OrbitMath.offset(world.getTime(), source.asLong(), slot);
        setPos(centerX + offset.x(), centerY + offset.y(), centerZ + offset.z());
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        setSprite(sprites);
        setColor(1.0F, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        age++;
        if (fadeTicks < 0 && (world.getTime() > keepAliveUntil
                || !(world.getBlockState(source).getBlock() instanceof AbstractExperienceOreBlock))) {
            fadeTicks = 0;
            fadeStartAlpha = 0.9F * Math.min(1.0F, age / 6.0F);
        }
        float opacity = 0.9F * Math.min(1.0F, age / 6.0F);
        if (fadeTicks >= 0) {
            opacity = fadeStartAlpha * OrbitMath.fadeFactor(fadeTicks++);
            if (fadeTicks > OrbitMath.FADE_TICKS) {
                markDead();
                return;
            }
        }
        OrbitMath.Offset offset = OrbitMath.offset(world.getTime(), source.asLong(), slot);
        setPos(centerX + offset.x(), centerY + offset.y(), centerZ + offset.z());
        BlockPos sample = BlockPos.ofFloored(x, y, z);
        alpha = world.getBlockState(sample).isOpaqueFullCube(world, sample) ? 0.0F : opacity;
        if (alpha > 0.01F) {
            for (int segment = 1; segment <= 2; segment++) {
                OrbitMath.Offset trail = OrbitMath.offset(
                        world.getTime() - segment * 0.5, source.asLong(), slot);
                double tx = centerX + trail.x();
                double ty = centerY + trail.y();
                double tz = centerZ + trail.z();
                BlockPos trailPos = BlockPos.ofFloored(tx, ty, tz);
                if (!world.getBlockState(trailPos).isOpaqueFullCube(world, trailPos)) {
                    world.addParticle(ModParticles.XP_TRAIL, tx, ty, tz, opacity * 0.55, 0.047, 0.0);
                }
            }
        }
    }

    @Override
    public int getBrightness(float tint) { return 0xF000F0; }

    @Override
    public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }

    public static final class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;
        private final Map<OrbitKey, WeakReference<XpAuraParticle>> active = new HashMap<>();
        private WeakReference<ClientWorld> activeWorld = new WeakReference<>(null);
        private int requests;

        public Factory(SpriteProvider sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world,
                                       double centerX, double centerY, double centerZ,
                                       double orbitIndex, double unusedY, double unusedZ) {
            if (activeWorld.get() != world) {
                active.clear();
                activeWorld = new WeakReference<>(world);
            }
            if ((requests++ & 63) == 0) {
                active.entrySet().removeIf(entry -> {
                    XpAuraParticle p = entry.getValue().get();
                    return p == null || !p.isAlive();
                });
            }
            int slot = (int) orbitIndex;
            if (slot < 0 || slot >= OrbitMath.COUNT) { return null; }
            BlockPos source = BlockPos.ofFloored(centerX, centerY, centerZ);
            OrbitKey key = new OrbitKey(source, slot);
            WeakReference<XpAuraParticle> reference = active.get(key);
            XpAuraParticle existing = reference == null ? null : reference.get();
            if (existing != null && existing.isAlive()) {
                if (existing.fadeTicks < 0) { existing.keepAliveUntil = world.getTime() + 80; }
                return null;
            }
            XpAuraParticle particle = new XpAuraParticle(world, centerX, centerY, centerZ, slot, sprites);
            active.put(key, new WeakReference<>(particle));
            return particle;
        }

        private record OrbitKey(BlockPos source, int slot) { }
    }
}
