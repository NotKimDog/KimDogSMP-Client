package kimdog.kimdog_smp.anticheat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class AntiCheatConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog AntiCheat");
    private static final Path CONFIG_DIR = Path.of("config/kimdog_smp");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("anticheat.json").toFile();
    private static AntiCheatConfig instance;

    // Detection Settings
    public boolean enableAntiCheat = true;
    public boolean enableSpeedHack = true;
    public boolean enableFlyHack = true;
    public boolean enableNoClipDetection = true;
    public boolean enableReachHack = true;
    public boolean enableInstantBreak = true;

    // Speed Hack Settings
    public double maxSpeed = 0.35; // Max legitimate speed
    public double speedHackThreshold = 0.5; // Speed to trigger detection
    public int speedHackWarnings = 3; // Warnings before kick

    // Fly Hack Settings
    public double maxVerticalSpeed = 0.5; // Max vertical movement
    public int flyHackWarnings = 2; // Warnings before kick

    // Reach Hack Settings
    public double maxReach = 5.5; // Max block reach distance (default is 5.5)
    public double reachHackThreshold = 7.0; // Distance to trigger detection

    // Logging Settings
    public boolean logToFile = true;
    public boolean logToConsole = true;
    public boolean notifyAdmins = true;
    public String logPath = "logs/anticheat/";

    // Punishment Settings
    public boolean enableKick = true;
    public int kicksBeforeBan = 3;
    public boolean enableBan = true;
    public String kickMessage = "§c[AntiCheat] Cheating detected! You have been kicked.";
    public String banMessage = "§c[AntiCheat] Too many violations! You have been banned.";

    public AntiCheatConfig() {
    }

    public static AntiCheatConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                Gson gson = new Gson();
                FileReader reader = new FileReader(CONFIG_FILE);
                instance = gson.fromJson(reader, AntiCheatConfig.class);
                reader.close();
                LOGGER.info("✅ AntiCheat config loaded");
            } else {
                instance = new AntiCheatConfig();
                save();
                LOGGER.info("📝 AntiCheat config created with defaults");
            }
        } catch (IOException e) {
            LOGGER.error("❌ Error loading AntiCheat config: {}", e.getMessage());
            instance = new AntiCheatConfig();
        }
    }

    public static void save() {
        try {
            CONFIG_DIR.toFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(gson.toJson(instance));
            writer.close();
            LOGGER.info("✅ AntiCheat config saved");
        } catch (IOException e) {
            LOGGER.error("❌ Error saving AntiCheat config: {}", e.getMessage());
        }
    }
}
