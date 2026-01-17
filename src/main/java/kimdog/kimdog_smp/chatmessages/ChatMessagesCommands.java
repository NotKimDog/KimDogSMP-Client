package kimdog.kimdog_smp.chatmessages;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ChatMessagesCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("chatmessages")
                    .executes(ctx -> showHelp(ctx.getSource()))

                    .then(
                        literal("reload")
                        .executes(ctx -> {
                            ChatMessagesConfig.load();
                            sendSuccess(ctx.getSource(), "♻️ Chat Messages config reloaded successfully!");
                            return 1;
                        })
                    )

                    .then(
                        literal("help")
                        .executes(ctx -> showHelp(ctx.getSource()))
                    )

                    .then(
                        literal("toggle")
                        .executes(ctx -> {
                            ChatMessagesConfig config = ChatMessagesConfig.get();
                            config.enabled = !config.enabled;
                            ChatMessagesConfig.save();
                            String status = config.enabled ? "🟢 ENABLED" : "🔴 DISABLED";
                            sendSuccess(ctx.getSource(), "💬 Chat Messages " + status);
                            return 1;
                        })
                    )
            );
        });
    }

    private static int showHelp(ServerCommandSource src) {
        sendMessage(src, "");
        sendMessage(src, Text.literal("╔══════════════════════════════════════╗").formatted(Formatting.DARK_AQUA));
        sendMessage(src, Text.literal("║   💬 Chat Messages Commands Help 💬   ║").formatted(Formatting.AQUA));
        sendMessage(src, Text.literal("╚══════════════════════════════════════╝").formatted(Formatting.DARK_AQUA));
        sendMessage(src, "");

        sendMessage(src, Text.literal("/chatmessages reload").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Reload configuration")));
        sendMessage(src, Text.literal("/chatmessages toggle").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Toggle chat messages on/off")));
        sendMessage(src, Text.literal("/chatmessages help").formatted(Formatting.YELLOW)
            .append(Text.literal(" - Show this help message")));

        sendMessage(src, "");
        sendMessage(src, Text.literal("Configuration:").formatted(Formatting.AQUA));
        sendMessage(src, "  📁 Location: config/kimdog_smp/chatmessages.json");
        sendMessage(src, "  ⏱️ Settings: enabled, messageIntervalSeconds, randomOrder");
        sendMessage(src, "  📝 Edit the JSON to add/remove messages");

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
