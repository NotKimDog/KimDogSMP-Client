package kimdog.kimdog_smp.utils;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Particle effect utilities for VeinMiner
 * Provides convenient methods for spawning particle effects
 */
public class ParticleUtils {

    /**
     * Spawn particles at a block position
     */
    public static void spawnParticles(ServerWorld world, ParticleEffect particle, BlockPos pos, int count) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        world.spawnParticles(particle, x, y, z, count, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Spawn particles at a Vec3d position
     */
    public static void spawnParticles(ServerWorld world, ParticleEffect particle, Vec3d pos, int count) {
        world.spawnParticles(particle, pos.x, pos.y, pos.z, count, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Spawn ore break particles (default Minecraft ore breaking)
     */
    public static void spawnOreBreakParticles(ServerWorld world, BlockPos pos) {
        spawnParticles(world, ParticleTypes.EXPLOSION, pos, 5);
    }

    /**
     * Spawn sparkle particles for visual effect
     */
    public static void spawnSparkles(ServerWorld world, BlockPos pos, int count) {
        spawnParticles(world, ParticleTypes.END_ROD, pos, count);
    }

    /**
     * Spawn flame particles
     */
    public static void spawnFlames(ServerWorld world, BlockPos pos, int count) {
        spawnParticles(world, ParticleTypes.FLAME, pos, count);
    }

    /**
     * Spawn smoke particles
     */
    public static void spawnSmoke(ServerWorld world, BlockPos pos, int count) {
        spawnParticles(world, ParticleTypes.SMOKE, pos, count);
    }

    /**
     * Spawn a cascading particle effect (blocks breaking in sequence)
     */
    public static void spawnCascadeEffect(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Spawn explosion particles with variation
        for (int i = 0; i < 3; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            world.spawnParticles(
                ParticleTypes.EXPLOSION,
                x + offsetX,
                y + offsetY,
                z + offsetZ,
                1,
                0.2, 0.2, 0.2,
                0.1
            );
        }
    }

    /**
     * Spawn a trail of particles between two points
     */
    public static void spawnParticleTrail(ServerWorld world, Vec3d start, Vec3d end, ParticleEffect particle, int steps) {
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double x = start.x + (end.x - start.x) * progress;
            double y = start.y + (end.y - start.y) * progress;
            double z = start.z + (end.z - start.z) * progress;

            world.spawnParticles(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Spawn success particles (green)
     */
    public static void spawnSuccessParticles(ServerWorld world, BlockPos pos) {
        spawnParticles(world, ParticleTypes.HAPPY_VILLAGER, pos, 8);
    }

    /**
     * Spawn error particles (red)
     */
    public static void spawnErrorParticles(ServerWorld world, BlockPos pos) {
        spawnParticles(world, ParticleTypes.DAMAGE_INDICATOR, pos, 8);
    }
}
