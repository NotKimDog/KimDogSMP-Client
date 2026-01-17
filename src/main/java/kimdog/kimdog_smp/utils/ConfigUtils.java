package kimdog.kimdog_smp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration file utilities for VeinMiner
 * Handles JSON serialization and file operations
 */
public class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config/kimdog_smp";

    static {
        // Create config directory if it doesn't exist
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Load a configuration from a JSON file
     */
    public static <T> T loadConfig(String filename, Class<T> configClass) {
        try {
            File file = new File(CONFIG_DIR + "/" + filename);
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    return GSON.fromJson(reader, configClass);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config: " + filename, e);
        }
        return null;
    }

    /**
     * Save a configuration to a JSON file
     */
    public static void saveConfig(String filename, Object config) {
        try {
            File file = new File(CONFIG_DIR + "/" + filename);
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            }
            LOGGER.info("Saved config: " + filename);
        } catch (IOException e) {
            LOGGER.error("Failed to save config: " + filename, e);
        }
    }

    /**
     * Check if a config file exists
     */
    public static boolean configExists(String filename) {
        return new File(CONFIG_DIR + "/" + filename).exists();
    }

    /**
     * Delete a config file
     */
    public static boolean deleteConfig(String filename) {
        try {
            return new File(CONFIG_DIR + "/" + filename).delete();
        } catch (Exception e) {
            LOGGER.error("Failed to delete config: " + filename, e);
            return false;
        }
    }

    /**
     * Get config directory path
     */
    public static String getConfigDir() {
        return CONFIG_DIR;
    }

    /**
     * Get full path to config file
     */
    public static String getConfigPath(String filename) {
        return CONFIG_DIR + "/" + filename;
    }
}
