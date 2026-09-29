package dev.galaxynoxus.xpore;

public final class OrbitMath {
    public static final int COUNT = 3;
    public static final double RADIUS = 1.04;
    public static final int FADE_TICKS = 20;

    private OrbitMath() { }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static double unit(long seed, int salt) {
        long mixed = mix(seed + salt * 0x9E3779B97F4A7C15L);
        return (mixed >>> 40) / (double) (1L << 24);
    }

    public static Offset offset(double ticks, long positionSeed, int slot) {
        long seed = mix(positionSeed);
        double speed = 0.05 + unit(seed, 0) * 0.03;
        double phase = unit(seed, 1 + slot) * Math.PI * 2.0;
        double angle = ticks * speed + phase + slot * Math.PI * 2.0 / COUNT;
        double a = RADIUS * Math.cos(angle);
        double b = RADIUS * Math.sin(angle);
        double x;
        double y;
        double z;
        switch (slot) {
            case 0 -> { x = a; y = 0.0; z = b; }
            case 1 -> { x = a; y = b; z = 0.0; }
            case 2 -> { x = 0.0; y = a; z = b; }
            default -> throw new IllegalArgumentException("Invalid orbit: " + slot);
        }
        double yaw = unit(seed, 4) * Math.PI * 2.0;
        double pitch = (unit(seed, 5) - 0.5) * Math.PI * 0.5;
        return tilt(x, y, z, yaw, pitch);
    }

    private static Offset tilt(double x, double y, double z, double yaw, double pitch) {
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double rx = x * cosYaw - z * sinYaw;
        double rz = x * sinYaw + z * cosYaw;
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double ry = y * cosPitch - rz * sinPitch;
        double finalZ = y * sinPitch + rz * cosPitch;
        return new Offset(rx, ry, finalZ);
    }

    public static float fadeFactor(int ticks) {
        float t = Math.clamp((float) ticks / FADE_TICKS, 0.0F, 1.0F);
        return 1.0F - t * t * (3.0F - 2.0F * t);
    }

    public record Offset(double x, double y, double z) { }
}
