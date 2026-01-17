package kimdog.kimdog_smp.zoom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoomClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog Zoom");
    private static KeyBinding zoomKey;
    private static double currentZoom = 1.0;
    private static double targetZoom = 1.0;
    private static boolean isZooming = false;
    private static boolean hudHidden = false;
    private static boolean lastFrameZooming = false;
    private static double baseFov = 70.0; // captured when zoom starts
    private static boolean fovCaptured = false;

    public static void onInitializeClient() {
        LOGGER.info("[ZOOM] Initializing Zoom System...");

        // Load configuration
        ZoomConfig.load();
        ZoomConfig config = ZoomConfig.get();

        if (!config.enableZoom) {
            LOGGER.info("[ZOOM] ⚠️  Zoom system is disabled in configuration");
            return;
        }

        // Register zoom key binding
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.kimdog.zoom",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyBinding.Category.MISC
        ));
        LOGGER.info("[ZOOM] ✅ Key binding registered ({} key)", config.zoomKeyName);

        // Set default zoom
        currentZoom = config.defaultZoom;
        targetZoom = config.defaultZoom;

        // Register client tick event for zoom control
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (!config.enableZoom) return;

            // Check if zoom key is pressed
            while (zoomKey.wasPressed()) {
                isZooming = !isZooming;
                if (isZooming) {
                    LOGGER.info("[ZOOM] ✅ Zoom mode ACTIVE");
                    targetZoom = currentZoom;
                } else {
                    LOGGER.info("[ZOOM] ❌ Zoom mode INACTIVE");
                    targetZoom = config.defaultZoom;
                }
            }

            // Update HUD only when state toggles
            if (isZooming != lastFrameZooming) {
                updateHUDVisibility(client, config);
                if (isZooming && !fovCaptured) {
                    baseFov = client.options.getFov().getValue();
                    fovCaptured = true;
                }
                if (!isZooming && fovCaptured) {
                    client.options.getFov().setValue((int) baseFov);
                    fovCaptured = false;
                }
                lastFrameZooming = isZooming;
            }

            // Smooth zoom transition
            if (config.smoothZoom) smoothZoomTransition();

            // Apply FOV while zooming
            if (isZooming && fovCaptured) {
                double zoomedFov = baseFov / currentZoom;
                zoomedFov = Math.max(10.0, Math.min(150.0, zoomedFov));
                client.options.getFov().setValue((int) zoomedFov);
            }
        });

        LOGGER.info("[ZOOM] ✅ Zoom System initialized successfully!");
        if (config.enableZoomLogging) {
            LOGGER.info(config.getConfigInfo());
        }
    }

    private static void updateHUDVisibility(MinecraftClient client, ZoomConfig config) {
        if (!config.hideHudWhenZooming) return;

        if (isZooming && !hudHidden) {
            // Hide HUD when zooming
            client.options.hudHidden = true;
            hudHidden = true;
            if (config.enableZoomLogging) {
                LOGGER.debug("[ZOOM] HUD hidden");
            }
        } else if (!isZooming && hudHidden) {
            // Show HUD when not zooming
            client.options.hudHidden = false;
            hudHidden = false;
            if (config.enableZoomLogging) {
                LOGGER.debug("[ZOOM] HUD shown");
            }
        }
    }

    private static void smoothZoomTransition() {
        ZoomConfig config = ZoomConfig.get();
        if (Math.abs(currentZoom - targetZoom) > 0.01) {
            double diff = targetZoom - currentZoom;
            currentZoom += diff * config.smoothTransitionSpeed;
            if (Math.abs(currentZoom - targetZoom) < 0.01) currentZoom = targetZoom;
        }
    }

    public static boolean isZooming() {
        return isZooming;
    }

    public static double getCurrentZoom() {
        return currentZoom;
    }

    public static void handleScroll(double scrollDelta) {
        if (!isZooming) return;
        ZoomConfig config = ZoomConfig.get();
        if (!config.enableZoom) return;
        targetZoom += scrollDelta * config.zoomSpeed;
        targetZoom = Math.max(config.minZoom, Math.min(config.maxZoom, targetZoom));
        if (config.enableZoomLogging) LOGGER.debug("[ZOOM] Target zoom level: {}", String.format("%.2f", targetZoom));
    }

    public static void resetZoom() {
        ZoomConfig config = ZoomConfig.get();
        targetZoom = config.defaultZoom;
        currentZoom = config.defaultZoom;
        isZooming = false;
        if (fovCaptured) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options != null) client.options.getFov().setValue((int) baseFov);
            fovCaptured = false;
        }
    }
}
