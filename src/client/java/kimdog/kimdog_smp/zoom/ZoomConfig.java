package kimdog.kimdog_smp.zoom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Configuration system for the Zoom feature
 */
public class ZoomConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog Zoom");
    private static final Path CONFIG_DIR = Path.of("config/kimdog_smp");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("zoom.json").toFile();
    private static ZoomConfig instance;

    // Zoom Settings
    public boolean enableZoom = true;
    public double minZoom = 0.1;
    public double maxZoom = 10.0;
    public double zoomSpeed = 0.1;
    public double defaultZoom = 1.0;

    // Key Binding Settings
    public String zoomKeyName = "Z";
    public int zoomKeyCode = 90; // GLFW code for Z key

    // Visual Settings
    public boolean smoothZoom = true;
    public boolean hideHudWhenZooming = true;
    public double smoothTransitionSpeed = 0.15; // 0.0-1.0, higher = faster
    public boolean showZoomIndicator = true;
    public String zoomIndicatorFormat = "Zoom: {zoom}x";

    // Performance Settings
    public boolean enableZoomLogging = false;

    public ZoomConfig() {
    }

    public static ZoomConfig get() {
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
                instance = gson.fromJson(reader, ZoomConfig.class);
                reader.close();
                LOGGER.info("[ZOOM] ✅ Configuration loaded");
            } else {
                instance = new ZoomConfig();
                save();
                LOGGER.info("[ZOOM] 📝 Configuration created with defaults");
            }
        } catch (IOException e) {
            LOGGER.error("[ZOOM] ❌ Error loading configuration: {}", e.getMessage());
            instance = new ZoomConfig();
        }
    }

    public static void save() {
        try {
            CONFIG_DIR.toFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(gson.toJson(instance));
            writer.close();
            LOGGER.info("[ZOOM] ✅ Configuration saved");
        } catch (IOException e) {
            LOGGER.error("[ZOOM] ❌ Error saving configuration: {}", e.getMessage());
        }
    }

    /**
     * Get configuration information for logging
     */
    public String getConfigInfo() {
        return String.format(
            "Zoom Config:\n" +
            "  Enabled: %b\n" +
            "  Min Zoom: %.1fx\n" +
            "  Max Zoom: %.1fx\n" +
            "  Zoom Speed: %.2f\n" +
            "  Default Zoom: %.1fx\n" +
            "  Key: %s (%d)\n" +
            "  Smooth Zoom: %b\n" +
            "  Smooth Speed: %.2f\n" +
            "  Hide HUD: %b\n" +
            "  Show Indicator: %b",
            enableZoom, minZoom, maxZoom, zoomSpeed, defaultZoom,
            zoomKeyName, zoomKeyCode, smoothZoom, smoothTransitionSpeed, hideHudWhenZooming, showZoomIndicator
        );
    }
}
