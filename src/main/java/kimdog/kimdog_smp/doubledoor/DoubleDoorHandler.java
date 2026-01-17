package kimdog.kimdog_smp.doubledoor;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleDoorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog DoubleDoor");

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Server-side only check - return immediately if on client
            if (!(world instanceof net.minecraft.server.world.ServerWorld)) return ActionResult.PASS;
            if (hand.toString().equals("OFF_HAND")) return ActionResult.PASS;

            DoubleDoorConfig config = DoubleDoorConfig.get();
            if (!config.enabled) return ActionResult.PASS;

            if (config.requireSneak && !player.isSneaking()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();

            if (!(block instanceof DoorBlock || block instanceof TrapdoorBlock)) {
                return ActionResult.PASS;
            }

            BlockPos[] nearbyPositions = getNearbyDoors(world, pos, config.maxDistance);

            if (nearbyPositions.length > 0) {
                LOGGER.info("🚪 Double Door activated at {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());

                toggleDoor(world, pos);
                for (BlockPos nearbyPos : nearbyPositions) {
                    toggleDoor(world, nearbyPos);
                }

                if (config.playSound) {
                    playSoundEffect(world, pos, config.soundVolume);
                }
                if (config.spawnParticles) {
                    spawnParticles(world, pos, config.particleCount);
                }

                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    private static BlockPos[] getNearbyDoors(net.minecraft.world.World world, BlockPos pos, int maxDistance) {
        java.util.List<BlockPos> nearby = new java.util.ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;

            for (int i = 1; i <= maxDistance; i++) {
                BlockPos checkPos = pos.offset(direction, i);
                Block block = world.getBlockState(checkPos).getBlock();

                if (block instanceof DoorBlock || block instanceof TrapdoorBlock) {
                    nearby.add(checkPos);
                    break;
                } else if (!(block instanceof net.minecraft.block.AirBlock)) {
                    break;
                }
            }
        }

        return nearby.toArray(new BlockPos[0]);
    }

    private static void toggleDoor(net.minecraft.world.World world, BlockPos pos) {
        try {
            var state = world.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapdoorBlock) {
                boolean isOpen = state.get(Properties.OPEN);
                world.setBlockState(pos, state.with(Properties.OPEN, !isOpen));
                LOGGER.debug("🚪 Door toggled at {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error toggling door: {}", e.getMessage());
        }
    }

    private static void playSoundEffect(net.minecraft.world.World world, BlockPos pos, float volume) {
        try {
            if (world instanceof net.minecraft.server.world.ServerWorld) {
                ((net.minecraft.server.world.ServerWorld) world).playSound(
                    null, pos,
                    net.minecraft.sound.SoundEvents.BLOCK_WOODEN_DOOR_OPEN,
                    net.minecraft.sound.SoundCategory.BLOCKS,
                    volume,
                    1.0f + (float)(Math.random() * 0.2 - 0.1)
                );
            }
        } catch (Exception e) {
            LOGGER.debug("Sound error: {}", e.getMessage());
        }
    }

    private static void spawnParticles(net.minecraft.world.World world, BlockPos pos, int count) {
        try {
            if (world instanceof net.minecraft.server.world.ServerWorld) {
                ((net.minecraft.server.world.ServerWorld) world).spawnParticles(
                    net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    count, 0.3, 0.3, 0.3, 0.1
                );
            }
        } catch (Exception e) {
            LOGGER.debug("Particle error: {}", e.getMessage());
        }
    }
}
