package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Advanced visual effects for VeinMiner
 */
public class VisualEffects {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Effects");
    private static final Random RANDOM = new Random();

    public static void spawnRainbowParticles(ServerWorld world, BlockPos pos, int count) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < count; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.8;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 0.8;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.8;

            // Cycle through different particle types for rainbow effect
            ParticleEffect particle = switch (i % 7) {
                case 0 -> ParticleTypes.FLAME;
                case 1 -> ParticleTypes.SOUL_FIRE_FLAME;
                case 2 -> ParticleTypes.END_ROD;
                case 3 -> ParticleTypes.ENCHANT;
                case 4 -> ParticleTypes.HAPPY_VILLAGER;
                case 5 -> ParticleTypes.TOTEM_OF_UNDYING;
                default -> ParticleTypes.GLOW;
            };

            world.spawnParticles(
                particle,
                x + offsetX, y + offsetY, z + offsetZ,
                1, 0.0, 0.1, 0.0, 0.02
            );
        }
    }

    public static void spawnOreSpecificParticles(ServerWorld world, BlockPos pos, String oreType) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ParticleEffect particle = switch (oreType.toLowerCase()) {
            case "diamond" -> ParticleTypes.END_ROD; // Blue/white
            case "emerald" -> ParticleTypes.HAPPY_VILLAGER; // Green
            case "gold" -> ParticleTypes.FLAME; // Yellow/orange
            case "iron" -> ParticleTypes.CRIT; // Gray
            case "coal" -> ParticleTypes.SMOKE; // Black
            case "redstone" -> ParticleTypes.FLAME; // Red (changed from DUST)
            case "lapis" -> ParticleTypes.ENCHANT; // Blue
            case "copper" -> ParticleTypes.WAX_OFF; // Orange
            case "quartz" -> ParticleTypes.GLOW; // White
            default -> ParticleTypes.EXPLOSION;
        };

        world.spawnParticles(particle, x, y, z, 15, 0.3, 0.3, 0.3, 0.05);
    }

    public static void spawnExplosionEffect(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Large explosion visual
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.FLAME, x, y, z, 50, 1.0, 1.0, 1.0, 0.1);
        world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 30, 0.5, 0.5, 0.5, 0.05);

        // Sound effect
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.5f, 1.0f);
    }

    public static void spawnLightningEffect(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Lightning strike particles
        for (int i = 0; i < 100; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            world.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                x + offsetX, y + i * 0.1, z + offsetZ,
                1, 0.1, 0.1, 0.1, 0.1
            );
        }

        // Thunder sound
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public static void spawnTrailEffect(ServerWorld world, BlockPos from, BlockPos to) {
        Vec3d start = Vec3d.of(from).add(0.5, 0.5, 0.5);
        Vec3d end = Vec3d.of(to).add(0.5, 0.5, 0.5);
        Vec3d direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        for (double d = 0; d < distance; d += 0.2) {
            Vec3d pos = start.add(direction.multiply(d));
            world.spawnParticles(
                ParticleTypes.END_ROD,
                pos.x, pos.y, pos.z,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    public static void spawnCascadeEffect(ServerWorld world, BlockPos center, int wave) {
        double radius = wave * 1.5;
        int particles = wave * 10;

        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            double x = center.getX() + 0.5 + Math.cos(angle) * radius;
            double y = center.getY() + 0.5 + (RANDOM.nextDouble() - 0.5);
            double z = center.getZ() + 0.5 + Math.sin(angle) * radius;

            world.spawnParticles(
                ParticleTypes.GLOW,
                x, y, z,
                1, 0.0, 0.1, 0.0, 0.02
            );
        }
    }

    public static void playOreSound(ServerWorld world, BlockPos pos, String oreType) {
        // Play appropriate sound based on ore type
        float pitch = 0.8f + RANDOM.nextFloat() * 0.4f; // Random pitch 0.8-1.2
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        switch (oreType.toLowerCase()) {
            case "diamond" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.8f, pitch);
            case "emerald" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 0.8f, pitch);
            case "gold" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 0.8f, pitch);
            case "iron" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.8f, pitch);
            case "redstone" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS, 0.8f, pitch);
            case "lapis" ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.8f, pitch);
            default ->
                world.playSound(null, x, y, z, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.8f, pitch);
        }
    }

    public static void spawnComboParticles(ServerWorld world, BlockPos pos, int comboLevel) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.5;
        double z = pos.getZ() + 0.5;

        ParticleEffect particle = comboLevel >= 10 ? ParticleTypes.TOTEM_OF_UNDYING :
                                  comboLevel >= 5 ? ParticleTypes.ENCHANT :
                                  ParticleTypes.HAPPY_VILLAGER;

        int count = Math.min(comboLevel * 2, 50);
        world.spawnParticles(particle, x, y, z, count, 0.5, 0.5, 0.5, 0.1);
    }
}
