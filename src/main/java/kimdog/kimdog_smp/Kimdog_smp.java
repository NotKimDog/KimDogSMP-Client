package kimdog.kimdog_smp;

import kimdog.kimdog_smp.veinminer.VeinMinerMod;
import kimdog.kimdog_smp.chatmessages.ChatMessagesMod;
import kimdog.kimdog_smp.doubledoor.DoubleDoorMod;
import kimdog.kimdog_smp.anticheat.AntiCheatMod;
import kimdog.kimdog_smp.fly.FlyCommands;
import kimdog.kimdog_smp.updater.UpdateChecker;
import kimdog.kimdog_smp.updater.UpdateNotifier;
import kimdog.kimdog_smp.updater.UpdateCommand;
import kimdog.kimdog_smp.web.WebServer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Timer;
import java.util.TimerTask;

public class Kimdog_smp implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP");
    private static final String VERSION = "1.0.0";
    private static final String MC_VERSION = "1.21";

    private static int modulesLoaded = 0;
    private static final int TOTAL_MODULES = 6;
    private static long startTime;

    private static Timer updateCheckTimer;
    private static MinecraftServer currentServer;
    private static final long UPDATE_CHECK_INTERVAL = 15 * 60 * 1000; // 15 minutes in milliseconds

    @Override
    public void onInitialize() {
        startTime = System.currentTimeMillis();
        printMainBanner();
        WebServer.addLog("INFO", "KimDog SMP initializing...");

        try {
            loadModule("Update Checker", "Checking GitHub for latest releases", "[UC]", () -> {
                UpdateChecker.initialize(VERSION);
                UpdateNotifier.initialize();
                UpdateCommand.register();
            });
            WebServer.addLog("INFO", "Update Checker loaded");

            loadModule("Command System", "Registering /fly and admin commands", "[CMD]", FlyCommands::register);
            WebServer.addLog("INFO", "Command System loaded");

            loadModule("VeinMiner", "Initializing ore vein mining mechanics", "[VM]", () -> new VeinMinerMod().onInitialize());
            WebServer.addLog("INFO", "VeinMiner loaded");

            loadModule("Chat Messages", "Loading chat formatting and announcements", "[CHAT]", () -> new ChatMessagesMod().onInitialize());
            WebServer.addLog("INFO", "Chat Messages loaded");

            loadModule("Double Door", "Setting up door mechanics", "[DD]", () -> new DoubleDoorMod().onInitialize());
            WebServer.addLog("INFO", "Double Door loaded");

            loadModule("AntiCheat", "Activating anti-cheat protection", "[AC]", () -> new AntiCheatMod().onInitialize());
            WebServer.addLog("INFO", "AntiCheat loaded");

            long loadTime = System.currentTimeMillis() - startTime;
            printCompletionBanner(loadTime);
            WebServer.addLog("INFO", "All modules loaded in " + loadTime + "ms");

            // Start web server for control panel
            WebServer.start();
            WebServer.addLog("INFO", "Web Control Panel started");

            // Start auto-update checker (15 minute interval)
            startAutoUpdateChecker();
            WebServer.addLog("INFO", "Auto-update checker started");
        } catch (Exception e) {
            LOGGER.error("============================================================");
            LOGGER.error("FATAL ERROR: Mod initialization failed!");
            LOGGER.error("============================================================", e);
            throw new RuntimeException("KimDog SMP initialization failed!", e);
        }
    }

    private static void loadModule(String name, String description, String tag, Runnable initializer) {
        modulesLoaded++;
        LOGGER.info("");
        LOGGER.info("{} +----------------------------------------------------------+", tag);
        LOGGER.info("{} | Loading: {}  [{}/{}]", tag, padRight(name, 35), modulesLoaded, TOTAL_MODULES);
        LOGGER.info("{} | -> {}", tag, description);
        LOGGER.info("{} +----------------------------------------------------------+", tag);

        try {
            long moduleStart = System.currentTimeMillis();
            initializer.run();
            long moduleTime = System.currentTimeMillis() - moduleStart;
            LOGGER.info("{} | OK {} loaded successfully ({} ms)", tag, name, moduleTime);
            LOGGER.info("{} +----------------------------------------------------------+", tag);
        } catch (Exception e) {
            LOGGER.error("{} | FAILED to load {}", tag, name);
            LOGGER.error("{} +----------------------------------------------------------+", tag);
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
        LOGGER.info("============================================================");
        LOGGER.info(" KimDog SMP - Server Enhancement Suite");
        LOGGER.info(" Version: {} | Minecraft: {} | Platform: Fabric", VERSION, MC_VERSION);
        LOGGER.info(" Modules to Load ({}):", TOTAL_MODULES);
        LOGGER.info("   [VM] VeinMiner         - Mine entire ore veins instantly");
        LOGGER.info("   [CHAT] Chat System     - Enhanced chat formatting");
        LOGGER.info("   [DD] Double Doors      - Synchronized door mechanics");
        LOGGER.info("   [AC] AntiCheat         - Server protection");
        LOGGER.info("   [CMD] Command System   - Custom admin commands");
        LOGGER.info("   [UC] Update Checker    - Auto-update detection");
        LOGGER.info("============================================================");
        LOGGER.info("");
    }

    private static void printCompletionBanner(long loadTime) {
        LOGGER.info("");
        LOGGER.info("============================================================");
        LOGGER.info(" ALL MODULES LOADED SUCCESSFULLY");
        LOGGER.info("------------------------------------------------------------");
        LOGGER.info(" Load Statistics:");
        LOGGER.info("   Total Modules Loaded: {} / {}", TOTAL_MODULES, TOTAL_MODULES);
        LOGGER.info("   Total Load Time: {} ms", loadTime);
        LOGGER.info("   Status: OK");
        LOGGER.info("------------------------------------------------------------");
        LOGGER.info(" Available Commands:");
        LOGGER.info("   /fly             - Toggle flight mode");
        LOGGER.info("   /veinminer       - VeinMiner stats");
        LOGGER.info("   /quest           - View quests");
        LOGGER.info("   /chatmessages    - Message controls");
        LOGGER.info("   /anticheat       - AntiCheat controls");
        LOGGER.info("------------------------------------------------------------");
        LOGGER.info(" Web Control Panel:");
        LOGGER.info("   Open browser: http://localhost:8080");
        LOGGER.info("   Dashboard for full server management");
        LOGGER.info("============================================================");
        LOGGER.info(" KimDog SMP is now fully operational!");
        LOGGER.info("============================================================");
        LOGGER.info("");
    }

    private static void startAutoUpdateChecker() {
        updateCheckTimer = new Timer("KimDog-UpdateChecker", true);
        updateCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("[UC] Running scheduled update check...");
                WebServer.addLog("INFO", "[UC] Running scheduled update check...");
                try {
                    // Check if update is available
                    if (UpdateChecker.isUpdateAvailable()) {
                        LOGGER.warn("[UC] Update available! Scheduling server restart...");
                        WebServer.addLog("WARN", "[UC] Update available! Scheduling server restart...");
                        scheduleServerRestart();
                    } else {
                        LOGGER.info("[UC] No updates available. Next check in 15 minutes.");
                        WebServer.addLog("INFO", "[UC] No updates available. Next check in 15 minutes.");
                    }
                } catch (Exception e) {
                    LOGGER.error("[UC] Error checking for updates: ", e);
                    WebServer.addLog("ERROR", "[UC] Error checking for updates: " + e.getMessage());
                }
            }
        }, UPDATE_CHECK_INTERVAL, UPDATE_CHECK_INTERVAL);

        LOGGER.info("[UC] Auto-update checker started (checking every 15 minutes)");
    }

    private static void scheduleServerRestart() {
        // Schedule restart with 10-second warning countdown
        new Thread(() -> {
            try {
                if (currentServer != null) {
                    // Save world
                    LOGGER.warn("[UC] Saving world before restart...");
                    WebServer.addLog("WARN", "[UC] Saving world before restart...");
                    currentServer.getPlayerManager().saveAllPlayerData();

                    // Warn players with 10-second countdown
                    for (int i = 10; i > 0; i--) {
                        String message = "Server updating in " + i + " seconds...";
                        currentServer.getPlayerManager().broadcast(
                            net.minecraft.text.Text.literal(message),
                            false
                        );
                        LOGGER.warn("[UC] " + message);
                        WebServer.addLog("WARN", "[UC] " + message);
                        Thread.sleep(1000);
                    }

                    // Final message and shutdown
                    currentServer.getPlayerManager().broadcast(
                        net.minecraft.text.Text.literal("Server restarting for update!"),
                        false
                    );
                    LOGGER.warn("[UC] Initiating server shutdown for update...");
                    WebServer.addLog("WARN", "[UC] Initiating server shutdown for update...");

                    // Shutdown server
                    currentServer.stop(false);
                }
            } catch (Exception e) {
                LOGGER.error("[UC] Error during restart sequence: ", e);
                WebServer.addLog("ERROR", "[UC] Error during restart sequence: " + e.getMessage());
            }
        }).start();
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
        LOGGER.info("[UC] Server instance registered for auto-update checks");
    }

    public static void stopUpdateChecker() {
        if (updateCheckTimer != null) {
            updateCheckTimer.cancel();
            LOGGER.info("[UC] Update checker stopped");
        }
    }
}

