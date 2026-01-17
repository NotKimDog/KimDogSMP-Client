package kimdog.kimdog_smp.commands.utils;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import java.util.concurrent.CompletableFuture;

/**
 * Suggestion providers for command auto-completion
 */
public class CommandSuggestions {

    // VeinMiner subcommands
    public static final SuggestionProvider<ServerCommandSource> VEINMINER_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"stats", "toggle", "enable", "disable", "reload", "help"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Quest subcommands
    public static final SuggestionProvider<ServerCommandSource> QUEST_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"new", "info"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Upgrade subcommands
    public static final SuggestionProvider<ServerCommandSource> UPGRADE_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"gui", "buy", "info"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Upgrade names
    public static final SuggestionProvider<ServerCommandSource> UPGRADE_NAMES =
        (context, builder) -> {
            String[] suggestions = {"maxblocks", "maxrange", "xpmultiplier", "speed", "particles"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Fly subcommands
    public static final SuggestionProvider<ServerCommandSource> FLY_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"on", "off", "speed"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Speed values
    public static final SuggestionProvider<ServerCommandSource> FLY_SPEEDS =
        (context, builder) -> {
            String[] suggestions = {"0.1", "0.2", "0.5", "1.0", "1.5", "2.0"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // AntiCheat subcommands
    public static final SuggestionProvider<ServerCommandSource> ANTICHEAT_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"status", "toggle", "reload", "help"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // DoubleDoor subcommands
    public static final SuggestionProvider<ServerCommandSource> DOUBLEDOOR_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"help", "status"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };

    // Chat subcommands
    public static final SuggestionProvider<ServerCommandSource> CHAT_SUBCOMMANDS =
        (context, builder) -> {
            String[] suggestions = {"help", "status", "reload"};
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
}
