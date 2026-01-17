package kimdog.kimdog_smp.anticheat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntiCheatMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog AntiCheat");

    public void onInitialize() {
        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("🛡️  KimDog AntiCheat - Initializing...");
        LOGGER.info("═══════════════════════════════════════════════════════════");

        // Load config
        AntiCheatConfig.load();

        // Register detection engine
        AntiCheatEngine.register();
        LOGGER.info("✅ AntiCheat engine registered");

        // Register commands
        AntiCheatCommands.register();
        LOGGER.info("✅ AntiCheat commands registered");

        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("✨ AntiCheat initialized successfully!");
        LOGGER.info("🛡️  Detection Systems: Speed Hack | Fly Hack | Reach Hack");
        LOGGER.info("═══════════════════════════════════════════════════════════");
    }
}
