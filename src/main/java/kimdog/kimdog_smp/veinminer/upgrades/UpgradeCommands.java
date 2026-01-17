package kimdog.kimdog_smp.veinminer.upgrades;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class UpgradeCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("kimdog")
                    .then(
                        literal("upgrade")
                            .executes(ctx -> showUpgradeGUI(ctx.getSource()))

                            .then(
                                literal("gui")
                        .executes(ctx -> showUpgradeGUI(ctx.getSource()))
                    )

                    .then(
                        literal("buy")
                        .then(
                            argument("upgrade", string())
                            .executes(ctx -> purchaseUpgrade(ctx.getSource(),
                                ctx.getArgument("upgrade", String.class)))
                        )
                    )

                    .then(
                        literal("info")
                        .executes(ctx -> showUpgradeInfo(ctx.getSource()))
                    )
                    )
            );
        });
    }

    private static int showUpgradeGUI(ServerCommandSource src) {
        net.minecraft.server.network.ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        UpgradeManager.PlayerUpgrades upgrades = UpgradeManager.getPlayerUpgrades(player.getUuid());

        sendMessage(src, "");
        sendMessage(src, Text.literal("").formatted(Formatting.DARK_GREEN));
        sendMessage(src, Text.literal("                      VEINMINER UPGRADES  ").formatted(Formatting.GREEN));
        sendMessage(src, Text.literal("").formatted(Formatting.DARK_GREEN));
        sendMessage(src, "");

        sendMessage(src, Text.literal(" Emeralds: " + upgrades.emeralds).formatted(Formatting.GREEN));
        sendMessage(src, "");

        // Max Blocks Upgrade
        sendMessage(src, formatUpgrade("Max Blocks", upgrades.maxBlocksLevel, 5,
                UpgradeManager.UpgradeCosts.MAX_BLOCKS_COSTS,
                upgrades.maxBlocksLevel < 5 ? 64 + (upgrades.maxBlocksLevel * 89) + "  " + (64 + ((upgrades.maxBlocksLevel + 1) * 89)) : "MAX"));

        // Max Range Upgrade
        sendMessage(src, formatUpgrade("Max Range", upgrades.maxRangeLevel, 5,
                UpgradeManager.UpgradeCosts.MAX_RANGE_COSTS,
                upgrades.maxRangeLevel < 5 ? 32 + (upgrades.maxRangeLevel * 44) + "  " + (32 + ((upgrades.maxRangeLevel + 1) * 44)) : "MAX"));

        // XP Multiplier Upgrade
        sendMessage(src, formatUpgrade("XP Multiplier", upgrades.xpMultiplierLevel, 5,
                UpgradeManager.UpgradeCosts.XP_MULTIPLIER_COSTS,
                upgrades.xpMultiplierLevel < 5 ? String.format("%.1fx  %.1fx",
                    1.0 + (upgrades.xpMultiplierLevel * 0.4),
                    1.0 + ((upgrades.xpMultiplierLevel + 1) * 0.4)) : "MAX"));

        // Speed Upgrade
        sendMessage(src, formatUpgrade("Mining Speed", upgrades.speedLevel, 5,
                UpgradeManager.UpgradeCosts.SPEED_COSTS,
                upgrades.speedLevel < 5 ? (upgrades.speedLevel + 1) + "x  " + (upgrades.speedLevel + 2) + "x" : "MAX"));

        // Particle Upgrade
        sendMessage(src, formatUpgrade("Particle Effects", upgrades.particleLevel, 3,
                UpgradeManager.UpgradeCosts.PARTICLE_COSTS,
                upgrades.particleLevel < 3 ? "Level " + upgrades.particleLevel + "  Level " + (upgrades.particleLevel + 1) : "MAX"));

        sendMessage(src, "");
        sendMessage(src, Text.literal("").formatted(Formatting.DARK_GREEN));
        sendMessage(src, Text.literal("/upgrade buy <name>  - Purchase an upgrade").formatted(Formatting.YELLOW));
        sendMessage(src, Text.literal("Names: maxblocks, maxrange, xpmultiplier, speed, particles").formatted(Formatting.GRAY));
        sendMessage(src, Text.literal("").formatted(Formatting.DARK_GREEN));
        sendMessage(src, "");

        return 1;
    }

    private static int purchaseUpgrade(ServerCommandSource src, String upgradeName) {
        net.minecraft.server.network.ServerPlayerEntity player = src.getPlayer();
        if (player == null) return 0;

        UpgradeManager.PlayerUpgrades upgrades = UpgradeManager.getPlayerUpgrades(player.getUuid());

        int nextCost = 0;
        int nextLevel = 0;
        String displayName = "";

        switch (upgradeName.toLowerCase()) {
            case "maxblocks":
                if (upgrades.maxBlocksLevel >= 5) {
                    sendError(src, " Max Blocks upgrade is already maxed!");
                    return 0;
                }
                nextCost = UpgradeManager.UpgradeCosts.MAX_BLOCKS_COSTS[upgrades.maxBlocksLevel];
                displayName = "Max Blocks";
                nextLevel = upgrades.maxBlocksLevel + 1;
                break;
            case "maxrange":
                if (upgrades.maxRangeLevel >= 5) {
                    sendError(src, " Max Range upgrade is already maxed!");
                    return 0;
                }
                nextCost = UpgradeManager.UpgradeCosts.MAX_RANGE_COSTS[upgrades.maxRangeLevel];
                displayName = "Max Range";
                nextLevel = upgrades.maxRangeLevel + 1;
                break;
            case "xpmultiplier":
                if (upgrades.xpMultiplierLevel >= 5) {
                    sendError(src, " XP Multiplier upgrade is already maxed!");
                    return 0;
                }
                nextCost = UpgradeManager.UpgradeCosts.XP_MULTIPLIER_COSTS[upgrades.xpMultiplierLevel];
                displayName = "XP Multiplier";
                nextLevel = upgrades.xpMultiplierLevel + 1;
                break;
            case "speed":
                if (upgrades.speedLevel >= 5) {
                    sendError(src, " Mining Speed upgrade is already maxed!");
                    return 0;
                }
                nextCost = UpgradeManager.UpgradeCosts.SPEED_COSTS[upgrades.speedLevel];
                displayName = "Mining Speed";
                nextLevel = upgrades.speedLevel + 1;
                break;
            case "particles":
                if (upgrades.particleLevel >= 3) {
                    sendError(src, " Particle Effects upgrade is already maxed!");
                    return 0;
                }
                nextCost = UpgradeManager.UpgradeCosts.PARTICLE_COSTS[upgrades.particleLevel];
                displayName = "Particle Effects";
                nextLevel = upgrades.particleLevel + 1;
                break;
            default:
                sendError(src, " Unknown upgrade: " + upgradeName);
                return 0;
        }

        if (upgrades.emeralds < nextCost) {
            sendError(src, " You need " + (nextCost - upgrades.emeralds) + " more emeralds!");
            sendError(src, "   Current: " + upgrades.emeralds + " | Cost: " + nextCost);
            return 0;
        }

        if (UpgradeManager.purchaseUpgrade(player.getUuid(), upgradeName)) {
            sendSuccess(src, " Upgraded " + displayName + " to level " + nextLevel + "!");
            sendSuccess(src, " Emeralds: " + upgrades.emeralds);
            return 1;
        }

        return 0;
    }

    private static int showUpgradeInfo(ServerCommandSource src) {
        sendMessage(src, "");
        sendMessage(src, Text.literal("UPGRADE SYSTEM INFO").formatted(Formatting.GREEN));
        sendMessage(src, "");
        sendMessage(src, " Max Blocks - Increases max blocks per vein (64  512)");
        sendMessage(src, " Max Range - Increases scanning range (32  256)");
        sendMessage(src, " XP Multiplier - Increases XP rewards (1x  3x)");
        sendMessage(src, " Mining Speed - Mine faster (1x  6x)");
        sendMessage(src, " Particles - Enhanced visual effects");
        sendMessage(src, "");
        sendMessage(src, " Emeralds are earned by mining ore!");
        sendMessage(src, "");

        return 1;
    }

    private static Text formatUpgrade(String name, int currentLevel, int maxLevel, int[] costs, String benefit) {
        String status = currentLevel >= maxLevel ? "c[MAX]" : "b[LEVEL " + currentLevel + "/" + maxLevel + "]";
        int cost = currentLevel < maxLevel ? costs[currentLevel] : 0;
        String costText = currentLevel < maxLevel ? " 6Cost: " + cost + " emeralds" : "";

        return Text.literal(status + " a" + name + costText + " f" + benefit);
    }

    private static void sendMessage(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg), false);
    }

    private static void sendMessage(ServerCommandSource src, Text msg) {
        src.sendFeedback(() -> msg, false);
    }

    private static void sendSuccess(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.GREEN), false);
    }

    private static void sendError(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.RED), false);
    }
}
