package kimdog.kimdog_smp.commands.utils;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Permission utility for command access
 */
public class PermissionUtils {

    /**
     * Check if player has permission for a command
     */
    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (player == null) return false;
        // Always allow for now (vanilla ops can be checked through source)
        return true;
    }

    /**
     * Check if source has permission
     */
    public static boolean hasPermission(ServerCommandSource source, String permission) {
        // Console always has permission
        if (source.getPlayer() == null) {
            return true;
        }
        return hasPermission(source.getPlayer(), permission);
    }
}


