package kimdog.kimdog_smp.veinminer.qol;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Smart notification system that doesn't spam players
 */
public class SmartNotifications {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Notifications");
    private static final Map<UUID, NotificationTracker> trackers = new HashMap<>();

    private static class NotificationTracker {
        Set<String> shownTips = new HashSet<>();
        long lastTipTime = 0;
        int sessionVeins = 0;
        boolean hasSeenWelcome = false;
    }

    private static NotificationTracker getTracker(UUID uuid) {
        return trackers.computeIfAbsent(uuid, k -> new NotificationTracker());
    }

    /**
     * Show tip only if player hasn't seen it
     */
    public static void showTipOnce(ServerPlayerEntity player, String tipId, String message) {
        NotificationTracker tracker = getTracker(player.getUuid());

        if (!tracker.shownTips.contains(tipId)) {
            player.sendMessage(
                Text.literal("💡 §eTip: §7" + message),
                false
            );
            tracker.shownTips.add(tipId);
            LOGGER.debug("Showed tip '{}' to {}", tipId, player.getName().getString());
        }
    }

    /**
     * Progressive tutorial system
     */
    public static void checkProgressiveTips(ServerPlayerEntity player) {
        NotificationTracker tracker = getTracker(player.getUuid());
        tracker.sessionVeins++;

        // First vein
        if (tracker.sessionVeins == 1) {
            showTipOnce(player, "first_vein",
                "Great! VeinMiner breaks all connected ores at once!");
        }

        // After 5 veins
        if (tracker.sessionVeins == 5) {
            showTipOnce(player, "upgrades",
                "Press §6U §7to open upgrades! Spend emeralds to improve your mining!");
        }

        // After 10 veins
        if (tracker.sessionVeins == 10) {
            showTipOnce(player, "stats",
                "Use §6/kimdog stats §7to see your mining statistics!");
        }

        // After 20 veins
        if (tracker.sessionVeins == 20) {
            showTipOnce(player, "leaderboard",
                "Compete with others! Use §6/kimdog leaderboard §7to see top miners!");
        }
    }

    /**
     * Show welcome only once per player
     */
    public static void showWelcomeIfNew(ServerPlayerEntity player) {
        NotificationTracker tracker = getTracker(player.getUuid());

        if (!tracker.hasSeenWelcome) {
            QoLFeatures.showWelcome(player);
            tracker.hasSeenWelcome = true;
        }
    }

    /**
     * Achievement-style notification
     */
    public static void showAchievement(ServerPlayerEntity player, String title, String description) {
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(
            Text.literal("§6§l✦ ACHIEVEMENT UNLOCKED! ✦")
                .formatted(Formatting.GOLD),
            false
        );
        player.sendMessage(
            Text.literal("§e" + title)
                .formatted(Formatting.YELLOW),
            false
        );
        player.sendMessage(
            Text.literal("§7" + description)
                .formatted(Formatting.GRAY),
            false
        );
    }

    /**
     * Milestone notification with celebration
     */
    public static void celebrateMilestone(ServerPlayerEntity player, String milestone, int reward) {
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(
            Text.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.GOLD),
            false
        );
        player.sendMessage(
            Text.literal("§6§l  🎉 MILESTONE REACHED! 🎉")
                .formatted(Formatting.GOLD),
            false
        );
        player.sendMessage(
            Text.literal("§e  " + milestone)
                .formatted(Formatting.YELLOW),
            false
        );
        player.sendMessage(
            Text.literal("§a  Reward: +" + reward + " Emeralds!")
                .formatted(Formatting.GREEN),
            false
        );
        player.sendMessage(
            Text.literal("§6§l━━━━━━━━━━━━━━━━━━━━━━━━")
                .formatted(Formatting.GOLD),
            false
        );
    }

    /**
     * Warning with severity levels
     */
    public static void showWarning(ServerPlayerEntity player, String message, WarningSeverity severity) {
        String prefix = switch (severity) {
            case LOW -> "§e⚠";
            case MEDIUM -> "§6⚠";
            case HIGH -> "§c⚠";
            case CRITICAL -> "§4§l⚠";
        };

        player.sendMessage(
            Text.literal(prefix + " " + message),
            true
        );
    }

    public enum WarningSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Show help command suggestions
     */
    public static void suggestCommand(ServerPlayerEntity player, String command, String reason) {
        player.sendMessage(
            Text.literal("§7💡 Try §6" + command + " §7" + reason),
            false
        );
    }

    /**
     * Compact info display (action bar)
     */
    public static void showCompactInfo(ServerPlayerEntity player, String info) {
        player.sendMessage(
            Text.literal(info),
            true // Action bar
        );
    }
}
