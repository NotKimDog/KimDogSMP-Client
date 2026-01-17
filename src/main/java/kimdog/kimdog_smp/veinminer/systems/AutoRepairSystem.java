package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-repair system - repairs tools using player XP
 */
public class AutoRepairSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner AutoRepair");

    public static boolean tryAutoRepair(ServerPlayerEntity player, ItemStack tool, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableAutoRepair) return false;
        if (!tool.isDamageable()) return false;

        int durability = tool.getMaxDamage() - tool.getDamage();
        int maxDurability = tool.getMaxDamage();
        int durabilityPercent = (durability * 100) / maxDurability;

        // Check if durability is below threshold
        if (durabilityPercent > cfg.autoRepairThreshold) {
            return false;
        }

        // Check if player has enough XP
        int playerXpLevel = player.experienceLevel;
        if (playerXpLevel < cfg.autoRepairCost) {
            player.sendMessage(
                Text.literal(" Auto-Repair failed! Need " + cfg.autoRepairCost + " XP levels to repair.")
                    .formatted(Formatting.RED),
                true
            );
            return false;
        }

        // Repair the tool
        tool.setDamage(0);
        player.addExperienceLevels(-cfg.autoRepairCost);

        player.sendMessage(
            Text.literal(" Auto-Repaired tool! Cost: " + cfg.autoRepairCost + " XP levels")
                .formatted(Formatting.GREEN),
            true
        );

        LOGGER.info("Auto-repaired {} for player {} (cost: {} levels)",
            tool.getName().getString(), player.getName().getString(), cfg.autoRepairCost);

        return true;
    }

    public static void checkAndWarn(ServerPlayerEntity player, ItemStack tool, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableAutoRepair) return;
        if (!tool.isDamageable()) return;

        int durability = tool.getMaxDamage() - tool.getDamage();
        int maxDurability = tool.getMaxDamage();
        int durabilityPercent = (durability * 100) / maxDurability;

        // Warn when getting low
        if (durabilityPercent <= cfg.autoRepairThreshold + 5 && durabilityPercent > cfg.autoRepairThreshold) {
            player.sendMessage(
                Text.literal(" Tool durability low (" + durabilityPercent + "%) - Auto-repair will activate soon!")
                    .formatted(Formatting.YELLOW),
                true
            );
        }
    }
}
