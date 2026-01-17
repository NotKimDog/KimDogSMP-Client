package kimdog.kimdog_smp.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class QuestCommandsImpl {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Quest commands are auto-registered through QuestCommands.register()
    }
}
