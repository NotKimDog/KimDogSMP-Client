package kimdog.kimdog_smp.veinminer.qol;

import kimdog.kimdog_smp.veinminer.VeinMinerConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-lag system to prevent server performance issues
 */
public class AntiLagSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner AntiLag");
    private static final Map<UUID, LagTracker> trackers = new ConcurrentHashMap<>();

    private static class LagTracker {
        long lastMineTime = 0;
        int blocksLastSecond = 0;
        int consecutiveLargeVeins = 0;
        boolean warningShown = false;
    }

    /**
     * Check if player should be throttled
     */
    public static boolean shouldThrottle(ServerPlayerEntity player, int veinSize) {
        VeinMinerConfig cfg = VeinMinerConfig.get();
        LagTracker tracker = trackers.computeIfAbsent(player.getUuid(), k -> new LagTracker());

        long now = System.currentTimeMillis();
        long timeSinceLastMine = now - tracker.lastMineTime;

        // Reset counter if more than 1 second passed
        if (timeSinceLastMine > 1000) {
            tracker.blocksLastSecond = 0;
            tracker.consecutiveLargeVeins = 0;
            tracker.warningShown = false;
        }

        // Check for spam mining
        if (timeSinceLastMine < 100) { // Less than 100ms between mines
            if (!tracker.warningShown) {
                SmartNotifications.showWarning(player,
                    "Mining too fast! Slow down to prevent lag.",
                    SmartNotifications.WarningSeverity.MEDIUM);
                tracker.warningShown = true;
            }
            return true;
        }

        // Check for very large veins
        if (veinSize > 100) {
            tracker.consecutiveLargeVeins++;
            if (tracker.consecutiveLargeVeins > 3) {
                SmartNotifications.showWarning(player,
                    "Too many large veins! Taking a break to prevent server lag.",
                    SmartNotifications.WarningSeverity.HIGH);
                return true;
            }
        } else {
            tracker.consecutiveLargeVeins = Math.max(0, tracker.consecutiveLargeVeins - 1);
        }

        tracker.lastMineTime = now;
        tracker.blocksLastSecond += veinSize;

        return false;
    }

    /**
     * Optimize vein size for performance
     */
    public static int getOptimizedVeinSize(int requestedSize, ServerPlayerEntity player) {
        VeinMinerConfig cfg = VeinMinerConfig.get();

        // If requested size is too large, warn and cap it
        if (requestedSize > 200) {
            SmartNotifications.showCompactInfo(player,
                "6 Large vein detected! Breaking in optimized batches...");
            LOGGER.info("Capped vein size from {} to 200 for {} (lag prevention)",
                requestedSize, player.getName().getString());
            return 200;
        }

        return requestedSize;
    }

    /**
     * Get recommended delay for vein size
     */
    public static int getRecommendedDelay(int veinSize) {
        if (veinSize < 10) return 25;  // Small vein - fast
        if (veinSize < 30) return 50;  // Medium vein - normal
        if (veinSize < 50) return 75;  // Large vein - slower
        return 100; // Huge vein - slowest for safety
    }

    /**
     * Show performance tip
     */
    public static void showPerformanceTip(ServerPlayerEntity player) {
        SmartNotifications.showTipOnce(player, "performance",
            "Mining huge veins? They'll break slower to prevent server lag!");
    }
}
