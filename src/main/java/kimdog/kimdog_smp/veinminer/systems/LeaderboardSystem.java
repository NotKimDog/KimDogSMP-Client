package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Leaderboard system for top miners
 */
public class LeaderboardSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Leaderboards");
    private static final Map<UUID, LeaderboardEntry> leaderboard = new LinkedHashMap<>();

    public static class LeaderboardEntry {
        public String playerName;
        public int totalBlocks;
        public int totalVeins;
        public long lastUpdate;

        public LeaderboardEntry(String playerName, int totalBlocks, int totalVeins) {
            this.playerName = playerName;
            this.totalBlocks = totalBlocks;
            this.totalVeins = totalVeins;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public static void updateLeaderboard(ServerPlayerEntity player, int blocksMined, ServerWorld world) {
        UUID uuid = player.getUuid();
        String name = player.getName().getString();

        LeaderboardEntry entry = leaderboard.computeIfAbsent(uuid, k -> new LeaderboardEntry(name, 0, 0));

        int oldBlocks = entry.totalBlocks;
        entry.totalBlocks += blocksMined;
        entry.totalVeins++;
        entry.lastUpdate = System.currentTimeMillis();

        // Check if moved up in rankings
        int oldRank = getRank(uuid, oldBlocks);
        int newRank = getRank(uuid, entry.totalBlocks);

        if (oldRank > newRank && newRank <= 3) {
            announceLeaderboardChange(player, newRank, world);
        }

        LOGGER.debug("{} updated: {} blocks ({} veins)", name, entry.totalBlocks, entry.totalVeins);
    }

    private static int getRank(UUID uuid, int blocks) {
        List<Map.Entry<UUID, LeaderboardEntry>> sorted = leaderboard.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().totalBlocks, a.getValue().totalBlocks))
            .toList();

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return sorted.size() + 1;
    }

    private static void announceLeaderboardChange(ServerPlayerEntity player, int rank, ServerWorld world) {
        String rankText = switch (rank) {
            case 1 -> "§6🥇 #1 - TOP MINER!";
            case 2 -> "§7🥈 #2";
            case 3 -> "§c🥉 #3";
            default -> "#" + rank;
        };

        world.getServer().getPlayerManager().broadcast(
            Text.literal(String.format("⛏️ %s is now %s on the mining leaderboard!",
                player.getName().getString(), rankText))
                .formatted(Formatting.GOLD),
            false
        );
    }

    public static String getLeaderboardText(int maxEntries) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l⛏ TOP MINERS ⛏§r\n");
        sb.append("§7━━━━━━━━━━━━━━━━━━━━\n");

        List<Map.Entry<UUID, LeaderboardEntry>> sorted = leaderboard.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().totalBlocks, a.getValue().totalBlocks))
            .limit(maxEntries)
            .toList();

        for (int i = 0; i < sorted.size(); i++) {
            LeaderboardEntry entry = sorted.get(i).getValue();
            String medal = switch (i) {
                case 0 -> "§6🥇";
                case 1 -> "§7🥈";
                case 2 -> "§c🥉";
                default -> "§7" + (i + 1) + ".";
            };

            sb.append(String.format("%s §e%s §7- §a%,d blocks §7(%d veins)\n",
                medal, entry.playerName, entry.totalBlocks, entry.totalVeins));
        }

        sb.append("§7━━━━━━━━━━━━━━━━━━━━");
        return sb.toString();
    }

    public static int getPlayerRank(UUID uuid) {
        return getRank(uuid, leaderboard.getOrDefault(uuid, new LeaderboardEntry("", 0, 0)).totalBlocks);
    }

    public static LeaderboardEntry getPlayerEntry(UUID uuid) {
        return leaderboard.get(uuid);
    }
}
