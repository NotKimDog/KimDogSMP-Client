package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Power-up system for VeinMiner - random bonuses during mining
 */
public class PowerUpSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner PowerUps");
    private static final Map<UUID, List<ActivePowerUp>> activePowerUps = new HashMap<>();
    private static final Random RANDOM = new Random();

    public enum PowerUpType {
        SPEED_BOOST(" Speed Boost", "e", 10, 0.05), // 5% chance, 10s duration
        DOUBLE_DROPS(" Double Drops", "b", 15, 0.03),
        INFINITE_DURABILITY(" Infinite Durability", "a", 8, 0.02),
        MEGA_FORTUNE(" Mega Fortune", "d", 12, 0.04),
        XP_MULTIPLIER(" XP Multiplier", "6", 15, 0.06),
        AUTO_SMELT(" Auto Smelt", "c", 20, 0.03),
        MAGNET_SURGE(" Magnet Surge", "3", 10, 0.05);

        public final String name;
        public final String color;
        public final int duration;
        public final double chance;

        PowerUpType(String name, String color, int duration, double chance) {
            this.name = name;
            this.color = color;
            this.duration = duration;
            this.chance = chance;
        }
    }

    public static class ActivePowerUp {
        public final PowerUpType type;
        public final long expiryTime;

        public ActivePowerUp(PowerUpType type, long expiryTime) {
            this.type = type;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public static void tryGrantPowerUp(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        // Clean up expired power-ups
        cleanupExpired(uuid);

        // Roll for each power-up
        for (PowerUpType type : PowerUpType.values()) {
            if (RANDOM.nextDouble() < type.chance) {
                grantPowerUp(player, type);
            }
        }
    }

    private static void grantPowerUp(ServerPlayerEntity player, PowerUpType type) {
        UUID uuid = player.getUuid();
        List<ActivePowerUp> powerUps = activePowerUps.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Check if already has this power-up
        if (hasPowerUp(uuid, type)) {
            return; // Don't stack same power-up
        }

        long expiryTime = System.currentTimeMillis() + (type.duration * 1000L);
        powerUps.add(new ActivePowerUp(type, expiryTime));

        // Notify player
        player.sendMessage(
            Text.literal(String.format(
                "%s%s activated! (%ds)",
                type.color, type.name, type.duration
            )).formatted(Formatting.BOLD),
            true // Action bar
        );

        player.sendMessage(
            Text.literal(String.format(
                "a Power-Up! %s%s afor e%d seconds!",
                type.color, type.name, type.duration
            )),
            false
        );
    }

    public static boolean hasPowerUp(UUID uuid, PowerUpType type) {
        List<ActivePowerUp> powerUps = activePowerUps.get(uuid);
        if (powerUps == null) return false;

        return powerUps.stream()
            .anyMatch(p -> p.type == type && !p.isExpired());
    }

    public static List<ActivePowerUp> getActivePowerUps(UUID uuid) {
        cleanupExpired(uuid);
        return activePowerUps.getOrDefault(uuid, new ArrayList<>());
    }

    private static void cleanupExpired(UUID uuid) {
        List<ActivePowerUp> powerUps = activePowerUps.get(uuid);
        if (powerUps != null) {
            powerUps.removeIf(ActivePowerUp::isExpired);
            if (powerUps.isEmpty()) {
                activePowerUps.remove(uuid);
            }
        }
    }

    public static void clearPowerUps(UUID uuid) {
        activePowerUps.remove(uuid);
    }

    public static String getPowerUpStatus(UUID uuid) {
        List<ActivePowerUp> powerUps = getActivePowerUps(uuid);
        if (powerUps.isEmpty()) {
            return "7No active power-ups";
        }

        StringBuilder sb = new StringBuilder("aActive Power-Ups: ");
        for (ActivePowerUp powerUp : powerUps) {
            long remaining = (powerUp.expiryTime - System.currentTimeMillis()) / 1000;
            sb.append(String.format("%s%s7(%ds) ",
                powerUp.type.color,
                powerUp.type.name.split(" ")[1], // Short name
                remaining
            ));
        }
        return sb.toString().trim();
    }
}
