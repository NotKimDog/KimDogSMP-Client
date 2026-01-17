package kimdog.kimdog_smp.veinminer.qol;

import kimdog.kimdog_smp.veinminer.VeinMinerConfig;
import kimdog.kimdog_smp.veinminer.VeinMinerStats;
import kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quality of Life features for VeinMiner
 * Provides helpful messages, tips, stats, and notifications for players
 *
 * Features:
 * - Player tips and tutorials
 * - Statistics display
 * - Tool durability warnings
 * - Welcome messages
 * - Streak notifications
 * - Proximity warnings
 */
public class QoLFeatures {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner QoL");

    // Message formatting constants
    private static final String SECTION_DIVIDER = "7";
    private static final String FULL_DIVIDER = "6l";
    private static final String FIRE_EMOJI = "";
    private static final String ALERT_EMOJI = "";
    private static final String PICKAXE_EMOJI = "";

    /**
     * Show helpful tips to players
     */
    public static void showTips(ServerPlayerEntity player) {
        VeinMinerConfig cfg = VeinMinerConfig.get();

        sendMessage(player, "");
        sendMessage(player, "6l" + PICKAXE_EMOJI + " VEINMINER TIPS " + PICKAXE_EMOJI);
        sendMessage(player, SECTION_DIVIDER);

        // Activation tip
        sendMessage(player, "7Activation: " + getActivationTipMessage(cfg));

        // Feature tips
        if (cfg.consolidateDrops) {
            sendMessage(player, "a 7All drops spawn at origin");
        }
        if (cfg.enableCascadeEffect) {
            sendMessage(player, "a 7Watch blocks break one by one!");
        }
        if (cfg.enableAutoRepair) {
            sendMessage(player, "a 7Tools auto-repair at low durability");
        }
        if (cfg.enableLeaderboards) {
            sendMessage(player, "a 7Use 6/kimdog leaderboard 7to compete!");
        }
        if (cfg.enableDailyRewards) {
            sendMessage(player, "a 7Mine daily for emerald rewards!");
        }

        sendMessage(player, SECTION_DIVIDER);
        sendMessage(player, "7Use 6/kimdog help 7for more commands");
    }

    private static String getActivationTipMessage(VeinMinerConfig cfg) {
        return switch (cfg.activation) {
            case "always" -> "aAlways active - just break ores!";
            case "sneak" -> "eSneak while breaking ores";
            case "toggle" -> "bUse /kimdog veinminer toggle";
            default -> "7Check your config";
        };
    }

    /**
     * Show player their current stats
     */
    public static void showQuickStats(ServerPlayerEntity player) {
        sendMessage(player, "");
        sendMessage(player, "6l" + PICKAXE_EMOJI + " YOUR STATS " + PICKAXE_EMOJI);
        sendMessage(player, SECTION_DIVIDER);

        // Get stats
        VeinMinerStats.PlayerStats stats = VeinMinerStats.getStats(player);
        UpgradeManager.PlayerUpgrades upgrades = UpgradeManager.getPlayerUpgrades(player.getUuid());

        // Display stats
        sendMessage(player, String.format("7Blocks Mined:     a%,d", stats.totalBlocksMined));
        sendMessage(player, String.format("7Total XP:         e%,d", stats.totalXpGained));
        sendMessage(player, String.format("7Current Streak:   c%d %s", stats.currentStreak, FIRE_EMOJI));
        sendMessage(player, String.format("7Best Streak:      6%d %s", stats.bestStreak, FIRE_EMOJI));
        sendMessage(player, String.format("7Largest Vein:     6%d blocks", stats.largestVeinSize));
        sendMessage(player, String.format("7Emeralds:         2%d ", upgrades.emeralds));

        sendMessage(player, SECTION_DIVIDER);
        sendMessage(player, "7Press 6U 7to open upgrade menu");
    }

    /**
     * Auto-notify player of tool durability status
     */
    public static void notifyLowDurability(ServerPlayerEntity player, ItemStack tool) {
        if (!tool.isDamageable()) return;

        int durability = tool.getMaxDamage() - tool.getDamage();
        int maxDurability = tool.getMaxDamage();
        int percent = (durability * 100) / maxDurability;

        if (percent <= 10 && percent > 5) {
            player.sendMessage(
                Text.literal("c Tool durability critical! " + percent + "%"),
                true
            );
        } else if (percent <= 5) {
            player.sendMessage(
                Text.literal("4l TOOL BREAKING! " + percent + "% "),
                true
            );
            if (VeinMinerConfig.get().enableAutoRepair) {
                player.sendMessage(
                    Text.literal("7Auto-repair will activate soon..."),
                    true
                );
            }
        }
    }

    /**
     * Welcome message for new players
     */
    public static void showWelcome(ServerPlayerEntity player) {
        sendMessage(player, "");
        sendMessage(player, FULL_DIVIDER);
        sendMessage(player, "6l    " + PICKAXE_EMOJI + " WELCOME TO VEINMINER! " + PICKAXE_EMOJI);
        sendMessage(player, FULL_DIVIDER);
        sendMessage(player, "");
        sendMessage(player, "7Break an eore block 7to activate!");
        sendMessage(player, "7VeinMiner will mine entire veins at once!");
        sendMessage(player, "");
        sendMessage(player, "aFeatures:");
        sendMessage(player, "  7 bMine all adjacent ores together");
        sendMessage(player, "  7 bEarn emeralds for upgrades");
        sendMessage(player, "  7 bDaily rewards & leaderboards");
        sendMessage(player, "  7 bQuests & achievements");
        sendMessage(player, "");
        sendMessage(player, "7Commands:");
        sendMessage(player, "  6/kimdog help 7- Show all commands");
        sendMessage(player, "  6/kimdog stats 7- View your stats");
        sendMessage(player, "  6Press U 7- Open upgrade menu");
        sendMessage(player, "");
        sendMessage(player, FULL_DIVIDER);
    }

    /**
     * Show combo/streak info to player
     */
    public static void showStreakInfo(ServerPlayerEntity player, int streak) {
        if (streak == 0) return;

        String emoji = "";
        Formatting color = Formatting.YELLOW;
        String title = "Streak";

        if (streak >= 25) {
            emoji = "";
            color = Formatting.GOLD;
            title = "LEGENDARY STREAK";
        } else if (streak >= 15) {
            emoji = "";
            color = Formatting.RED;
            title = "HOT STREAK";
        } else if (streak >= 10) {
            emoji = "";
            color = Formatting.RED;
            title = "Streak";
        }

        player.sendMessage(
            Text.literal(String.format("%s %s: %d veins! %s", emoji, title, streak, emoji))
                .formatted(color),
            true
        );
    }

    /**
     * Proximity warning for dangerous situations
     */
    public static void warnProximity(ServerPlayerEntity player, String danger) {
        player.sendMessage(
            Text.literal(" cWARNING: " + danger + " nearby!")
                .formatted(Formatting.RED),
            true
        );
    }

    /**
     * Helper method to send messages to player
     */
    private static void sendMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message), false);
    }
}
