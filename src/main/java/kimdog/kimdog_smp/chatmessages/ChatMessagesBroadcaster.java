package kimdog.kimdog_smp.chatmessages;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChatMessagesBroadcaster {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog ChatMessages");
    private static long lastMessageTime = 0;
    private static int lastMessageIndex = -1;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ChatMessagesConfig config = ChatMessagesConfig.get();

            if (!config.enabled) {
                return;
            }

            List<String> messages = ChatMessagesConfig.getMessages();
            if (messages.isEmpty()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long intervalMs = config.messageIntervalSeconds * 1000L;

            if (currentTime - lastMessageTime >= intervalMs) {
                broadcastMessage(server, config, messages);
                lastMessageTime = currentTime;
            }
        });
    }

    private static void broadcastMessage(net.minecraft.server.MinecraftServer server, ChatMessagesConfig config, List<String> messages) {
        try {
            String message;

            if (config.randomOrder) {
                int randomIndex = (int) (Math.random() * messages.size());
                message = messages.get(randomIndex);
            } else {
                lastMessageIndex++;
                if (lastMessageIndex >= messages.size()) {
                    lastMessageIndex = 0;
                }
                message = messages.get(lastMessageIndex);
            }

            Text chatMessage = Text.literal(message).formatted(Formatting.AQUA);
            server.getPlayerManager().broadcast(chatMessage, false);
            LOGGER.info(" Broadcasted: {}", message);
        } catch (Exception e) {
            LOGGER.error(" Error broadcasting message: {}", e.getMessage());
        }
    }
}
