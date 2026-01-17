package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Daily rewards system for consistent miners
 */
public class DailyRewardSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Daily");
    private static final Map<UUID, DailyData> dailyData = new HashMap<>();

    public static class DailyData {
        public LocalDate lastClaim;
        public int consecutiveDays;
        public int blocksToday;
        public boolean milestoneReached;

        public DailyData() {
            this.lastClaim = LocalDate.now().minusDays(2); // Ensure first claim works
            this.consecutiveDays = 0;
            this.blocksToday = 0;
            this.milestoneReached = false;
        }
    }

    public static void tryClaimDailyReward(ServerPlayerEntity player, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableDailyRewards) return;

        UUID uuid = player.getUuid();
        DailyData data = dailyData.computeIfAbsent(uuid, k -> new DailyData());
        LocalDate today = LocalDate.now();

        // Check if already claimed today
        if (data.lastClaim != null && data.lastClaim.equals(today)) {
            return; // Already claimed
        }

        // Check streak
        if (data.lastClaim != null && data.lastClaim.plusDays(1).equals(today)) {
            data.consecutiveDays++;
        } else if (data.lastClaim != null && !data.lastClaim.equals(today)) {
            data.consecutiveDays = 1; // Reset streak
        }

        data.lastClaim = today;
        data.blocksToday = 0;
        data.milestoneReached = false;

        // Calculate reward
        int reward = cfg.dailyRewardEmeralds + (data.consecutiveDays * cfg.dailyStreakBonus);

        // Give reward
        kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.addEmeralds(uuid, reward);

        // Notify player
        player.sendMessage(
            Text.literal(String.format(" Daily Reward! +%d emeralds! (Streak: %d days)",
                reward, data.consecutiveDays))
                .formatted(Formatting.GREEN),
            false
        );

        LOGGER.info("{} claimed daily reward: {} emeralds (streak: {})",
            player.getName().getString(), reward, data.consecutiveDays);
    }

    public static void trackBlocksMined(ServerPlayerEntity player, int blocks, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableDailyRewards) return;

        UUID uuid = player.getUuid();
        DailyData data = dailyData.computeIfAbsent(uuid, k -> new DailyData());

        data.blocksToday += blocks;

        // Check milestone
        if (!data.milestoneReached && data.blocksToday >= cfg.dailyMilestoneBlocks) {
            data.milestoneReached = true;

            int bonus = cfg.dailyRewardEmeralds;
            kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.addEmeralds(uuid, bonus);

            player.sendMessage(
                Text.literal(String.format(" Daily Milestone! Mined %d blocks today! +%d emeralds!",
                    cfg.dailyMilestoneBlocks, bonus))
                    .formatted(Formatting.GOLD),
                false
            );

            LOGGER.info("{} reached daily milestone: {} blocks",
                player.getName().getString(), data.blocksToday);
        }
    }

    public static DailyData getPlayerData(UUID uuid) {
        return dailyData.computeIfAbsent(uuid, k -> new DailyData());
    }
}
