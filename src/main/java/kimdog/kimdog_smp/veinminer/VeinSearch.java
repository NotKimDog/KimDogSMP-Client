package kimdog.kimdog_smp.veinminer;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

public class VeinSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");

    public static List<BlockPos> search(World world, BlockPos start, BlockState originState, int maxBlocks, int maxRange, Predicate<BlockState> matchPredicate) {
        List<BlockPos> results = new ArrayList<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        LOGGER.info(" Starting vein search from {}, {}, {}", start.getX(), start.getY(), start.getZ());
        LOGGER.info(" Search range: {}, Max blocks: {}", maxRange, maxBlocks);
        LOGGER.info(" Origin block type: {}", originState.getBlock());

        int blocksChecked = 0;
        while (!queue.isEmpty() && results.size() < maxBlocks) {
            BlockPos pos = queue.poll();

            for (int i = 0; i < DirectionOffsets.OFFSETS.length && results.size() < maxBlocks; i++) {
                int[] offset = DirectionOffsets.OFFSETS[i];
                BlockPos offsetPos = pos.add(offset[0], offset[1], offset[2]);

                // Skip if already visited
                if (visited.contains(offsetPos)) continue;

                // Skip if out of range
                if (Math.abs(offsetPos.getX() - start.getX()) > maxRange ||
                    Math.abs(offsetPos.getY() - start.getY()) > maxRange ||
                    Math.abs(offsetPos.getZ() - start.getZ()) > maxRange) continue;

                BlockState state = world.getBlockState(offsetPos);

                // Mark as visited
                visited.add(offsetPos);
                blocksChecked++;

                // If it matches, add to results and queue for further searching
                if (matchPredicate.test(state)) {
                    results.add(offsetPos);
                    queue.add(offsetPos);
                    LOGGER.info(" Found matching block at {}, {}, {}", offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
                }
            }
        }

        LOGGER.info(" Vein search complete! Found {} matching adjacent blocks (checked {} blocks)", results.size(), blocksChecked);
        if (results.isEmpty()) {
            LOGGER.info("  Single block vein - no adjacent matching blocks found");
        }
        return results;
    }

    private static class DirectionOffsets {
        // 26 directions: 6 cardinal + 12 edge diagonals + 8 corner diagonals
        public static final int[][] OFFSETS = new int[][]{
                // Cardinal directions (6)
                {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
                // Edge diagonals (12) - edges of cube
                {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
                {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
                {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1},
                // Corner diagonals (8) - corners of cube
                {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
                {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
        };
    }
}


