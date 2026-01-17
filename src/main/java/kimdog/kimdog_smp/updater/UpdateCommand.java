package kimdog.kimdog_smp.updater;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Command to check for updates manually
 */
public class UpdateCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("kimdogsmp")
                .then(CommandManager.literal("update")
                    .executes(UpdateCommand::checkUpdate)
                    .then(CommandManager.literal("download")
                        .executes(UpdateCommand::downloadUpdate))
                    .then(CommandManager.literal("status")
                        .executes(UpdateCommand::downloadStatus)))
                .then(CommandManager.literal("version")
                    .executes(UpdateCommand::showVersion))
            );
        });
    }

    private static int checkUpdate(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!UpdateChecker.isCheckComplete()) {
            source.sendFeedback(() -> Text.literal("⏳ Update check in progress, please wait...").formatted(Formatting.YELLOW), false);
            return 1;
        }

        if (UpdateChecker.isUpdateAvailable()) {
            Text updateNotification = UpdateChecker.getUpdateNotificationText();
            if (updateNotification != null) {
                source.sendFeedback(() -> updateNotification, false);
            }

            // Show download option
            source.sendFeedback(() -> Text.literal(""), false);
            source.sendFeedback(() -> Text.literal("💡 To automatically download and install:")
                .formatted(Formatting.YELLOW), false);
            source.sendFeedback(() -> Text.literal("   /kimdogsmp update download")
                .formatted(Formatting.AQUA, Formatting.BOLD), false);
            source.sendFeedback(() -> Text.literal("   (Requires OP level 3)")
                .formatted(Formatting.GRAY, Formatting.ITALIC), false);
        } else {
            source.sendFeedback(() -> Text.literal("✅ You are running the latest version!").formatted(Formatting.GREEN), false);

            UpdateChecker.UpdateInfo latestUpdate = UpdateChecker.getLatestUpdate();
            if (latestUpdate != null) {
                source.sendFeedback(() -> Text.literal("Current Version: " + latestUpdate.version).formatted(Formatting.GRAY), false);
            }
        }

        return 1;
    }

    private static int showVersion(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("🎮 KimDog SMP Mod Info").formatted(Formatting.YELLOW, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Version: ").formatted(Formatting.GRAY)
            .append(Text.literal("1.0.0-DEV").formatted(Formatting.GREEN)), false);
        source.sendFeedback(() -> Text.literal("Minecraft: ").formatted(Formatting.GRAY)
            .append(Text.literal("1.21").formatted(Formatting.GREEN)), false);
        source.sendFeedback(() -> Text.literal("Platform: ").formatted(Formatting.GRAY)
            .append(Text.literal("Fabric").formatted(Formatting.GREEN)), false);
        source.sendFeedback(() -> Text.literal("GitHub: ").formatted(Formatting.GRAY)
            .append(Text.literal(UpdateChecker.getDownloadUrl()).formatted(Formatting.AQUA, Formatting.UNDERLINE)), false);
        source.sendFeedback(() -> Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD), false);

        if (UpdateChecker.isUpdateAvailable()) {
            source.sendFeedback(() -> Text.literal("\n⚠️  An update is available! Use /kimdogsmp update to see details.").formatted(Formatting.YELLOW), false);
        }

        return 1;
    }

    private static int downloadUpdate(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();


        if (!UpdateChecker.isUpdateAvailable()) {
            source.sendFeedback(() -> Text.literal("ℹ️  No update available to download!")
                .formatted(Formatting.YELLOW), false);
            return 0;
        }

        if (AutoDownloader.isDownloadInProgress()) {
            source.sendFeedback(() -> Text.literal("⏳ Download already in progress!")
                .formatted(Formatting.YELLOW), false);
            source.sendFeedback(() -> Text.literal("   Progress: " + String.format("%.1f%%", AutoDownloader.getDownloadProgress()))
                .formatted(Formatting.GRAY), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
            .formatted(Formatting.GOLD, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("🔄 Starting Automatic Update Download...")
            .formatted(Formatting.YELLOW, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
            .formatted(Formatting.GOLD, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("📦 Target Version: " + UpdateChecker.getLatestUpdate().version)
            .formatted(Formatting.GREEN), false);
        source.sendFeedback(() -> Text.literal("⏬ Downloading from GitHub...")
            .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("⚠️  This process will:")
            .formatted(Formatting.YELLOW), false);
        source.sendFeedback(() -> Text.literal("   1. Download the new version")
            .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("   2. Delete the old mod file")
            .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal("   3. Install the new version")
            .formatted(Formatting.GRAY), false);
        source.sendFeedback(() -> Text.literal(""), false);
        source.sendFeedback(() -> Text.literal("💡 Use /kimdogsmp update status to check progress")
            .formatted(Formatting.AQUA), false);
        source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
            .formatted(Formatting.GOLD, Formatting.BOLD), false);

        // Start download asynchronously
        AutoDownloader.downloadAndInstallUpdate().thenAccept(success -> {
            if (success) {
                source.sendFeedback(() -> Text.literal(""), false);
                source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
                source.sendFeedback(() -> Text.literal("✅ UPDATE INSTALLED SUCCESSFULLY!")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
                source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
                source.sendFeedback(() -> Text.literal(""), false);
                source.sendFeedback(() -> Text.literal("⚠️  SERVER RESTART REQUIRED!")
                    .formatted(Formatting.YELLOW, Formatting.BOLD), false);
                source.sendFeedback(() -> Text.literal("   The new version will be active after restart.")
                    .formatted(Formatting.GRAY), false);
                source.sendFeedback(() -> Text.literal(""), false);
                source.sendFeedback(() -> Text.literal("Old mod files have been automatically removed.")
                    .formatted(Formatting.GREEN), false);
                source.sendFeedback(() -> Text.literal("═══════════════════════════════════════════════")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);
            } else {
                source.sendFeedback(() -> Text.literal(""), false);
                source.sendFeedback(() -> Text.literal("❌ Update download failed!")
                    .formatted(Formatting.RED, Formatting.BOLD), false);
                source.sendFeedback(() -> Text.literal("Status: " + AutoDownloader.getDownloadStatus())
                    .formatted(Formatting.GRAY), false);
                source.sendFeedback(() -> Text.literal("Please try again or download manually from GitHub.")
                    .formatted(Formatting.YELLOW), false);
            }
        });

        return 1;
    }

    private static int downloadStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (AutoDownloader.isDownloadInProgress()) {
            source.sendFeedback(() -> Text.literal("═══════════════════════════════════")
                .formatted(Formatting.GOLD), false);
            source.sendFeedback(() -> Text.literal("⏬ Download Status")
                .formatted(Formatting.YELLOW, Formatting.BOLD), false);
            source.sendFeedback(() -> Text.literal("═══════════════════════════════════")
                .formatted(Formatting.GOLD), false);
            source.sendFeedback(() -> Text.literal("Status: In Progress")
                .formatted(Formatting.YELLOW), false);
            source.sendFeedback(() -> Text.literal("Progress: " + String.format("%.1f%%", AutoDownloader.getDownloadProgress()))
                .formatted(Formatting.GREEN), false);
            source.sendFeedback(() -> Text.literal("Current: " + AutoDownloader.getDownloadStatus())
                .formatted(Formatting.GRAY), false);
            source.sendFeedback(() -> Text.literal("═══════════════════════════════════")
                .formatted(Formatting.GOLD), false);
        } else {
            String status = AutoDownloader.getDownloadStatus();
            if (status == null || status.isEmpty()) {
                source.sendFeedback(() -> Text.literal("ℹ️  No download in progress")
                    .formatted(Formatting.GRAY), false);
            } else {
                source.sendFeedback(() -> Text.literal("Last Status: " + status)
                    .formatted(Formatting.GRAY), false);
            }
        }

        return 1;
    }
}
