package kimdog.kimdog_smp.veinminer.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VeinMinerNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final Map<UUID, Boolean> toggles = new ConcurrentHashMap<>();

    public static void register() {
        LOGGER.info(" Registering VeinMiner networking payloads...");

        // Register the payload type with its codec so Fabric knows about it
        PayloadTypeRegistry.playS2C().register(VeinMinerTogglePayload.ID, VeinMinerTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(VeinMinerTogglePayload.ID, VeinMinerTogglePayload.CODEC);
        LOGGER.info(" Payload types registered (S2C and C2S)");

        // Now register the payload handler for the server to receive toggle packets from clients
        ServerPlayNetworking.registerGlobalReceiver(VeinMinerTogglePayload.ID, (payload, context) -> {
            // Store the toggle state for this player
            UUID playerUuid = context.player().getUuid();
            boolean enabled = payload.isEnabled();
            toggles.put(playerUuid, enabled);
            String status = enabled ? " ENABLED" : " DISABLED";
            LOGGER.info(" VeinMiner toggle status for {}: {}", context.player().getName().getString(), status);
        });
        LOGGER.info(" Payload handler registered");
    }

    public static Boolean getToggleForPlayer(UUID uuid) {
        return toggles.getOrDefault(uuid, false);
    }

    // allow commands / other code to set player toggle state
    public static void setToggleForPlayer(UUID uuid, boolean on) {
        toggles.put(uuid, on);
    }
}
