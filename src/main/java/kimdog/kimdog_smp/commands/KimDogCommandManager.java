package kimdog.kimdog_smp.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Master command handler for all KimDog SMP commands
 * All commands are organized under /kimdog
 */
public class KimDogCommandManager {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Main /kimdog command
        dispatcher.register(
            literal("kimdog")
                .executes(ctx -> showMainHelp(ctx.getSource()))

                .then(
                    literal("help")
                    .executes(ctx -> showMainHelp(ctx.getSource()))
                )
        );
    }

    private static int showMainHelp(ServerCommandSource src) {
        sendMessage(src, "");
        sendMessage(src, Text.literal("╔════════════════════════════════════════════════════════════╗").formatted(Formatting.DARK_AQUA));
        sendMessage(src, Text.literal("║         [PEAK] KimDog SMP - Command Center [PEAK]           ║").formatted(Formatting.AQUA));
        sendMessage(src, Text.literal("╚════════════════════════════════════════════════════════════╝").formatted(Formatting.DARK_AQUA));
        sendMessage(src, "");

        sendMessage(src, Text.literal("⛏️  VeinMiner System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog veinminer              - Show VeinMiner help");
        sendMessage(src, "  /kimdog veinminer stats        - View your mining statistics");
        sendMessage(src, "  /kimdog veinminer toggle       - Toggle VeinMiner on/off");
        sendMessage(src, "");

        sendMessage(src, Text.literal("📋 Quest System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog quest                  - View active quest");
        sendMessage(src, "  /kimdog quest new              - Generate new quest");
        sendMessage(src, "");

        sendMessage(src, Text.literal("⬆️  Upgrade System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog upgrade                - Show upgrade GUI");
        sendMessage(src, "  /kimdog upgrade buy <name>     - Purchase upgrade");
        sendMessage(src, "");

        sendMessage(src, Text.literal("🚪 Double Door System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog doubledoor help        - Double door commands");
        sendMessage(src, "");

        sendMessage(src, Text.literal("✈️  Fly System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog fly                    - Toggle creative flight");
        sendMessage(src, "  /kimdog fly speed <0-1>        - Set flight speed");
        sendMessage(src, "");

        sendMessage(src, Text.literal("💬 Chat System:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog chatmessages           - Chat message commands");
        sendMessage(src, "");

        sendMessage(src, Text.literal("🛡️  AntiCheat:").formatted(Formatting.YELLOW));
        sendMessage(src, "  /kimdog anticheat status       - View AntiCheat status");
        sendMessage(src, "");

        sendMessage(src, Text.literal("╔════════════════════════════════════════════════════════════╗").formatted(Formatting.DARK_AQUA));
        sendMessage(src, Text.literal("║  Type /kimdog <system> help for more information           ║").formatted(Formatting.AQUA));
        sendMessage(src, Text.literal("╚════════════════════════════════════════════════════════════╝").formatted(Formatting.DARK_AQUA));
        sendMessage(src, "");

        return 1;
    }

    private static void sendMessage(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg), false);
    }

    private static void sendMessage(ServerCommandSource src, Text msg) {
        src.sendFeedback(() -> msg, false);
    }
}
