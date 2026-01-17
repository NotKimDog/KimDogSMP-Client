package kimdog.kimdog_smp.veinminer.effects;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreEffects {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");

    public static String getOreType(BlockState state) {
        String name = state.getBlock().toString().toLowerCase();
        if (name.contains("diamond")) return "diamond";
        if (name.contains("emerald")) return "emerald";
        if (name.contains("gold")) return "gold";
        if (name.contains("redstone")) return "redstone";
        if (name.contains("lapis")) return "lapis";
        if (name.contains("iron")) return "iron";
        if (name.contains("coal")) return "coal";
        if (name.contains("copper")) return "copper";
        return "ore";
    }

    public static void spawnOreParticles(ServerWorld world, BlockPos pos, String oreType) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            switch (oreType.toLowerCase()) {
                case "diamond":
                    // Cyan/Blue sparkles for diamond
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, 20, 0.5, 0.5, 0.5, 0.25);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FALLING_WATER, x, y + 0.2, z, 10, 0.3, 0.3, 0.3, 0.15);
                    break;
                case "emerald":
                    // Green sparkles for emerald
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, x, y, z, 15, 0.4, 0.4, 0.4, 0.2);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, 10, 0.3, 0.3, 0.3, 0.2);
                    break;
                case "gold":
                    // Gold/Yellow particles for gold
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, x, y, z, 18, 0.5, 0.5, 0.5, 0.2);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, x, y + 0.1, z, 12, 0.3, 0.3, 0.3, 0.15);
                    break;
                case "redstone":
                    // Red particles for redstone
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FALLING_LAVA, x, y, z, 16, 0.4, 0.4, 0.4, 0.2);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, x, y, z, 8, 0.2, 0.2, 0.2, 0.15);
                    break;
                case "lapis":
                    // Blue particles for lapis
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.RAIN, x, y, z, 14, 0.4, 0.4, 0.4, 0.2);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, 10, 0.3, 0.3, 0.3, 0.2);
                    break;
                case "iron":
                    // Gray/White particles for iron
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, x, y, z, 12, 0.3, 0.3, 0.3, 0.15);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ASH, x, y + 0.1, z, 10, 0.3, 0.3, 0.3, 0.15);
                    break;
                case "coal":
                    // Dark smoke for coal
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, x, y, z, 10, 0.3, 0.3, 0.3, 0.15);
                    break;
                case "copper":
                    // Orange/Brown particles for copper
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, x, y, z, 12, 0.4, 0.4, 0.4, 0.15);
                    break;
                default:
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, 8, 0.3, 0.3, 0.3, 0.15);
            }
        } catch (Exception e) {
            LOGGER.debug("Particle error: {}", e.getMessage());
        }
    }

    public static void playSoundForOre(ServerWorld world, BlockPos pos, String oreType) {
        try {
            float pitch = 1.0f;

            switch (oreType.toLowerCase()) {
                case "diamond":
                case "emerald":
                    // High pitch for rare ores
                    pitch = 1.8f + (float) (Math.random() * 0.3);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK,
                            net.minecraft.sound.SoundCategory.BLOCKS, 1.2f, pitch);
                    break;
                case "gold":
                    // Musical sound for gold
                    pitch = 1.5f + (float) (Math.random() * 0.2);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.8f, pitch);
                    break;
                case "redstone":
                    // Pop sound for redstone
                    pitch = 1.5f + (float) (Math.random() * 0.2);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.6f, pitch);
                    break;
                case "iron":
                    // Metallic sound for iron
                    pitch = 1.2f + (float) (Math.random() * 0.2);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_ANVIL_PLACE,
                            net.minecraft.sound.SoundCategory.BLOCKS, 0.7f, pitch);
                    break;
                default:
                    // Standard stone break
                    pitch = 0.9f + (float) (Math.random() * 0.2);
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK,
                            net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, pitch);
            }
        } catch (Exception e) {
            LOGGER.debug("Sound error: {}", e.getMessage());
        }
    }
}
