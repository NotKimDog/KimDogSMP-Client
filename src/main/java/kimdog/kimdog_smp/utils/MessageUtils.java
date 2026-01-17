package kimdog.kimdog_smp.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Message formatting and display utilities
 * Provides consistent message formatting throughout the mod
 */
public class MessageUtils {

    // Message constants
    public static final String SECTION_DIVIDER = "§7━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    public static final String FULL_DIVIDER = "§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    // Emojis
    public static final String FIRE_EMOJI = "🔥";
    public static final String ALERT_EMOJI = "⚠️";
    public static final String PICKAXE_EMOJI = "⛏";
    public static final String DIAMOND_EMOJI = "💎";
    public static final String EMERALD_EMOJI = "✨";
    public static final String HEART_EMOJI = "💚";
    public static final String STAR_EMOJI = "⭐";

    /**
     * Send a message to player (action bar, false = chat)
     */
    public static void send(ServerPlayerEntity player, String message, boolean actionBar) {
        player.sendMessage(Text.literal(message), actionBar);
    }

    /**
     * Send a message to chat
     */
    public static void sendChat(ServerPlayerEntity player, String message) {
        send(player, message, false);
    }

    /**
     * Send a message to action bar
     */
    public static void sendActionBar(ServerPlayerEntity player, String message) {
        send(player, message, true);
    }

    /**
     * Send a formatted title message
     */
    public static void sendTitle(ServerPlayerEntity player, String title) {
        sendChat(player, "");
        sendChat(player, FULL_DIVIDER);
        sendChat(player, "§6§l" + title);
        sendChat(player, FULL_DIVIDER);
    }

    /**
     * Send a success message (green)
     */
    public static void sendSuccess(ServerPlayerEntity player, String message) {
        sendChat(player, "§a✓ §7" + message);
    }

    /**
     * Send an error message (red)
     */
    public static void sendError(ServerPlayerEntity player, String message) {
        sendChat(player, "§c✗ §7" + message);
    }

    /**
     * Send a warning message (yellow)
     */
    public static void sendWarning(ServerPlayerEntity player, String message) {
        sendChat(player, "§e⚠ §7" + message);
    }

    /**
     * Send an info message (blue)
     */
    public static void sendInfo(ServerPlayerEntity player, String message) {
        sendChat(player, "§b§li §7" + message);
    }

    /**
     * Format a stat line
     */
    public static String formatStat(String label, String value, String color) {
        return "§7" + label + ": " + color + value;
    }

    /**
     * Format a stat line with default color
     */
    public static String formatStat(String label, String value) {
        return formatStat(label, value, "§a");
    }

    /**
     * Create a separator line
     */
    public static String separator() {
        return SECTION_DIVIDER;
    }

    /**
     * Create a full width separator
     */
    public static String fullSeparator() {
        return FULL_DIVIDER;
    }
}
