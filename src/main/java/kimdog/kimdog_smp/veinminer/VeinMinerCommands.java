package kimdog.kimdog_smp.veinminer;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import kimdog.kimdog_smp.veinminer.network.VeinMinerNetworking;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VeinMinerCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("kimdog")
                    .then(
                        literal("veinminer")
                            .executes(ctx -> showHelp(ctx.getSource())) // Show help by default

                    // Stats command
                    .then(
                        literal("stats")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerPlayerEntity player = src.getPlayer();
                            if (player == null) {
                                sendError(src, "❌ This command must be run by a player!");
                                return 0;
                            }
                            return showStats(src, player);
                        })
                        .then(
                            argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                ServerCommandSource src = ctx.getSource();
                                ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(name);
                                if (target == null) {
                                    sendError(src, "❌ Player not found: " + name);
                                    return 0;
                                }
                                return showStats(src, target);
                            })
                        )
                    )

                    // Toggle command
                    .then(
                        literal("toggle")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerPlayerEntity player = src.getPlayer();
                            if (player == null) {
                                sendError(src, "❌ This command must be run by a player!");
                                return 0;
                            }
                            boolean current = VeinMinerNetworking.getToggleForPlayer(player.getUuid());
                            VeinMinerNetworking.setToggleForPlayer(player.getUuid(), !current);
                            String status = !current ? "🟢 ENABLED" : "🔴 DISABLED";
                            sendSuccess(src, "⛏️ VeinMiner " + status);
                            return 1;
                        })
                        .then(
                            argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                ServerCommandSource src = ctx.getSource();
                                ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(name);
                                if (target == null) {
                                    sendError(src, "❌ Player not found: " + name);
                                    return 0;
                                }
                                boolean current = VeinMinerNetworking.getToggleForPlayer(target.getUuid());
                                VeinMinerNetworking.setToggleForPlayer(target.getUuid(), !current);
                                String status = !current ? "🟢 ENABLED" : "🔴 DISABLED";
                                sendSuccess(src, "⛏️ VeinMiner for " + target.getName().getString() + " " + status);
                                return 1;
                            })
                        )
                    )

                    // Enable command
                    .then(
                        literal("enable")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerPlayerEntity player = src.getPlayer();
                            if (player == null) {
                                sendError(src, "❌ This command must be run by a player!");
                                return 0;
                            }
                            VeinMinerNetworking.setToggleForPlayer(player.getUuid(), true);
                            sendSuccess(src, "🟢 VeinMiner ENABLED");
                            return 1;
                        })
                        .then(
                            argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                ServerCommandSource src = ctx.getSource();
                                ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(name);
                                if (target == null) {
                                    sendError(src, "❌ Player not found: " + name);
                                    return 0;
                                }
                                VeinMinerNetworking.setToggleForPlayer(target.getUuid(), true);
                                sendSuccess(src, "🟢 VeinMiner enabled for " + target.getName().getString());
                                return 1;
                            })
                        )
                    )

                    // Disable command
                    .then(
                        literal("disable")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerPlayerEntity player = src.getPlayer();
                            if (player == null) {
                                sendError(src, "❌ This command must be run by a player!");
                                return 0;
                            }
                            VeinMinerNetworking.setToggleForPlayer(player.getUuid(), false);
                            sendSuccess(src, "🔴 VeinMiner DISABLED");
                            return 1;
                        })
                        .then(
                            argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String name = StringArgumentType.getString(ctx, "player");
                                ServerCommandSource src = ctx.getSource();
                                ServerPlayerEntity target = src.getServer().getPlayerManager().getPlayer(name);
                                if (target == null) {
                                    sendError(src, "❌ Player not found: " + name);
                                    return 0;
                                }
                                VeinMinerNetworking.setToggleForPlayer(target.getUuid(), false);
                                sendSuccess(src, "🔴 VeinMiner disabled for " + target.getName().getString());
                                return 1;
                            })
                        )
                    )

                    // Reload command
                    .then(
                        literal("reload")
                        .executes(ctx -> {
                            VeinMinerConfig.load();
                            sendSuccess(ctx.getSource(), "♻️ VeinMiner config reloaded successfully!");
                            return 1;
                        })
                    )

                    // Help command
                    .then(
                        literal("help")
                        .executes(ctx -> showHelp(ctx.getSource()))
                    )
                    )
            );
        });
    }

    private static int showHelp(ServerCommandSource src) {
        sendMessage(src, "");
        sendMessage(src, Text.literal("╔══════════════════════════════════════╗").formatted(Formatting.DARK_GREEN));
        sendMessage(src, Text.literal("║     ⛏️ VeinMiner Commands Help ⛏️      ║").formatted(Formatting.GREEN));
        sendMessage(src, Text.literal("╚══════════════════════════════════════╝").formatted(Formatting.DARK_GREEN));
        sendMessage(src, "");

        sendMessage(src, Text.literal("/kimdog veinminer stats [player]").formatted(Formatting.YELLOW)
            .append(Text.literal(" - View your vein mining statistics")));
        sendMessage(src, Text.literal("/kimdog veinminer toggle [player]").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Toggle VeinMiner on/off")));
        sendMessage(src, Text.literal("/kimdog veinminer enable [player]").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Enable VeinMiner")));
        sendMessage(src, Text.literal("/veinminer disable [player]").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Disable VeinMiner")));
        sendMessage(src, Text.literal("/veinminer reload").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Reload configuration")));
        sendMessage(src, Text.literal("/veinminer help").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Show this help message")));

        sendMessage(src, "");
        sendMessage(src, Text.literal("Features:").formatted(Formatting.AQUA));
        sendMessage(src, "  ✨ Automatic vein detection with diagonal support");
        sendMessage(src, "  ⚡ Enchantment bonuses (Efficiency, Unbreaking, Fortune)");
        sendMessage(src, "  🔥 Streak multiplier system");
        sendMessage(src, "  🍀 Luck-based random bonuses");
        sendMessage(src, "  🏆 Achievement tracking");

        sendMessage(src, "");
        return 1;
    }

    private static int showStats(ServerCommandSource src, ServerPlayerEntity player) {
        VeinMinerStats.PlayerStats stats = VeinMinerStats.getStats(player);

        sendMessage(src, "");
        sendMessage(src, Text.literal("╔══════════════════════════════════════╗").formatted(Formatting.GOLD));
        sendMessage(src, Text.literal("║  ⛏️ VeinMiner Stats - " + player.getName().getString() + " ⛏️").formatted(Formatting.YELLOW));
        sendMessage(src, Text.literal("╚══════════════════════════════════════╝").formatted(Formatting.GOLD));
        sendMessage(src, "");

        sendMessage(src, Text.literal("📊 Total Blocks Mined: ").formatted(Formatting.AQUA)
            .append(Text.literal(stats.totalBlocksMined + "").formatted(Formatting.GREEN)));
        sendMessage(src, Text.literal("💫 Total XP Gained: ").formatted(Formatting.AQUA)
            .append(Text.literal(stats.totalXpGained + "").formatted(Formatting.GREEN)));
        sendMessage(src, Text.literal("🏆 Largest Vein: ").formatted(Formatting.AQUA)
            .append(Text.literal(stats.largestVeinSize + " blocks").formatted(Formatting.GREEN)));

        sendMessage(src, "");
        sendMessage(src, Text.literal("💎 Diamond Veins Found: ").formatted(Formatting.LIGHT_PURPLE)
            .append(Text.literal(stats.diamondVeinsFound + "").formatted(Formatting.DARK_PURPLE)));
        sendMessage(src, Text.literal("✨ Emerald Veins Found: ").formatted(Formatting.LIGHT_PURPLE)
            .append(Text.literal(stats.emeraldVeinsFound + "").formatted(Formatting.DARK_PURPLE)));
        sendMessage(src, Text.literal("🪨 Rarest Ore: ").formatted(Formatting.LIGHT_PURPLE)
            .append(Text.literal(stats.rariestOreFound.toUpperCase()).formatted(Formatting.DARK_PURPLE)));

        sendMessage(src, "");
        sendMessage(src, Text.literal("🔥 Current Streak: ").formatted(Formatting.RED)
            .append(Text.literal(stats.currentStreak + "").formatted(Formatting.GOLD)));
        sendMessage(src, Text.literal("⚡ Best Streak: ").formatted(Formatting.RED)
            .append(Text.literal(stats.bestStreak + "").formatted(Formatting.GOLD)));

        sendMessage(src, "");
        return 1;
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
