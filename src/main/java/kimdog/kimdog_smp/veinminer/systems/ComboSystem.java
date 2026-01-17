package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Combo system for VeinMiner - rewards consecutive mining
 */
public class ComboSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Combo");
    private static final Map<UUID, ComboData> combos = new HashMap<>();

    public static class ComboData {
        public int combo = 0;
        public long lastMineTime = 0;
        public int totalBlocks = 0;
        public double xpMultiplier = 1.0;
    }

    public static void onVeinMined(ServerPlayerEntity player, int blocksMined, int xpGained) {
        UUID uuid = player.getUuid();
        ComboData data = combos.computeIfAbsent(uuid, k -> new ComboData());
        long now = System.currentTimeMillis();

        // Check if combo expired (3 seconds default)
        if (now - data.lastMineTime > 3000) {
            if (data.combo > 0) {
                sendComboEndMessage(player, data);
            }
            data.combo = 0;
            data.totalBlocks = 0;
            data.xpMultiplier = 1.0;
        }

        // Increment combo
        data.combo++;
        data.totalBlocks += blocksMined;
        data.lastMineTime = now;

        // Calculate XP multiplier (increases with combo)
        data.xpMultiplier = 1.0 + (data.combo * 0.1); // 10% per combo

        // Send combo notification
        sendComboMessage(player, data, blocksMined);

        // Milestone rewards
        checkMilestones(player, data);
    }

    private static void sendComboMessage(ServerPlayerEntity player, ComboData data, int blocks) {
        String comboText = getComboTier(data.combo);
        String color = getComboColor(data.combo);

        player.sendMessage(
            Text.literal(String.format(
                "%s COMBO x%d %s | +%d blocks | %.1fx XP",
                color, data.combo, comboText, blocks, data.xpMultiplier
            )),
            true // Action bar
        );
    }

    private static void sendComboEndMessage(ServerPlayerEntity player, ComboData data) {
        if (data.combo >= 3) {
            player.sendMessage(
                Text.literal(String.format(
                    "e Combo Ended! x%d | Total: %d blocks | XP Bonus: +%.0f%%",
                    data.combo, data.totalBlocks, (data.xpMultiplier - 1.0) * 100
                )).formatted(Formatting.YELLOW),
                false
            );
        }
    }

    private static String getComboTier(int combo) {
        if (combo >= 50) return "clGODLY";
        if (combo >= 30) return "6lLEGENDARY";
        if (combo >= 20) return "5lEPIC";
        if (combo >= 10) return "blRARE";
        if (combo >= 5) return "alGOOD";
        return "f";
    }

    private static String getComboColor(int combo) {
        if (combo >= 50) return "c";
        if (combo >= 30) return "6";
        if (combo >= 20) return "5";
        if (combo >= 10) return "b";
        if (combo >= 5) return "a";
        return "f";
    }

    private static void checkMilestones(ServerPlayerEntity player, ComboData data) {
        int combo = data.combo;
        if (combo == 5) {
            player.sendMessage(Text.literal("a Milestone! 5 Combo!").formatted(Formatting.GREEN), false);
        } else if (combo == 10) {
            player.sendMessage(Text.literal("b Milestone! 10 Combo! Keep going!").formatted(Formatting.AQUA), false);
        } else if (combo == 25) {
            player.sendMessage(Text.literal("5 Milestone! 25 Combo! Amazing!").formatted(Formatting.LIGHT_PURPLE), false);
        } else if (combo == 50) {
            player.sendMessage(Text.literal("c LEGENDARY! 50 COMBO! UNSTOPPABLE!").formatted(Formatting.RED), false);
        } else if (combo == 100) {
            player.sendMessage(Text.literal("4l GODLIKE! 100 COMBO! YOU'RE A LEGEND!").formatted(Formatting.DARK_RED), false);
        }
    }

    public static double getXpMultiplier(UUID uuid) {
        ComboData data = combos.get(uuid);
        return data != null ? data.xpMultiplier : 1.0;
    }

    public static int getCurrentCombo(UUID uuid) {
        ComboData data = combos.get(uuid);
        return data != null ? data.combo : 0;
    }

    public static void resetCombo(UUID uuid) {
        combos.remove(uuid);
    }
}
