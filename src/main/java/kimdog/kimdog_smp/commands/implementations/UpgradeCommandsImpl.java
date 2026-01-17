package kimdog.kimdog_smp.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class UpgradeCommandsImpl {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Upgrade commands are auto-registered through UpgradeCommands.register()
    }
}
