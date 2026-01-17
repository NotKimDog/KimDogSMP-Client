package kimdog.kimdog_smp.anticheat;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class AntiCheatCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("anticheat")
                    .executes(ctx -> showHelp(ctx.getSource()))

                    .then(
                        literal("status")
                        .executes(ctx -> {
                            AntiCheatConfig config = AntiCheatConfig.get();
                            ServerCommandSource src = ctx.getSource();

                            sendMessage(src, "");
                            sendMessage(src, Text.literal("╔══════════════════════════════════════╗").formatted(Formatting.DARK_RED));
                            sendMessage(src, Text.literal("║    🛡️ AntiCheat Status 🛡️           ║").formatted(Formatting.RED));
                            sendMessage(src, Text.literal("╚══════════════════════════════════════╝").formatted(Formatting.DARK_RED));
                            sendMessage(src, "");

                            sendMessage(src, Text.literal("🟢 Enabled: ").formatted(Formatting.RED).append(
                                Text.literal(config.enableAntiCheat ? "YES" : "NO").formatted(config.enableAntiCheat ? Formatting.GREEN : Formatting.RED)));
                            sendMessage(src, Text.literal("⚡ Speed Hack Detection: ").formatted(Formatting.YELLOW).append(
                                Text.literal(config.enableSpeedHack ? "ON" : "OFF").formatted(Formatting.AQUA)));
                            sendMessage(src, Text.literal("🚀 Fly Hack Detection: ").formatted(Formatting.YELLOW).append(
                                Text.literal(config.enableFlyHack ? "ON" : "OFF").formatted(Formatting.AQUA)));
                            sendMessage(src, Text.literal("👊 Reach Hack Detection: ").formatted(Formatting.YELLOW).append(
                                Text.literal(config.enableReachHack ? "ON" : "OFF").formatted(Formatting.AQUA)));
                            sendMessage(src, "");

                            return 1;
                        })
                    )

                    .then(
                        literal("toggle")
                        .executes(ctx -> {
                            AntiCheatConfig config = AntiCheatConfig.get();
                            config.enableAntiCheat = !config.enableAntiCheat;
                            AntiCheatConfig.save();

                            String status = config.enableAntiCheat ? "🟢 ENABLED" : "🔴 DISABLED";
                            sendSuccess(ctx.getSource(), "🛡️ AntiCheat " + status);
                            return 1;
                        })
                    )

                    .then(
                        literal("reload")
                        .executes(ctx -> {
                            AntiCheatConfig.load();
                            sendSuccess(ctx.getSource(), "♻️ AntiCheat config reloaded!");
                            return 1;
                        })
                    )

                    .then(
                        literal("help")
                        .executes(ctx -> showHelp(ctx.getSource()))
                    )
            );
        });
    }

    private static int showHelp(ServerCommandSource src) {
        sendMessage(src, "");
        sendMessage(src, Text.literal("╔══════════════════════════════════════╗").formatted(Formatting.DARK_RED));
        sendMessage(src, Text.literal("║   🛡️ AntiCheat Commands Help 🛡️    ║").formatted(Formatting.RED));
        sendMessage(src, Text.literal("╚══════════════════════════════════════╝").formatted(Formatting.DARK_RED));
        sendMessage(src, "");

        sendMessage(src, Text.literal("/anticheat status").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Show AntiCheat status")));
        sendMessage(src, Text.literal("/anticheat toggle").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Toggle AntiCheat on/off")));
        sendMessage(src, Text.literal("/anticheat reload").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Reload configuration")));
        sendMessage(src, Text.literal("/anticheat help").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Show this help")));

        sendMessage(src, "");
        sendMessage(src, Text.literal("Detections:").formatted(Formatting.AQUA));
        sendMessage(src, "  ⚡ Speed Hack - Detects impossible movement speeds");
        sendMessage(src, "  🚀 Fly Hack - Detects illegal upward movement");
        sendMessage(src, "  👊 Reach Hack - Detects impossible block reach");
        sendMessage(src, "  📊 Logging - All violations logged to file");
        sendMessage(src, "  🔔 Admin Notifications - Real-time cheater alerts");

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
}
