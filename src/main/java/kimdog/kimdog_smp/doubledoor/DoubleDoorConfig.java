package kimdog.kimdog_smp.doubledoor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class DoubleDoorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog DoubleDoor");
    private static final Path CONFIG_DIR = Path.of("config/kimdog_smp");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("doubledoor.json").toFile();
    private static DoubleDoorConfig instance;

    public boolean enabled = true;
    public int maxDistance = 1; // Maximum distance between doors (in blocks)
    public boolean requireSneak = false; // Require sneaking to activate
    public boolean playSound = true; // Play sound effect when doors open
    public float soundVolume = 1.0f; // Sound volume (0.0 - 1.0)
    public boolean spawnParticles = true; // Show particles when opening
    public int particleCount = 10; // Number of particles

    public DoubleDoorConfig() {
    }

    public static DoubleDoorConfig get() {
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
                instance = gson.fromJson(reader, DoubleDoorConfig.class);
                reader.close();
                LOGGER.info(" Double Door config loaded");
            } else {
                instance = new DoubleDoorConfig();
                save();
                LOGGER.info(" Double Door config created with defaults");
            }
        } catch (IOException e) {
            LOGGER.error(" Error loading Double Door config: {}", e.getMessage());
            instance = new DoubleDoorConfig();
        }
    }

    public static void save() {
        try {
            CONFIG_DIR.toFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(gson.toJson(instance));
            writer.close();
            LOGGER.info(" Double Door config saved");
        } catch (IOException e) {
            LOGGER.error(" Error saving Double Door config: {}", e.getMessage());
        }
    }
}
