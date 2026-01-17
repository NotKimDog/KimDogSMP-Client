package kimdog.kimdog_smp.veinminer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VeinMinerStats {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final Map<java.util.UUID, PlayerStats> playerStats = new HashMap<>();

    public static class PlayerStats {
        public long totalBlocksMined = 0;
        public long totalXpGained = 0;
        public int largestVeinSize = 0;
        public String rariestOreFound = "none";
        public int diamondVeinsFound = 0;
        public int emeraldVeinsFound = 0;
        public int currentStreak = 0;
        public int bestStreak = 0;
        public long totalOreValue = 0; // In terms of XP equivalent
    }

    public static PlayerStats getStats(ServerPlayerEntity player) {
        return playerStats.computeIfAbsent(player.getUuid(), k -> new PlayerStats());
    }

    public static void recordVein(ServerPlayerEntity player, int blocksDestroyed, int xpGained, String oreType) {
        PlayerStats stats = getStats(player);

        stats.totalBlocksMined += blocksDestroyed;
        stats.totalXpGained += xpGained;
        stats.currentStreak++;

        if (blocksDestroyed > stats.largestVeinSize) {
            stats.largestVeinSize = blocksDestroyed;
        }

        if (stats.currentStreak > stats.bestStreak) {
            stats.bestStreak = stats.currentStreak;
        }

        // Track rare ores
        if (oreType.contains("diamond")) {
            stats.diamondVeinsFound++;
            stats.rariestOreFound = "diamond";
            stats.totalOreValue += xpGained * 2;
        } else if (oreType.contains("emerald")) {
            stats.emeraldVeinsFound++;
            stats.rariestOreFound = "emerald";
            stats.totalOreValue += xpGained * 2;
        } else {
            stats.totalOreValue += xpGained;
        }
    }

    public static void resetStreak() {
        playerStats.values().forEach(stats -> stats.currentStreak = 0);
    }

    public static String getStatsString(ServerPlayerEntity player) {
        PlayerStats stats = getStats(player);
        return String.format(
            "⛏️ VEINMINER STATS - %s ⛏️\n" +
            "📊 Total Blocks Mined: %d\n" +
            "💫 Total XP Gained: %d\n" +
            "🏆 Largest Vein: %d blocks\n" +
            "💎 Diamond Veins: %d\n" +
            "✨ Emerald Veins: %d\n" +
            "🔥 Current Streak: %d\n" +
            "⚡ Best Streak: %d\n" +
            "🪨 Rarest Ore: %s",
            player.getName().getString(),
            stats.totalBlocksMined,
            stats.totalXpGained,
            stats.largestVeinSize,
            stats.diamondVeinsFound,
            stats.emeraldVeinsFound,
            stats.currentStreak,
            stats.bestStreak,
            stats.rariestOreFound
        );
    }
}
