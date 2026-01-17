package kimdog.kimdog_smp.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * GitHub Release Update Checker for KimDog SMP
 * Checks for new versions on GitHub and notifies users
 */
public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP Update Checker");

    // GitHub Configuration
    private static final String GITHUB_USER = "NotKimDog"; // Your GitHub username
    private static final String GITHUB_REPO = "KimDogSMP-Client"; // Your repository name
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases/latest";
    private static final String GITHUB_RELEASES_URL = "https://github.com/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases";

    private static String currentVersion;
    private static UpdateInfo latestUpdate = null;
    private static boolean updateAvailable = false;
    private static boolean checkComplete = false;

    /**
     * Initialize and start checking for updates
     */
    public static void initialize(String version) {
        currentVersion = version;
        LOGGER.info("🔍 Initializing Update Checker...");
        LOGGER.info("   Current Version: {}", currentVersion);
        LOGGER.info("   GitHub Repository: {}/{}", GITHUB_USER, GITHUB_REPO);

        // Check for updates asynchronously to not block server startup
        CompletableFuture.runAsync(() -> {
            try {
                checkForUpdates();
            } catch (Exception e) {
                LOGGER.warn("Failed to check for updates: {}", e.getMessage());
            }
        });
    }

    /**
     * Check GitHub for latest release
     */
    private static void checkForUpdates() {
        try {
            LOGGER.info("📡 Checking for updates from GitHub...");

            URL url = new URL(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "KimDogSMP-Updater");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseUpdateInfo(response.toString());
            } else if (responseCode == 404) {
                LOGGER.info("ℹ️  No releases found on GitHub yet");
            } else {
                LOGGER.warn("⚠️  Update check returned code: {}", responseCode);
            }

            checkComplete = true;

        } catch (Exception e) {
            LOGGER.warn("⚠️  Could not check for updates: {}", e.getMessage());
            checkComplete = true;
        }
    }

    /**
     * Parse GitHub API response
     */
    private static void parseUpdateInfo(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String latestVersion = json.get("tag_name").getAsString();
            String releaseName = json.get("name").getAsString();
            String releaseBody = json.has("body") ? json.get("body").getAsString() : "No description available";
            String downloadUrl = json.get("html_url").getAsString();
            String publishedAt = json.get("published_at").getAsString();

            // Remove 'v' prefix if present
            String cleanLatestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
            String cleanCurrentVersion = currentVersion.replace("-DEV", "").replace("-SNAPSHOT", "");

            latestUpdate = new UpdateInfo(
                cleanLatestVersion,
                releaseName,
                releaseBody,
                downloadUrl,
                publishedAt
            );

            // Compare versions
            if (isNewerVersion(cleanLatestVersion, cleanCurrentVersion)) {
                updateAvailable = true;
                LOGGER.info("✨ ════════════════════════════════════════════════════════════════");
                LOGGER.info("✨ NEW UPDATE AVAILABLE!");
                LOGGER.info("✨ ════════════════════════════════════════════════════════════════");
                LOGGER.info("✨ Current Version: {}", currentVersion);
                LOGGER.info("✨ Latest Version:  {}", latestVersion);
                LOGGER.info("✨ Release Name:    {}", releaseName);
                LOGGER.info("✨ Download:        {}", GITHUB_RELEASES_URL);
                LOGGER.info("✨ ════════════════════════════════════════════════════════════════");
            } else {
                LOGGER.info("✅ You are running the latest version!");
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to parse update information: {}", e.getMessage());
        }
    }

    /**
     * Compare two version strings
     * Returns true if newVersion is newer than currentVersion
     */
    private static boolean isNewerVersion(String newVersion, String currentVersion) {
        try {
            // Split versions by dots
            String[] newParts = newVersion.split("\\.");
            String[] currentParts = currentVersion.split("\\.");

            int maxLength = Math.max(newParts.length, currentParts.length);

            for (int i = 0; i < maxLength; i++) {
                int newPart = i < newParts.length ? parseVersionPart(newParts[i]) : 0;
                int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

                if (newPart > currentPart) {
                    return true;
                } else if (newPart < currentPart) {
                    return false;
                }
            }

            return false; // Versions are equal
        } catch (Exception e) {
            LOGGER.warn("Error comparing versions: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse a version part to integer, removing non-numeric characters
     */
    private static int parseVersionPart(String part) {
        try {
            // Remove any non-numeric characters
            String numericPart = part.replaceAll("[^0-9]", "");
            return numericPart.isEmpty() ? 0 : Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get update notification text for players/admins
     */
    public static Text getUpdateNotificationText() {
        if (!updateAvailable || latestUpdate == null) {
            return null;
        }

        return Text.literal("")
            .append(Text.literal("═══════════════════════════════════════════════\n").formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal("🎮 ").formatted(Formatting.YELLOW))
            .append(Text.literal("KimDog SMP Update Available!\n").formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal("═══════════════════════════════════════════════\n").formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal("Current Version: ").formatted(Formatting.GRAY))
            .append(Text.literal(currentVersion + "\n").formatted(Formatting.RED))
            .append(Text.literal("Latest Version: ").formatted(Formatting.GRAY))
            .append(Text.literal(latestUpdate.version + "\n").formatted(Formatting.GREEN, Formatting.BOLD))
            .append(Text.literal("\n"))
            .append(Text.literal("📝 Release: ").formatted(Formatting.YELLOW))
            .append(Text.literal(latestUpdate.name + "\n").formatted(Formatting.WHITE))
            .append(Text.literal("\n"))
            .append(Text.literal("Download: ").formatted(Formatting.GRAY))
            .append(Text.literal(GITHUB_RELEASES_URL + "\n").formatted(Formatting.AQUA, Formatting.UNDERLINE))
            .append(Text.literal("\n"))
            .append(Text.literal("═══════════════════════════════════════════════").formatted(Formatting.GOLD, Formatting.BOLD));
    }

    /**
     * Get simple update message for console
     */
    public static String getUpdateMessage() {
        if (!updateAvailable || latestUpdate == null) {
            return null;
        }

        return String.format("Update available: %s -> %s. Download: %s",
            currentVersion, latestUpdate.version, GITHUB_RELEASES_URL);
    }

    // Getters
    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public static boolean isCheckComplete() {
        return checkComplete;
    }

    public static UpdateInfo getLatestUpdate() {
        return latestUpdate;
    }

    public static String getDownloadUrl() {
        return GITHUB_RELEASES_URL;
    }

    /**
     * Update information class
     */
    public static class UpdateInfo {
        public final String version;
        public final String name;
        public final String description;
        public final String downloadUrl;
        public final String publishedAt;

        public UpdateInfo(String version, String name, String description, String downloadUrl, String publishedAt) {
            this.version = version;
            this.name = name;
            this.description = description;
            this.downloadUrl = downloadUrl;
            this.publishedAt = publishedAt;
        }
    }
}
