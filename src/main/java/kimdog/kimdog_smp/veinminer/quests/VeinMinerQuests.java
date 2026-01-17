package kimdog.kimdog_smp.veinminer.quests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VeinMinerQuests {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final Map<java.util.UUID, PlayerQuest> playerQuests = new HashMap<>();
    private static final Map<java.util.UUID, String> playerQuestHashes = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Path QUESTS_DIR = Path.of("config/kimdog_smp/quests");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String ANTI_CHEAT_KEY = "kimdog_veinminer_security_2026";

    public static class PlayerQuest {
        public String questType;
        public int targetCount;
        public int currentCount;
        public String displayName;
        public String description;
        public String icon;
        public int reward;
        public long createdTime;
        public String color;
        public String checksum;

        public PlayerQuest(String type, int count) {
            this.questType = type;
            this.targetCount = count;
            this.currentCount = 0;
            this.createdTime = System.currentTimeMillis();
            setupQuest();
            generateChecksum();
        }

        private void setupQuest() {
            switch (questType.toLowerCase()) {
                case "diamond":
                    this.displayName = " Diamond Hunter";
                    this.description = "Mine " + targetCount + " diamond veins";
                    this.reward = targetCount * 100;
                    this.icon = "";
                    this.color = "b";
                    break;
                case "emerald":
                    this.displayName = " Emerald Collector";
                    this.description = "Mine " + targetCount + " emerald veins";
                    this.reward = targetCount * 80;
                    this.icon = "";
                    this.color = "a";
                    break;
                case "gold":
                    this.displayName = " Gold Seeker";
                    this.description = "Mine " + targetCount + " gold veins";
                    this.reward = targetCount * 60;
                    this.icon = "";
                    this.color = "6";
                    break;
                case "iron":
                    this.displayName = " Iron Worker";
                    this.description = "Mine " + targetCount + " iron veins";
                    this.reward = targetCount * 50;
                    this.icon = "";
                    this.color = "7";
                    break;
                case "redstone":
                    this.displayName = " Redstone Engineer";
                    this.description = "Mine " + targetCount + " redstone veins";
                    this.reward = targetCount * 40;
                    this.icon = "";
                    this.color = "c";
                    break;
                case "coal":
                    this.displayName = " Coal Miner";
                    this.description = "Mine " + targetCount + " coal veins";
                    this.reward = targetCount * 30;
                    this.icon = "";
                    this.color = "8";
                    break;
                case "copper":
                    this.displayName = " Copper Extractor";
                    this.description = "Mine " + targetCount + " copper veins";
                    this.reward = targetCount * 40;
                    this.icon = "";
                    this.color = "6";
                    break;
                default:
                    this.displayName = " Ore Miner";
                    this.description = "Mine " + targetCount + " ore veins";
                    this.reward = targetCount * 50;
                    this.icon = "";
                    this.color = "f";
            }
        }

        public void generateChecksum() {
            try {
                String data = questType + "|" + targetCount + "|" + currentCount + "|" + createdTime + "|" + ANTI_CHEAT_KEY;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                this.checksum = sb.toString();
            } catch (Exception e) {
                LOGGER.error(" Error generating checksum: {}", e.getMessage());
                this.checksum = "invalid";
            }
        }

        public boolean isValid() {
            try {
                String data = questType + "|" + targetCount + "|" + currentCount + "|" + createdTime + "|" + ANTI_CHEAT_KEY;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                String expectedChecksum = sb.toString();
                return expectedChecksum.equals(this.checksum);
            } catch (Exception e) {
                LOGGER.error(" Error validating checksum: {}", e.getMessage());
                return false;
            }
        }

        public boolean isComplete() {
            return currentCount >= targetCount;
        }

        public int getProgress() {
            return (int) ((currentCount * 100.0) / targetCount);
        }

        public String getProgressBar() {
            int progress = getProgress();
            int filledBars = progress / 10;
            int emptyBars = 10 - filledBars;
            StringBuilder bar = new StringBuilder();
            bar.append("a");
            for (int i = 0; i < filledBars; i++) bar.append("");
            bar.append("7");
            for (int i = 0; i < emptyBars; i++) bar.append("");
            bar.append("r ").append(progress).append("%");
            return bar.toString();
        }
    }

    public static PlayerQuest getActiveQuest(ServerPlayerEntity player) {
        return playerQuests.get(player.getUuid());
    }

    public static void generateNewQuest(ServerPlayerEntity player) {
        String[] oreTypes = {"diamond", "emerald", "gold", "iron", "redstone", "coal", "copper"};
        String randomOre = oreTypes[RANDOM.nextInt(oreTypes.length)];
        int count = randomOre.equals("diamond") || randomOre.equals("emerald") ?
                    RANDOM.nextInt(3) + 2 : RANDOM.nextInt(5) + 3;

        PlayerQuest quest = new PlayerQuest(randomOre, count);
        playerQuests.put(player.getUuid(), quest);
        playerQuestHashes.put(player.getUuid(), quest.checksum);
        saveQuestData(player);
        LOGGER.info(" New quest generated for {}: {}", player.getName().getString(), quest.displayName);
    }

    public static void incrementOreCount(ServerPlayerEntity player, String oreType) {
        PlayerQuest quest = playerQuests.get(player.getUuid());
        if (quest == null) {
            loadQuestData(player);
            quest = playerQuests.get(player.getUuid());
        }

        if (!validateQuestIntegrity(player, quest)) {
            LOGGER.warn("  CHEATING ATTEMPT DETECTED for player {}! Quest data tampered!", player.getName().getString());
            player.sendMessage(net.minecraft.text.Text.literal(" Your quest data has been tampered with! Quest reset.")
                    .formatted(net.minecraft.util.Formatting.RED));
            generateNewQuest(player);
            return;
        }

        if (quest.questType.equalsIgnoreCase(oreType)) {
            if (hasUnrealisticProgress(player)) {
                LOGGER.warn("  SUSPICIOUS ACTIVITY DETECTED for player {}! Progress increased too fast!", player.getName().getString());
                player.sendMessage(net.minecraft.text.Text.literal(" Suspicious quest progress detected!")
                        .formatted(net.minecraft.util.Formatting.RED));
                return;
            }

            quest.currentCount++;
            quest.generateChecksum();
            playerQuestHashes.put(player.getUuid(), quest.checksum);
            saveQuestData(player);

            if (quest.isComplete()) {
                completeQuest(player, quest);
            }
        }
    }

    private static boolean validateQuestIntegrity(ServerPlayerEntity player, PlayerQuest quest) {
        if (quest == null || quest.checksum == null) {
            return false;
        }

        String storedHash = playerQuestHashes.get(player.getUuid());
        if (storedHash == null) {
            return false;
        }

        return quest.isValid();
    }

    private static boolean hasUnrealisticProgress(ServerPlayerEntity player) {
        PlayerQuest quest = playerQuests.get(player.getUuid());
        if (quest == null) return false;

        long elapsedSeconds = (System.currentTimeMillis() - quest.createdTime) / 1000;

        if (elapsedSeconds < 10 && quest.currentCount >= quest.targetCount) {
            return true;
        }

        if (elapsedSeconds > 0 && quest.currentCount > elapsedSeconds) {
            return true;
        }

        return false;
    }

    private static void completeQuest(ServerPlayerEntity player, PlayerQuest quest) {
        LOGGER.info(" Quest completed by {}: {}", player.getName().getString(), quest.displayName);
        player.sendMessage(net.minecraft.text.Text.literal(" Quest completed! " + quest.displayName + " - Reward: " + quest.reward + " XP")
                .formatted(net.minecraft.util.Formatting.GOLD));
        playerQuests.remove(player.getUuid());
        playerQuestHashes.remove(player.getUuid());
        deleteQuestData(player);
    }

    public static String getQuestDisplay(ServerPlayerEntity player) {
        PlayerQuest quest = playerQuests.get(player.getUuid());
        if (quest == null) {
            return " No active quest";
        }
        return String.format("%s %s [%d/%d] %s",
            quest.icon, quest.displayName, quest.currentCount, quest.targetCount, quest.getProgressBar());
    }

    public static String getDetailedQuestScreen(ServerPlayerEntity player) {
        PlayerQuest quest = playerQuests.get(player.getUuid());
        if (quest == null) {
            return "6\n" +
                   "6f No Active Quest\n" +
                   "6f Type /quest new to generate one!\n" +
                   "6";
        }

        String separator = "6";
        String header = String.format("6f %s %s", quest.icon, quest.displayName);
        String description = String.format("6f %s", quest.description);
        String progress = String.format("6f Progress: %d/%d", quest.currentCount, quest.targetCount);
        String progressBar = String.format("6f %s", quest.getProgressBar());
        String reward = String.format("6f  Reward: %d XP", quest.reward);

        return separator + "\n" + header + "\n" + description + "\n" + progressBar + "\n" + progress + "\n" + reward + "\n" + separator;
    }

    private static void saveQuestData(ServerPlayerEntity player) {
        try {
            QUESTS_DIR.toFile().mkdirs();
            File questFile = QUESTS_DIR.resolve(player.getUuid() + ".json").toFile();
            PlayerQuest quest = playerQuests.get(player.getUuid());
            if (quest != null) {
                try (FileWriter writer = new FileWriter(questFile)) {
                    GSON.toJson(quest, writer);
                }
                LOGGER.debug(" Quest saved for {}", player.getUuid());
            }
        } catch (IOException e) {
            LOGGER.error(" Error saving quest data: {}", e.getMessage());
        }
    }

    public static void loadQuestData(ServerPlayerEntity player) {
        try {
            QUESTS_DIR.toFile().mkdirs();
            File questFile = QUESTS_DIR.resolve(player.getUuid() + ".json").toFile();
            if (questFile.exists()) {
                try (FileReader reader = new FileReader(questFile)) {
                    PlayerQuest quest = GSON.fromJson(reader, PlayerQuest.class);
                    if (quest != null) {
                        if (!quest.isValid()) {
                            LOGGER.warn("  TAMPERED QUEST DATA DETECTED for {}! Generating new quest!", player.getName().getString());
                            generateNewQuest(player);
                            return;
                        }

                        playerQuests.put(player.getUuid(), quest);
                        playerQuestHashes.put(player.getUuid(), quest.checksum);
                        LOGGER.info(" Quest data loaded for {}: {}", player.getName().getString(), quest.displayName);
                    }
                }
            } else {
                generateNewQuest(player);
            }
        } catch (IOException e) {
            LOGGER.error(" Error loading quest data: {}", e.getMessage());
            generateNewQuest(player);
        }
    }

    private static void deleteQuestData(ServerPlayerEntity player) {
        try {
            File questFile = QUESTS_DIR.resolve(player.getUuid() + ".json").toFile();
            if (questFile.exists()) {
                questFile.delete();
            }
        } catch (Exception e) {
            LOGGER.error(" Error deleting quest data: {}", e.getMessage());
        }
    }
}
