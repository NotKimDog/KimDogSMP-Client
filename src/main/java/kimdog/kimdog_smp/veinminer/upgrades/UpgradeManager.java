package kimdog.kimdog_smp.veinminer.upgrades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpgradeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final Path UPGRADES_DIR = Path.of("config/kimdog_smp/upgrades");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerUpgrades> playerUpgrades = new HashMap<>();

    public static class PlayerUpgrades {
        public int maxBlocksLevel = 0;      // Level 0-5 (64 -> 512 blocks)
        public int maxRangeLevel = 0;       // Level 0-5 (32 -> 256 blocks)
        public int xpMultiplierLevel = 0;   // Level 0-5 (1x -> 3x XP)
        public int speedLevel = 0;          // Level 0-5 (normal -> super fast)
        public int particleLevel = 0;       // Level 0-3 (off, basic, fancy, epic)

        public int emeralds = 0;            // Currency
        public long lastReward = 0;         // Last time they got emeralds
    }

    public static class UpgradeCosts {
        public static final int[] MAX_BLOCKS_COSTS = {100, 200, 400, 800, 1600, 3200};
        public static final int[] MAX_RANGE_COSTS = {150, 300, 600, 1200, 2400, 4800};
        public static final int[] XP_MULTIPLIER_COSTS = {200, 400, 800, 1600, 3200, 6400};
        public static final int[] SPEED_COSTS = {100, 200, 400, 800, 1600, 3200};
        public static final int[] PARTICLE_COSTS = {50, 100, 200, 400};
    }

    public static PlayerUpgrades getPlayerUpgrades(UUID uuid) {
        return playerUpgrades.getOrDefault(uuid, new PlayerUpgrades());
    }

    public static void addEmeralds(UUID uuid, int amount) {
        PlayerUpgrades upgrades = playerUpgrades.computeIfAbsent(uuid, k -> new PlayerUpgrades());
        upgrades.emeralds += amount;
        saveUpgrades(uuid);
    }

    public static boolean purchaseUpgrade(UUID uuid, String upgradeType) {
        PlayerUpgrades upgrades = playerUpgrades.computeIfAbsent(uuid, k -> new PlayerUpgrades());
        int cost = 0;

        switch (upgradeType.toLowerCase()) {
            case "maxblocks":
                if (upgrades.maxBlocksLevel >= 5) return false;
                cost = UpgradeCosts.MAX_BLOCKS_COSTS[upgrades.maxBlocksLevel];
                if (upgrades.emeralds >= cost) {
                    upgrades.emeralds -= cost;
                    upgrades.maxBlocksLevel++;
                    saveUpgrades(uuid);
                    return true;
                }
                break;
            case "maxrange":
                if (upgrades.maxRangeLevel >= 5) return false;
                cost = UpgradeCosts.MAX_RANGE_COSTS[upgrades.maxRangeLevel];
                if (upgrades.emeralds >= cost) {
                    upgrades.emeralds -= cost;
                    upgrades.maxRangeLevel++;
                    saveUpgrades(uuid);
                    return true;
                }
                break;
            case "xpmultiplier":
                if (upgrades.xpMultiplierLevel >= 5) return false;
                cost = UpgradeCosts.XP_MULTIPLIER_COSTS[upgrades.xpMultiplierLevel];
                if (upgrades.emeralds >= cost) {
                    upgrades.emeralds -= cost;
                    upgrades.xpMultiplierLevel++;
                    saveUpgrades(uuid);
                    return true;
                }
                break;
            case "speed":
                if (upgrades.speedLevel >= 5) return false;
                cost = UpgradeCosts.SPEED_COSTS[upgrades.speedLevel];
                if (upgrades.emeralds >= cost) {
                    upgrades.emeralds -= cost;
                    upgrades.speedLevel++;
                    saveUpgrades(uuid);
                    return true;
                }
                break;
            case "particles":
                if (upgrades.particleLevel >= 3) return false;
                cost = UpgradeCosts.PARTICLE_COSTS[upgrades.particleLevel];
                if (upgrades.emeralds >= cost) {
                    upgrades.emeralds -= cost;
                    upgrades.particleLevel++;
                    saveUpgrades(uuid);
                    return true;
                }
                break;
        }
        return false;
    }

    public static void saveUpgrades(UUID uuid) {
        try {
            UPGRADES_DIR.toFile().mkdirs();
            File upgradeFile = UPGRADES_DIR.resolve(uuid + ".json").toFile();
            PlayerUpgrades upgrades = playerUpgrades.get(uuid);
            if (upgrades != null) {
                try (FileWriter writer = new FileWriter(upgradeFile)) {
                    GSON.toJson(upgrades, writer);
                }
            }
        } catch (IOException e) {
            LOGGER.error(" Error saving upgrades for {}: {}", uuid, e.getMessage());
        }
    }

    public static void loadUpgrades(UUID uuid) {
        try {
            UPGRADES_DIR.toFile().mkdirs();
            File upgradeFile = UPGRADES_DIR.resolve(uuid + ".json").toFile();
            if (upgradeFile.exists()) {
                try (FileReader reader = new FileReader(upgradeFile)) {
                    PlayerUpgrades upgrades = GSON.fromJson(reader, PlayerUpgrades.class);
                    if (upgrades != null) {
                        playerUpgrades.put(uuid, upgrades);
                    }
                }
            } else {
                playerUpgrades.put(uuid, new PlayerUpgrades());
            }
        } catch (IOException e) {
            LOGGER.error(" Error loading upgrades for {}: {}", uuid, e.getMessage());
        }
    }

    public static int getMaxBlocks(UUID uuid) {
        PlayerUpgrades upgrades = getPlayerUpgrades(uuid);
        return 64 + (upgrades.maxBlocksLevel * 89); // 64, 153, 242, 331, 420, 509
    }

    public static int getMaxRange(UUID uuid) {
        PlayerUpgrades upgrades = getPlayerUpgrades(uuid);
        return 32 + (upgrades.maxRangeLevel * 44); // 32, 76, 120, 164, 208, 252
    }

    public static double getXpMultiplier(UUID uuid) {
        PlayerUpgrades upgrades = getPlayerUpgrades(uuid);
        return 1.0 + (upgrades.xpMultiplierLevel * 0.4); // 1.0, 1.4, 1.8, 2.2, 2.6, 3.0
    }

    public static int getSpeedLevel(UUID uuid) {
        return getPlayerUpgrades(uuid).speedLevel;
    }

    public static int getParticleLevel(UUID uuid) {
        return getPlayerUpgrades(uuid).particleLevel;
    }
}
