package kimdog.kimdog_smp;

import kimdog.kimdog_smp.veinminer.VeinMinerMod;
import kimdog.kimdog_smp.chatmessages.ChatMessagesMod;
import kimdog.kimdog_smp.doubledoor.DoubleDoorMod;
import kimdog.kimdog_smp.anticheat.AntiCheatMod;
import kimdog.kimdog_smp.fly.FlyCommands;
import kimdog.kimdog_smp.updater.UpdateChecker;
import kimdog.kimdog_smp.updater.UpdateNotifier;
import kimdog.kimdog_smp.updater.UpdateCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kimdog_smp implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP");
    private static final String VERSION = "1.0.2";
    private static final String MC_VERSION = "1.21";

    private static int modulesLoaded = 0;
    private static final int TOTAL_MODULES = 6;
    private static long startTime;

    @Override
    public void onInitialize() {
        startTime = System.currentTimeMillis();
        printMainBanner();

        try {
            // Update Checker (runs asynchronously)
            loadModule("Update Checker", "Checking GitHub for latest releases", "ðŸ”„", () -> {
                UpdateChecker.initialize(VERSION);
                UpdateNotifier.initialize();
                UpdateCommand.register();
            });

            // Register commands
            loadModule("Command System", "Registering /fly and admin commands", "ðŸŽ®", () -> {
                FlyCommands.register();
            });

            // VeinMiner
            loadModule("VeinMiner", "Initializing ore vein mining mechanics & quest system", "â›ï¸", () -> {
                new VeinMinerMod().onInitialize();
            });

            // Chat Messages
            loadModule("Chat Messages", "Loading custom chat formatting and auto-announcements", "ðŸ’¬", () -> {
                new ChatMessagesMod().onInitialize();
            });

            // Double Door
            loadModule("Double Door", "Setting up synchronized door & trapdoor mechanics", "ðŸšª", () -> {
                new DoubleDoorMod().onInitialize();
            });

            // AntiCheat
            loadModule("AntiCheat", "Activating anti-cheat protection (Speed/Fly/Reach)", "ðŸ›¡ï¸", () -> {
                new AntiCheatMod().onInitialize();
            });

            long loadTime = System.currentTimeMillis() - startTime;
            printCompletionBanner(loadTime);
        } catch (Exception e) {
            LOGGER.error("");
            LOGGER.error("################################################################################");
            LOGGER.error("#                                                                              #");
            LOGGER.error("#  âŒ FATAL ERROR: Mod initialization failed!                                  #");
            LOGGER.error("#                                                                              #");
            LOGGER.error("################################################################################");
            LOGGER.error("");
            LOGGER.error("Stack trace:", e);
            throw new RuntimeException("KimDog SMP initialization failed!", e);
        }
    }

    private static void loadModule(String name, String description, String icon, Runnable initializer) {
        modulesLoaded++;
        LOGGER.info("");
        LOGGER.info("{} +-------------------------------------------------------------------+", icon);
        LOGGER.info("{} | Loading: {}  [{}/{}]", icon, padRight(name, 41), modulesLoaded, TOTAL_MODULES);
        LOGGER.info("{} | -> {}", icon, description);
        LOGGER.info("{} +-------------------------------------------------------------------+", icon);

        try {
            long moduleStart = System.currentTimeMillis();
            initializer.run();
            long moduleTime = System.currentTimeMillis() - moduleStart;
            LOGGER.info("{} | âœ… {} loaded successfully! ({}ms)", icon, name, moduleTime);
            LOGGER.info("{} +-------------------------------------------------------------------+", icon);
        } catch (Exception e) {
            LOGGER.error("{} | âŒ Failed to load {}", icon, name);
            LOGGER.error("{} +-------------------------------------------------------------------+", icon);
            throw e;
        }
    }

    private static String padRight(String text, int length) {
        if (text.length() >= length) return text;
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private static void printMainBanner() {
        LOGGER.info("");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#                   â–ˆâ–ˆâ•—  â–ˆâ–ˆâ•—â–ˆâ–ˆâ•—â–ˆâ–ˆâ–ˆâ•—   â–ˆâ–ˆâ–ˆâ•—â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•—  â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•—  â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•—          #");
        LOGGER.info("#                   â–ˆâ–ˆâ•‘ â–ˆâ–ˆâ•”â•â–ˆâ–ˆâ•‘â–ˆâ–ˆâ–ˆâ–ˆâ•— â–ˆâ–ˆâ–ˆâ–ˆâ•‘â–ˆâ–ˆâ•”â•â•â–ˆâ–ˆâ•—â–ˆâ–ˆâ•”â•â•â•â–ˆâ–ˆâ•—â–ˆâ–ˆâ•”â•â•â•â•â•          #");
        LOGGER.info("#                   â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•”â• â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•”â–ˆâ–ˆâ–ˆâ–ˆâ•”â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘  â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘   â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘  â–ˆâ–ˆâ–ˆâ•—         #");
        LOGGER.info("#                   â–ˆâ–ˆâ•”â•â–ˆâ–ˆâ•— â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘â•šâ–ˆâ–ˆâ•”â•â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘  â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘   â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘   â–ˆâ–ˆâ•‘         #");
        LOGGER.info("#                   â–ˆâ–ˆâ•‘  â–ˆâ–ˆâ•—â–ˆâ–ˆâ•‘â–ˆâ–ˆâ•‘ â•šâ•â• â–ˆâ–ˆâ•‘â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•”â•â•šâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•”â•â•šâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ•”â•         #");
        LOGGER.info("#                   â•šâ•â•  â•šâ•â•â•šâ•â•â•šâ•â•     â•šâ•â•â•šâ•â•â•â•â•â•  â•šâ•â•â•â•â•â•  â•šâ•â•â•â•â•â•          #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#                      ðŸŽ® SMP - Server Enhancement Suite ðŸŽ®                   #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  Version: {}  |  Minecraft: {}  |  Platform: Fabric                    #", VERSION, MC_VERSION);
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  ðŸ“¦ Modules to Load:                                                         #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#    â›ï¸  VeinMiner        - Mine entire ore veins instantly                   #");
        LOGGER.info("#    ðŸ’¬ Chat System      - Enhanced chat formatting & announcements          #");
        LOGGER.info("#    ðŸšª Double Doors     - Synchronized door opening mechanics               #");
        LOGGER.info("#    ðŸ›¡ï¸  AntiCheat        - Server protection & cheat detection               #");
        LOGGER.info("#    ðŸŽ® Command System   - Custom admin & player commands                    #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("");
        LOGGER.info("ðŸš€ Starting Module Initialization...");
        LOGGER.info("");
    }

    private static void printCompletionBanner(long loadTime) {
        LOGGER.info("");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#                  âœ¨ ALL MODULES LOADED SUCCESSFULLY! âœ¨                     #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  ðŸ“Š Load Statistics:                                                         #");
        LOGGER.info("#     â€¢ Total Modules Loaded: {} / {}                                         #", TOTAL_MODULES, TOTAL_MODULES);
        LOGGER.info("#     â€¢ Total Load Time: {}ms                                              #", padRight(loadTime + "", 5));
        LOGGER.info("#     â€¢ Status: âœ… Operational                                                 #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  â„¹ï¸  Configuration:                                                          #");
        LOGGER.info("#     â€¢ Config Directory: config/kimdog_smp/                                  #");
        LOGGER.info("#     â€¢ Logs Directory: logs/                                                 #");
        LOGGER.info("#     â€¢ Check logs for module-specific settings and details                   #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  ðŸŽ® Available Commands:                                                      #");
        LOGGER.info("#     â€¢ /fly              - Toggle flight mode (Admin only)                   #");
        LOGGER.info("#     â€¢ /veinminer        - VeinMiner configuration & stats                   #");
        LOGGER.info("#     â€¢ /quest            - View and manage mining quests                     #");
        LOGGER.info("#     â€¢ /chatmessages     - Message system controls (Admin)                   #");
        LOGGER.info("#     â€¢ /anticheat        - AntiCheat status & controls (Admin)               #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("#                                                                              #");
        LOGGER.info("#  ðŸ’¡ Tips:                                                                    #");
        LOGGER.info("#     â€¢ Hold Shift while mining to activate VeinMiner                         #");
        LOGGER.info("#     â€¢ Check /quest daily for mining challenges                              #");
        LOGGER.info("#     â€¢ AntiCheat runs automatically in the background                        #");
        LOGGER.info("#                                                                              #");
        LOGGER.info("################################################################################");
        LOGGER.info("");
        LOGGER.info("â­ KimDog SMP is now fully operational! Server ready for players! â­");
        LOGGER.info("");
    }
}
