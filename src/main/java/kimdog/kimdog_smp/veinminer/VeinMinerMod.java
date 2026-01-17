package kimdog.kimdog_smp.veinminer;

import kimdog.kimdog_smp.veinminer.network.VeinMinerNetworking;
import kimdog.kimdog_smp.veinminer.quests.QuestCommands;
import kimdog.kimdog_smp.veinminer.quests.VeinMinerQuests;
import kimdog.kimdog_smp.veinminer.upgrades.UpgradeCommands;
import kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeinMinerMod implements ModInitializer {

    public static final String MOD_ID = "kimdog_veinminer";
    public static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");

    @Override
    public void onInitialize() {
        printBanner();

        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("💎 KimDog VeinMiner - Mod Initialization Started");
        LOGGER.info("═══════════════════════════════════════════════════════════");

        try {
            LOGGER.info("📋 Loading configuration...");
            VeinMinerConfig.load();
            LOGGER.info("✅ Configuration loaded successfully!");

            LOGGER.info("🔗 Registering networking...");
            VeinMinerNetworking.register();
            LOGGER.info("✅ Networking registered!");

            LOGGER.info("⚒️  Registering block break handler...");
            VeinMinerHandler.register();
            LOGGER.info("✅ Block break handler registered!");

            LOGGER.info("🎯 Registering commands...");
            VeinMinerCommands.register();
            QuestCommands.register();
            UpgradeCommands.register();
            LOGGER.info("✅ Commands registered!");

            LOGGER.info("📋 Registering quest system...");
            registerQuestSystem();
            LOGGER.info("✅ Quest system registered!");

            LOGGER.info("═══════════════════════════════════════════════════════════");
            LOGGER.info("✨ KimDog VeinMiner initialized successfully! ✨");
            LOGGER.info("═══════════════════════════════════════════════════════════");
        } catch (Exception e) {
            LOGGER.error("❌ FATAL ERROR during VeinMiner initialization!", e);
            throw new RuntimeException("VeinMiner initialization failed", e);
        }
    }

    private static void registerQuestSystem() {
        // Load quest data and upgrades when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, server, sender) -> {
            java.util.UUID playerUuid = handler.getPlayer().getUuid();
            LOGGER.info("👤 Player joined - Loading VeinMiner data for {}", handler.getPlayer().getName().getString());

            // Load player quests
            VeinMinerQuests.loadQuestData(handler.getPlayer());
            LOGGER.info("📋 Quest data loaded");

            // Load player upgrades
            UpgradeManager.loadUpgrades(playerUuid);
            LOGGER.info("⚙️  Upgrade data loaded");
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            LOGGER.info("📋 VeinMiner systems ready!");
        });
    }

    private static void printBanner() {
        LOGGER.info("\n");
        LOGGER.info("╔═══════════════════════════════════════════════════════════╗");
        LOGGER.info("║                                                           ║");
        LOGGER.info("║        🏔️  KimDog VeinMiner - Mining Made Easy! 🏔️       ║");
        LOGGER.info("║                                                           ║");
        LOGGER.info("║              Break one ore, break them all!               ║");
        LOGGER.info("║                                                           ║");
        LOGGER.info("╚═══════════════════════════════════════════════════════════╝");
        LOGGER.info("\n");
    }
}
