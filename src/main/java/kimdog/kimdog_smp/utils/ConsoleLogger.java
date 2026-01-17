package kimdog.kimdog_smp.utils;

import org.slf4j.Logger;

/**
 * Beautiful console logging utility for KimDog SMP
 * Provides formatted output with boxes, separators, and emojis
 */
public class ConsoleLogger {

    private static final String BORDER_FULL = "################################################################################";
    private static final String BORDER_SHORT = "#######################################################################";
    private static final String BOX_TOP = "################################################################################";
    private static final String BOX_BOTTOM = "################################################################################";
    private static final String BOX_LINE = "|";

    /**
     * Print a formatted header section
     */
    public static void printHeader(Logger logger, String title, String emoji) {
        logger.info("");
        logger.info(BORDER_SHORT);
        logger.info("");
        logger.info("{} {} {}", emoji, title, emoji);
        logger.info("");
        logger.info(BORDER_SHORT);
        logger.info("");
    }

    /**
     * Print a boxed message
     */
    public static void printBox(Logger logger, String message) {
        logger.info("");
        logger.info(BOX_TOP);
        logger.info("{} {}",BOX_LINE, centerText(message));
        logger.info(BOX_BOTTOM);
        logger.info("");
    }

    /**
     * Print a section with subsections
     */
    public static void printSection(Logger logger, String title, String emoji) {
        logger.info("");
        logger.info("{} {} {}", emoji, title, emoji);
        logger.info("-------------------------------------------");
    }

    /**
     * Print a list item
     */
    public static void printItem(Logger logger, String item, String details) {
        logger.info("  {} {}", item, details);
    }

    /**
     * Print a subsection header
     */
    public static void printSubsection(Logger logger, String text) {
        logger.info("");
        logger.info("▶ {}", text);
    }

    /**
     * Print a divider line
     */
    public static void printDivider(Logger logger) {
        logger.info("");
        logger.info(BORDER_SHORT);
        logger.info("");
    }

    /**
     * Print module initialization
     */
    public static void printModuleStart(Logger logger, String moduleName, String emoji) {
        logger.info("");
        logger.info("{} ==========================================", emoji);
        logger.info("{} Initializing {}...", emoji, moduleName);
        logger.info("{} ==========================================", emoji);
    }

    /**
     * Print module completion
     */
    public static void printModuleComplete(Logger logger, String moduleName, String emoji) {
        logger.info("{} ✅ {} loaded successfully!", emoji, moduleName);
        logger.info("");
    }

    /**
     * Print a success message
     */
    public static void printSuccess(Logger logger, String message, String emoji) {
        logger.info("{}✅ {}", emoji, message);
    }

    /**
     * Print an error message
     */
    public static void printError(Logger logger, String message, String emoji) {
        logger.info("{}❌ {}", emoji, message);
    }

    /**
     * Print an info message
     */
    public static void printInfo(Logger logger, String message, String emoji) {
        logger.info("{}ℹ️  {}", emoji, message);
    }

    /**
     * Center text within 80 characters
     */
    private static String centerText(String text) {
        int totalLength = 76; // Leaving room for border characters
        int textLength = text.length();
        int paddingLeft = (totalLength - textLength) / 2;
        int paddingRight = totalLength - textLength - paddingLeft;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paddingLeft; i++) {
            sb.append(" ");
        }
        sb.append(text);
        for (int i = 0; i < paddingRight; i++) {
            sb.append(" ");
        }
        return "| " + sb.toString() + " |";
    }

    /**
     * Print a complete feature list
     */
    public static void printFeatureList(Logger logger) {
        printSection(logger, "📊 Active Features", "");
        printItem(logger, "⛏️", "VeinMiner        - Mine entire ore veins at once!");
        printItem(logger, "💬", "Chat Messages    - Automatic server announcements");
        printItem(logger, "🚪", "Double Doors     - Open 2 doors/trapdoors together!");
        printItem(logger, "🛡️", "AntiCheat        - Speed Hack | Fly Hack | Reach Hack Detection");
        printItem(logger, "📋", "Quest System     - Daily ore mining quests with rewards!");
    }

    /**
     * Print configuration files info
     */
    public static void printConfigInfo(Logger logger) {
        printSection(logger, "⚙️  Configuration Files", "");
        logger.info("  📁 config/kimdog_smp/");
        logger.info("     ├─ veinminer.json");
        logger.info("     ├─ chatmessages.json");
        logger.info("     ├─ doubledoor.json");
        logger.info("     ├─ anticheat.json");
        logger.info("     └─ quests/");
    }

    /**
     * Print commands info
     */
    public static void printCommandInfo(Logger logger) {
        printSection(logger, "🎮 Commands", "");
        logger.info("  /veinminer help     - VeinMiner commands");
        logger.info("  /veinminer stats    - View your mining stats");
        logger.info("  /quest              - View active quest");
        logger.info("  /quest new          - Generate new quest");
        logger.info("  /chatmessages       - Message system controls");
        logger.info("  /anticheat status   - AntiCheat status");
    }
}
