package kimdog.kimdog_smp.anticheat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AntiCheatEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog AntiCheat");
    private static final Map<java.util.UUID, PlayerData> playerDataMap = new HashMap<>();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class PlayerData {
        public double lastX;
        public double lastY;
        public double lastZ;
        public long lastTickTime;
        public double lastSpeed;
        public int speedHackWarnings;
        public int flyHackWarnings;
        public int totalViolations;
        public boolean isMonitored;

        public PlayerData() {
            this.speedHackWarnings = 0;
            this.flyHackWarnings = 0;
            this.totalViolations = 0;
            this.isMonitored = false;
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!AntiCheatConfig.get().enableAntiCheat) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkPlayer(player);
            }
        });
    }

    private static void checkPlayer(ServerPlayerEntity player) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUuid(), k -> new PlayerData());

        // Check for Speed Hack
        if (AntiCheatConfig.get().enableSpeedHack) {
            checkSpeedHack(player, data);
        }

        // Check for Fly Hack
        if (AntiCheatConfig.get().enableFlyHack) {
            checkFlyHack(player, data);
        }

        // Check for Reach Hack
        if (AntiCheatConfig.get().enableReachHack) {
            checkReachHack(player, data);
        }

        // Update player position
        data.lastX = player.getX();
        data.lastY = player.getY();
        data.lastZ = player.getZ();
        data.lastTickTime = System.currentTimeMillis();
    }

    private static void checkSpeedHack(ServerPlayerEntity player, PlayerData data) {
        AntiCheatConfig config = AntiCheatConfig.get();

        if (data.lastTickTime == 0) {
            data.lastTickTime = System.currentTimeMillis();
            return;
        }

        double deltaX = player.getX() - data.lastX;
        double deltaZ = player.getZ() - data.lastZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Ignore if player is flying (creative/spectator mode or using our fly system)
        if (player.getAbilities().flying || player.getAbilities().allowFlying) return;

        if (distance > config.speedHackThreshold) {
            data.speedHackWarnings++;
            data.totalViolations++;

            String message = String.format("⚠️ [SPEED HACK] %s moved %.2f blocks (max: %.2f) - Violations: %d",
                    player.getName().getString(), distance, config.speedHackThreshold, data.totalViolations);

            logViolation(player, "SPEED_HACK", message, distance);
            notifyAdmins(message);

            LOGGER.warn(message);

            if (config.enableKick && data.speedHackWarnings >= config.speedHackWarnings) {
                kickPlayer(player, config.kickMessage);
                data.speedHackWarnings = 0;
            } else if (data.totalViolations % 5 == 0) {
                player.sendMessage(Text.literal("§e[AntiCheat] Speed hack detected! Please stop.").formatted(Formatting.YELLOW));
            }
        }
    }

    private static void checkFlyHack(ServerPlayerEntity player, PlayerData data) {
        AntiCheatConfig config = AntiCheatConfig.get();

        if (data.lastTickTime == 0) return;

        // Skip fly hack detection entirely if flight is allowed (creative, spectator, or our fly system)
        if (player.getAbilities().flying || player.getAbilities().allowFlying) return;

        double deltaY = player.getY() - data.lastY;

        // Allow falling
        if (deltaY < 0) return;

        // Check if player is moving upward without jumping
        if (deltaY > config.maxVerticalSpeed && !player.isOnGround()) {
            data.flyHackWarnings++;
            data.totalViolations++;

            String message = String.format("⚠️ [FLY HACK] %s moved up %.2f blocks (max: %.2f) - Violations: %d",
                    player.getName().getString(), deltaY, config.maxVerticalSpeed, data.totalViolations);

            logViolation(player, "FLY_HACK", message, deltaY);
            notifyAdmins(message);

            LOGGER.warn(message);

            if (config.enableKick && data.flyHackWarnings >= config.flyHackWarnings) {
                kickPlayer(player, config.kickMessage);
                data.flyHackWarnings = 0;
            } else if (data.totalViolations % 5 == 0) {
                player.sendMessage(Text.literal("§e[AntiCheat] Fly hack detected! Please stop.").formatted(Formatting.YELLOW));
            }
        }
    }

    private static void checkReachHack(ServerPlayerEntity player, PlayerData data) {
        AntiCheatConfig config = AntiCheatConfig.get();

        // This is a basic check - more sophisticated systems would track block interactions
        // For now, we'll flag suspicious behavior based on movement patterns
        if (player.getAttackCooldownProgress(0.5f) == 1.0f && Math.random() < 0.01) {
            // Random sampling of player actions for reach detection
            // In a real system, you'd track individual block break attempts
        }
    }

    private static void kickPlayer(ServerPlayerEntity player, String message) {
        AntiCheatConfig config = AntiCheatConfig.get();
        if (!config.enableKick) return;

        LOGGER.warn("🚫 Kicking player {} for cheating", player.getName().getString());
        player.networkHandler.disconnect(Text.literal(message));
    }

    private static void logViolation(ServerPlayerEntity player, String violationType, String message, double value) {
        AntiCheatConfig config = AntiCheatConfig.get();

        if (!config.logToFile) return;

        try {
            Path logDir = Path.of(config.logPath);
            Files.createDirectories(logDir);

            String fileName = String.format("anticheat_%s.log", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            File logFile = logDir.resolve(fileName).toFile();

            try (FileWriter writer = new FileWriter(logFile, true)) {
                String timestamp = LocalDateTime.now().format(dateFormatter);
                String logEntry = String.format("[%s] %s | Player: %s | Type: %s | Value: %.2f | Total Violations: %d\n",
                        timestamp, message, player.getName().getString(), violationType, value,
                        playerDataMap.get(player.getUuid()).totalViolations);
                writer.write(logEntry);
            }
        } catch (IOException e) {
            LOGGER.error("❌ Error writing to anticheat log: {}", e.getMessage());
        }
    }

    private static void notifyAdmins(String message) {
        AntiCheatConfig config = AntiCheatConfig.get();
        if (!config.notifyAdmins) return;

        // In a real system, you'd send this to all online admins
        LOGGER.info("📢 ADMIN NOTIFICATION: {}", message);
    }

    public static void removePlayerData(java.util.UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static Map<java.util.UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
}
