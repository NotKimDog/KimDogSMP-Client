package kimdog.kimdog_smp.chatmessages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatMessagesMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog ChatMessages");

    public void onInitialize() {
        LOGGER.info("");
        LOGGER.info(" KimDog Chat Messages - Initializing...");
        LOGGER.info("");

        // Load config
        ChatMessagesConfig.load();

        // Register broadcaster
        ChatMessagesBroadcaster.register();

        // Register commands
        ChatMessagesCommands.register();

        LOGGER.info(" Chat Messages initialized successfully!");
        LOGGER.info("");
    }
}
