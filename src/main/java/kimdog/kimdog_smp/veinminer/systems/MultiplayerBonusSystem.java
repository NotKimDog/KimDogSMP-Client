package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Multiplayer bonus system - rewards mining near other players
 */
public class MultiplayerBonusSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Multiplayer");

    public static float getMultiplayerXpBonus(ServerPlayerEntity player, ServerWorld world, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableMultiplayerBonus) return 1.0f;

        // Find nearby players
        Box searchBox = Box.of(player.getBlockPos().toCenterPos(), cfg.multiplayerRadius, cfg.multiplayerRadius, cfg.multiplayerRadius);
        List<ServerPlayerEntity> nearbyPlayers = world.getPlayers(p ->
            p != player &&
            p.squaredDistanceTo(player) <= cfg.multiplayerRadius * cfg.multiplayerRadius
        );

        int nearbyCount = Math.min(nearbyPlayers.size(), cfg.maxMultiplayerBonus);

        if (nearbyCount > 0) {
            float bonus = 1.0f + (nearbyCount * cfg.multiplayerXpBonus);
            LOGGER.debug("{} mining with {} nearby players - {}x XP bonus",
                player.getName().getString(), nearbyCount, bonus);
            return bonus;
        }

        return 1.0f;
    }

    public static void notifyMultiplayerBonus(ServerPlayerEntity player, int nearbyCount, float bonusMultiplier) {
        if (nearbyCount > 0) {
            player.sendMessage(
                Text.literal(String.format(" Mining with %d player%s! %.0f%% XP Bonus!",
                    nearbyCount, nearbyCount > 1 ? "s" : "", (bonusMultiplier - 1.0f) * 100))
                    .formatted(Formatting.AQUA),
                true
            );
        }
    }
}
