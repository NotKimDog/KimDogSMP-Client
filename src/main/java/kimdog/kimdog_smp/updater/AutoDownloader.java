package kimdog.kimdog_smp.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

/**
 * Automatic mod downloader and updater
 * Downloads new versions from GitHub and replaces old files
 */
public class AutoDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog SMP Auto-Downloader");

    private static final String GITHUB_USER = "NotKimDog";
    private static final String GITHUB_REPO = "KimDogSMP-Client";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases/latest";

    private static boolean downloadInProgress = false;
    private static double downloadProgress = 0.0;
    private static String downloadStatus = "";

    /**
     * Download and install update automatically
     */
    public static CompletableFuture<Boolean> downloadAndInstallUpdate() {
        if (downloadInProgress) {
            LOGGER.warn("Download already in progress!");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                downloadInProgress = true;
                downloadStatus = "Fetching release information...";
                LOGGER.info(" Starting automatic update download...");

                // Get release info from GitHub
                String downloadUrl = getDownloadUrl();
                if (downloadUrl == null) {
                    LOGGER.error(" Could not find download URL!");
                    downloadStatus = "Failed: No download URL found";
                    return false;
                }

                LOGGER.info(" Download URL: {}", downloadUrl);
                downloadStatus = "Downloading new version...";

                // Download the new JAR file
                Path tempFile = downloadFile(downloadUrl);
                if (tempFile == null) {
                    LOGGER.error(" Failed to download file!");
                    downloadStatus = "Failed: Download error";
                    return false;
                }

                LOGGER.info(" Download complete: {}", tempFile.toString());
                downloadStatus = "Installing update...";

                // Install the update (replace old file)
                boolean installed = installUpdate(tempFile);
                if (installed) {
                    LOGGER.info(" Update installed successfully!");
                    LOGGER.info("  Server will need to restart to apply the update!");
                    downloadStatus = "Update ready - restart required";
                    return true;
                } else {
                    LOGGER.error(" Failed to install update!");
                    downloadStatus = "Failed: Installation error";
                    return false;
                }

            } catch (Exception e) {
                LOGGER.error(" Update failed: {}", e.getMessage());
                downloadStatus = "Failed: " + e.getMessage();
                return false;
            } finally {
                downloadInProgress = false;
            }
        });
    }

    /**
     * Get download URL from GitHub release
     */
    private static String getDownloadUrl() {
        try {
            URL url = new URL(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "KimDogSMP-Updater");

            if (connection.getResponseCode() != 200) {
                LOGGER.warn("GitHub API returned code: {}", connection.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray assets = json.getAsJsonArray("assets");

            if (assets.size() == 0) {
                LOGGER.warn("No assets found in release!");
                return null;
            }

            // Find the .jar file
            for (int i = 0; i < assets.size(); i++) {
                JsonObject asset = assets.get(i).getAsJsonObject();
                String name = asset.get("name").getAsString();
                if (name.endsWith(".jar") && !name.contains("sources")) {
                    return asset.get("browser_download_url").getAsString();
                }
            }

            LOGGER.warn("No suitable JAR file found in release!");
            return null;

        } catch (Exception e) {
            LOGGER.error("Failed to get download URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Download file from URL
     */
    private static Path downloadFile(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "KimDogSMP-Updater");

            int fileSize = connection.getContentLength();
            LOGGER.info(" File size: {} bytes ({} MB)", fileSize, fileSize / 1024.0 / 1024.0);

            // Create temp file
            Path tempFile = Files.createTempFile("kimdogsmp-update-", ".jar");
            LOGGER.info(" Downloading to: {}", tempFile.toString());

            // Download with progress
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(tempFile.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (fileSize > 0) {
                        downloadProgress = (totalBytesRead * 100.0) / fileSize;
                        if (totalBytesRead % (1024 * 1024) == 0) { // Log every MB
                            LOGGER.info(" Progress: {:.1f}%", downloadProgress);
                        }
                    }
                }
            }

            downloadProgress = 100.0;
            LOGGER.info(" Download complete!");
            return tempFile;

        } catch (Exception e) {
            LOGGER.error("Failed to download file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Install the update by replacing the old JAR file
     */
    private static boolean installUpdate(Path newFile) {
        try {
            // Find the mods folder
            Path modsFolder = findModsFolder();
            if (modsFolder == null) {
                LOGGER.error(" Could not find mods folder!");
                return false;
            }

            LOGGER.info(" Mods folder: {}", modsFolder.toString());

            // Find and delete old KimDog SMP JAR files
            deleteOldModFiles(modsFolder);

            // Copy new file to mods folder
            String newFileName = "kimdog-smp-" + UpdateChecker.getLatestUpdate().version + ".jar";
            Path targetPath = modsFolder.resolve(newFileName);

            Files.copy(newFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(" New file installed: {}", targetPath.toString());

            // Delete temp file
            Files.delete(newFile);
            LOGGER.info("  Temp file cleaned up");

            // Create update marker file for next restart
            createUpdateMarkerFile(modsFolder, newFileName);

            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to install update: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Find the mods folder
     */
    private static Path findModsFolder() {
        try {
            // Try to find mods folder relative to the current JAR
            Path currentPath = Paths.get("").toAbsolutePath();

            // Check common locations
            Path[] possiblePaths = {
                currentPath.resolve("mods"),
                currentPath.getParent().resolve("mods"),
                Paths.get("mods"),
                Paths.get("../mods"),
                Paths.get("../../mods")
            };

            for (Path path : possiblePaths) {
                if (Files.exists(path) && Files.isDirectory(path)) {
                    LOGGER.info(" Found mods folder: {}", path.toAbsolutePath());
                    return path.toAbsolutePath();
                }
            }

            // If not found, create it
            Path defaultModsPath = currentPath.resolve("mods");
            Files.createDirectories(defaultModsPath);
            LOGGER.info(" Created mods folder: {}", defaultModsPath.toAbsolutePath());
            return defaultModsPath.toAbsolutePath();

        } catch (Exception e) {
            LOGGER.error("Error finding mods folder: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete old KimDog SMP mod files
     */
    private static void deleteOldModFiles(Path modsFolder) {
        try {
            Files.list(modsFolder)
                .filter(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    // Match kimdog-smp-*.jar pattern (e.g., kimdog-smp-1.0.jar)
                    return fileName.startsWith("kimdog-smp-") && fileName.endsWith(".jar");
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        LOGGER.info("  Deleted old mod file: {}", path.getFileName());
                    } catch (IOException e) {
                        LOGGER.warn("  Could not delete {}: {}", path.getFileName(), e.getMessage());
                    }
                });
        } catch (Exception e) {
            LOGGER.warn("Error cleaning old files: {}", e.getMessage());
        }
    }

    /**
     * Create a marker file to indicate an update was installed
     */
    private static void createUpdateMarkerFile(Path modsFolder, String newFileName) {
        try {
            Path markerFile = modsFolder.resolve(".kimdogsmp-updated");
            Files.writeString(markerFile,
                "Updated to: " + newFileName + "\n" +
                "Timestamp: " + System.currentTimeMillis() + "\n" +
                "Please restart the server to apply changes!");
            LOGGER.info(" Created update marker file");
        } catch (Exception e) {
            LOGGER.warn("Could not create marker file: {}", e.getMessage());
        }
    }

    // Getters for status
    public static boolean isDownloadInProgress() {
        return downloadInProgress;
    }

    public static double getDownloadProgress() {
        return downloadProgress;
    }

    public static String getDownloadStatus() {
        return downloadStatus;
    }
}
