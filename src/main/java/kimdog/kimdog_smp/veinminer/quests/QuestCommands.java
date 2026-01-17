package kimdog.kimdog_smp.veinminer.quests;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class QuestCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("kimdog")
                    .then(
                        literal("quest")
                            .executes(ctx -> showQuestScreen(ctx.getSource()))

                            .then(
                                literal("new")
                                .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            net.minecraft.server.network.ServerPlayerEntity player = src.getPlayer();
                            if (player == null) {
                                sendError(src, "❌ This command must be run by a player!");
                                return 0;
                            }
                            VeinMinerQuests.generateNewQuest(player);
                            sendSuccess(src, "✨ New quest generated!");
                            return showQuestScreen(src);
                        })
                    )

                    .then(
                        literal("info")
                        .executes(ctx -> showQuestScreen(ctx.getSource()))
                    )
                    )
            );
        });
    }

    private static int showQuestScreen(ServerCommandSource src) {
        net.minecraft.server.network.ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            sendError(src, "❌ This command must be run by a player!");
            return 0;
        }

        String questScreen = VeinMinerQuests.getDetailedQuestScreen(player);
        for (String line : questScreen.split("\n")) {
            src.sendFeedback(() -> Text.literal(line), false);
        }
        return 1;
    }

    private static void sendSuccess(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.GREEN), false);
    }

    private static void sendError(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.RED), false);
    }
}
