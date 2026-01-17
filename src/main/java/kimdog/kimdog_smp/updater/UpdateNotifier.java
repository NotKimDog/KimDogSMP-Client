package kimdog.kimdog_smp.updater;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Notifies admins/ops about available updates when they join
 */
public class UpdateNotifier {

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // Wait a bit for the player to fully load, then notify about updates
            server.execute(() -> {
                try {
                    Thread.sleep(3000); // 3 second delay

                    if (UpdateChecker.isUpdateAvailable()) {
                        Text updateNotification = UpdateChecker.getUpdateNotificationText();
                        if (updateNotification != null) {
                            player.sendMessage(updateNotification, false);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
    }
}
