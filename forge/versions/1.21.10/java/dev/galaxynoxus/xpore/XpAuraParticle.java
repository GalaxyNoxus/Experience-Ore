package dev.galaxynoxus.xpore;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.BlockPos;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class XpAuraParticle extends SingleQuadParticle {
    private final double centerX;
    private final double centerZ;
    private final double centerY;
    private final BlockPos source;
    private final int slot;
    private long keepAliveUntil;
    private int fadeTicks = -1;
    private float fadeStartAlpha;

    private XpAuraParticle(ClientLevel level, double centerX, double centerY, double centerZ,
                          int slot, SpriteSet sprites) {
        super(level, centerX, centerY, centerZ, sprites.get(level.getRandom()));
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.source = BlockPos.containing(centerX, centerY, centerZ);
        this.slot = slot;
        this.lifetime = Integer.MAX_VALUE;
        this.quadSize = 0.075F;
        this.alpha = 0.0F;
        this.hasPhysics = false;
        this.keepAliveUntil = level.getGameTime() + 80;
        OrbitMath.Offset offset = OrbitMath.offset(level.getGameTime(), source.asLong(), slot);
        setPos(centerX + offset.x(), centerY + offset.y(), centerZ + offset.z());
        xo = x;
        yo = y;
        zo = z;
        setSprite(sprites.get(level.getRandom()));
        setColor(1.0F, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        age++;
        if (fadeTicks < 0 && (level.getGameTime() > keepAliveUntil
                || !(level.getBlockState(source).getBlock() instanceof AbstractExperienceOreBlock))) {
            fadeTicks = 0;
            fadeStartAlpha = 0.9F * Math.min(1.0F, age / 6.0F);
        }
        float opacity = 0.9F * Math.min(1.0F, age / 6.0F);
        if (fadeTicks >= 0) {
            opacity = fadeStartAlpha * OrbitMath.fadeFactor(fadeTicks++);
            if (fadeTicks > OrbitMath.FADE_TICKS) {
                remove();
                return;
            }
        }
        OrbitMath.Offset offset = OrbitMath.offset(level.getGameTime(), source.asLong(), slot);
        setPos(centerX + offset.x(), centerY + offset.y(), centerZ + offset.z());
        BlockPos sample = BlockPos.containing(x, y, z);
        alpha = level.getBlockState(sample).isSolidRender() ? 0.0F : opacity;
        if (alpha > 0.01F) {
            for (int segment = 1; segment <= 2; segment++) {
                OrbitMath.Offset trail = OrbitMath.offset(
                        level.getGameTime() - segment * 0.5, source.asLong(), slot);
                double tx = centerX + trail.x();
                double ty = centerY + trail.y();
                double tz = centerZ + trail.z();
                BlockPos trailPos = BlockPos.containing(tx, ty, tz);
                if (!level.getBlockState(trailPos).isSolidRender()) {
                    level.addParticle(ModParticles.XP_TRAIL.get(), tx, ty, tz, opacity * 0.55, 0.047, 0.0);
                }
            }
        }
    }

    @Override
    public int getLightColor(float tint) { return 0xF000F0; }

    @Override
    public Layer getLayer() { return Layer.TRANSLUCENT; }

    public static final class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Map<OrbitKey, WeakReference<XpAuraParticle>> active = new HashMap<>();
        private WeakReference<ClientLevel> activeWorld = new WeakReference<>(null);
        private int requests;

        public Factory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double centerX, double centerY, double centerZ,
                                       double orbitIndex, double unusedY, double unusedZ, net.minecraft.util.RandomSource random) {
            if (activeWorld.get() != level) {
                active.clear();
                activeWorld = new WeakReference<>(level);
            }
            if ((requests++ & 63) == 0) {
                active.entrySet().removeIf(entry -> {
                    XpAuraParticle p = entry.getValue().get();
                    return p == null || !p.isAlive();
                });
            }
            int slot = (int) orbitIndex;
            if (slot < 0 || slot >= OrbitMath.COUNT) { return null; }
            BlockPos source = BlockPos.containing(centerX, centerY, centerZ);
            OrbitKey key = new OrbitKey(source, slot);
            WeakReference<XpAuraParticle> reference = active.get(key);
            XpAuraParticle existing = reference == null ? null : reference.get();
            if (existing != null && existing.isAlive()) {
                if (existing.fadeTicks < 0) { existing.keepAliveUntil = level.getGameTime() + 80; }
                return null;
            }
            XpAuraParticle particle = new XpAuraParticle(level, centerX, centerY, centerZ, slot, sprites);
            active.put(key, new WeakReference<>(particle));
            return particle;
        }

        private record OrbitKey(BlockPos source, int slot) { }
    }
}
