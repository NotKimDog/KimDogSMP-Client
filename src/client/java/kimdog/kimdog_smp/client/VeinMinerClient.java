package kimdog.kimdog_smp.client;

import kimdog.kimdog_smp.veinminer.VeinMinerConfig;
import kimdog.kimdog_smp.veinminer.network.VeinMinerTogglePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeinMinerClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner Client");
    private static KeyBinding toggleKey;
    private static boolean clientEnabled = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[CLIENT] Initializing VeinMiner Client...");

        // Load the config
        VeinMinerConfig.load();
        String activationMode = VeinMinerConfig.get().activation;
        LOGGER.info("[CLIENT] VeinMiner activation mode: {}", activationMode);

        // Register the payload type on client side
        try {
            PayloadTypeRegistry.playC2S().register(VeinMinerTogglePayload.ID, VeinMinerTogglePayload.CODEC);
            LOGGER.info("[CLIENT] ✅ C2S Payload type registered");
        } catch (Exception e) {
            LOGGER.debug("[CLIENT] Payload already registered or error: {}", e.getMessage());
        }

        // Only register toggle key if using toggle mode
        if ("toggle".equalsIgnoreCase(activationMode)) {
            toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kimdog.veinminer.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.MISC
            ));
            LOGGER.info("[CLIENT] ✅ Key binding registered (K key for toggle mode)");
        }

        // Listen on client tick
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle toggle key if in toggle mode
            if ("toggle".equalsIgnoreCase(activationMode) && toggleKey != null) {
                while (toggleKey.wasPressed()) {
                    clientEnabled = !clientEnabled;
                    LOGGER.info("[CLIENT] VeinMiner toggled: {}", clientEnabled ? "ON" : "OFF");

                    // Send packet to server
                    if (client.player != null) {
                        try {
                            ClientPlayNetworking.send(new VeinMinerTogglePayload(clientEnabled));
                            LOGGER.info("[CLIENT] ✅ Toggle packet sent to server");
                        } catch (Exception e) {
                            LOGGER.error("[CLIENT] ❌ Error sending toggle packet: {}", e.getMessage());
                        }
                    }
                }
            }
            // If always active, send enabled state once on first connection
            else if ("always".equalsIgnoreCase(activationMode)) {
                if (client.player != null && !clientEnabled) {
                    clientEnabled = true;
                    try {
                        ClientPlayNetworking.send(new VeinMinerTogglePayload(true));
                        LOGGER.info("[CLIENT] ✅ VeinMiner set to ALWAYS ACTIVE");
                    } catch (Exception e) {
                        LOGGER.debug("[CLIENT] Already sent activation packet: {}", e.getMessage());
                    }
                }
            }
        });

        LOGGER.info("[CLIENT] ✅ VeinMiner Client initialized successfully!");
        LOGGER.info("[CLIENT] Activation Mode: {} | Enabled: {}", activationMode,
            "always".equalsIgnoreCase(activationMode) ? "ALWAYS" :
            "sneak".equalsIgnoreCase(activationMode) ? "WHEN SNEAKING" :
            "TOGGLE MODE (K key)");
    }
}
