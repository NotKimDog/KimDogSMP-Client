package kimdog.kimdog_smp.doubledoor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleDoorMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog DoubleDoor");

    public void onInitialize() {
        LOGGER.info("");
        LOGGER.info(" KimDog Double Door - Initializing...");
        LOGGER.info("");

        // Load config
        DoubleDoorConfig.load();

        // Register handler
        DoubleDoorHandler.register();

        LOGGER.info(" Double Door initialized successfully!");
        LOGGER.info(" Right-click a door/trapdoor to open adjacent ones!");
        LOGGER.info("");
    }
}
