package kimdog.kimdog_smp.veinminer.gui;

import kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side handler for VeinMiner GUI
 */
public class VeinMinerGuiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner GUI");
    private static KeyBinding openUpgradeMenuKey;

    public static void initialize() {
        LOGGER.info("[GUI] Initializing VeinMiner GUI system...");

        // Register keybinding (default: U key)
        openUpgradeMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.kimdog.veinminer.upgrades",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            KeyBinding.Category.MISC
        ));

        // Register tick event to check for key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (openUpgradeMenuKey.wasPressed()) {
                openUpgradeMenu(client);
            }
        });

        LOGGER.info("[GUI] ✅ VeinMiner GUI system initialized (Press U to open upgrades)");
    }

    private static void openUpgradeMenu(MinecraftClient client) {
        if (client.player == null) return;

        // Load player upgrades
        UpgradeManager.PlayerUpgrades upgrades = UpgradeManager.getPlayerUpgrades(client.player.getUuid());

        // Open the GUI screen
        client.setScreen(new VeinMinerUpgradeScreen(upgrades));
        LOGGER.debug("[GUI] Opened upgrade menu for player");
    }

    public static void openUpgradeMenuFromCommand() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            openUpgradeMenu(client);
        }
    }
}
