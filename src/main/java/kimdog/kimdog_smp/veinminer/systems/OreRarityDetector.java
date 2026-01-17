package kimdog.kimdog_smp.veinminer.systems;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Ore rarity detector - announces rare ore finds
 */
public class OreRarityDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Rarity");

    public static void checkRareOres(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, kimdog.kimdog_smp.veinminer.VeinMinerConfig cfg) {
        if (!cfg.enableRarityDetector) return;

        int diamondCount = 0;
        int emeraldCount = 0;
        int ancientDebrisCount = 0;

        // Count rare ores in vein
        for (BlockPos pos : positions) {
            BlockState state = world.getBlockState(pos);
            String blockName = Registries.BLOCK.getId(state.getBlock()).getPath();

            if (blockName.contains("diamond_ore")) diamondCount++;
            else if (blockName.contains("emerald_ore")) emeraldCount++;
            else if (blockName.contains("ancient_debris")) ancientDebrisCount++;
        }

        // Announce rare finds
        if (cfg.detectDiamond && diamondCount >= 5) {
            announceRareFind(player, world, "Diamond", diamondCount, cfg.rarityAnnounceRadius);
        }

        if (cfg.detectEmerald && emeraldCount >= 3) {
            announceRareFind(player, world, "Emerald", emeraldCount, cfg.rarityAnnounceRadius);
        }

        if (cfg.detectAncientDebris && ancientDebrisCount >= 2) {
            announceRareFind(player, world, "Ancient Debris", ancientDebrisCount, cfg.rarityAnnounceRadius);
        }
    }

    private static void announceRareFind(ServerPlayerEntity finder, ServerWorld world, String oreName, int count, int radius) {
        String message = String.format("💎 %s found a vein with %d %s!",
            finder.getName().getString(), count, oreName);

        // Announce to nearby players
        world.getPlayers(p -> p.squaredDistanceTo(finder) <= radius * radius)
            .forEach(p -> {
                p.sendMessage(
                    Text.literal(message).formatted(Formatting.LIGHT_PURPLE),
                    false
                );
            });

        LOGGER.info("Rare ore find: {} found {} {} ores",
            finder.getName().getString(), count, oreName);
    }

    public static void announceGlobalRareFind(ServerPlayerEntity finder, ServerWorld world, String oreName) {
        String message = String.format("✨ %s found %s! Congratulations!",
            finder.getName().getString(), oreName);

        world.getServer().getPlayerManager().broadcast(
            Text.literal(message).formatted(Formatting.GOLD),
            false
        );
    }
}
