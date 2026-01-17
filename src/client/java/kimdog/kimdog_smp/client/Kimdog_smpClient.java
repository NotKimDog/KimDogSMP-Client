package kimdog.kimdog_smp.client;

import net.fabricmc.api.ClientModInitializer;
import kimdog.kimdog_smp.zoom.ZoomClient;
import kimdog.kimdog_smp.veinminer.gui.VeinMinerGuiClient;

public class Kimdog_smpClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Initialize veinminer client features (keybind)
        new VeinMinerClient().onInitializeClient();

        // Initialize zoom system
        ZoomClient.onInitializeClient();

        // Initialize VeinMiner GUI
        VeinMinerGuiClient.initialize();
    }
}
