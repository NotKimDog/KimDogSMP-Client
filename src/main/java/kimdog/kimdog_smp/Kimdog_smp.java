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
    private static final String VERSION = "1.0.6";
    private static final String MC_VERSION = "1.21";

    private static int modulesLoaded = 0;
    private static final int TOTAL_MODULES = 6;
    private static long startTime;

    @Override
    public void onInitialize() {
        startTime = System.currentTimeMillis();
        printMainBanner();

        try {
            loadModule("Update Checker", "Checking GitHub for latest releases", "[UC]", () -> {
                UpdateChecker.initialize(VERSION);
                UpdateNotifier.initialize();
                UpdateCommand.register();
            });

            loadModule("Command System", "Registering /fly and admin commands", "[CMD]", FlyCommands::register);

            loadModule("VeinMiner", "Initializing ore vein mining mechanics & quest system", "[VM]", () -> new VeinMinerMod().onInitialize());

            loadModule("Chat Messages", "Loading custom chat formatting and auto-announcements", "[CHAT]", () -> new ChatMessagesMod().onInitialize());

            loadModule("Double Door", "Setting up synchronized door & trapdoor mechanics", "[DD]", () -> new DoubleDoorMod().onInitialize());

            loadModule("AntiCheat", "Activating anti-cheat protection (Speed/Fly/Reach)", "[AC]", () -> new AntiCheatMod().onInitialize());

            long loadTime = System.currentTimeMillis() - startTime;
            printCompletionBanner(loadTime);
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
        LOGGER.info("   [CHAT] Chat System     - Enhanced chat formatting & announcements");
        LOGGER.info("   [DD] Double Doors      - Synchronized door opening mechanics");
        LOGGER.info("   [AC] AntiCheat         - Server protection & cheat detection");
        LOGGER.info("   [CMD] Command System   - Custom admin & player commands");
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
        LOGGER.info("   /fly             - Toggle flight mode (Admin only)");
        LOGGER.info("   /veinminer       - VeinMiner configuration & stats");
        LOGGER.info("   /quest           - View and manage mining quests");
        LOGGER.info("   /chatmessages    - Message system controls (Admin)");
        LOGGER.info("   /anticheat       - AntiCheat status & controls (Admin)");
        LOGGER.info("============================================================");
        LOGGER.info(" KimDog SMP is now fully operational. Server ready for players!");
        LOGGER.info("============================================================");
        LOGGER.info("");
    }
}