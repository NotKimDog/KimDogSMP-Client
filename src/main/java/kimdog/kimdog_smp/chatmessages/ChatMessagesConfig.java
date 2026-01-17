package kimdog.kimdog_smp.chatmessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatMessagesConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog ChatMessages");
    private static final Path CONFIG_DIR = Path.of("config/kimdog_smp");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("chatmessages.json").toFile();
    private static ChatMessagesConfig instance;

    public boolean enabled = true;
    public int messageIntervalSeconds = 300; // 5 minutes
    public boolean randomOrder = true;

    // Hardcoded messages - cannot be modified in config
    public static final List<String> HARDCODED_MESSAGES = Arrays.asList(
        "💎 Welcome to KimDog SMP!",
        "⛏️ Use /veinminer help for VeinMiner commands",
        "🏆 Check the advancements tab for VeinMiner achievements!",
        "💬 Have fun and enjoy the server!",
        "🎮 Don't forget to use /veinminer stats to track your progress",
        "⭐ Break ore blocks to activate the VeinMiner feature",
        "🔥 Keep your streak going for XP multipliers!",
        "🍀 You have a 15% chance of lucky hits for bonus XP",
        "📊 Use /veinminer stats [player] to see anyone's stats",
        "✨ Enchanted pickaxes work better with VeinMiner!"
    );

    public ChatMessagesConfig() {
    }

    public static ChatMessagesConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static List<String> getMessages() {
        return new ArrayList<>(HARDCODED_MESSAGES);
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                Gson gson = new Gson();
                FileReader reader = new FileReader(CONFIG_FILE);
                instance = gson.fromJson(reader, ChatMessagesConfig.class);
                reader.close();
                LOGGER.info("✅ Chat Messages config loaded");
            } else {
                instance = new ChatMessagesConfig();
                save();
                LOGGER.info("📝 Chat Messages config created with defaults");
            }
        } catch (IOException e) {
            LOGGER.error("❌ Error loading Chat Messages config: {}", e.getMessage());
            instance = new ChatMessagesConfig();
        }
    }

    public static void save() {
        try {
            CONFIG_DIR.toFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(gson.toJson(instance));
            writer.close();
            LOGGER.info("✅ Chat Messages config saved");
        } catch (IOException e) {
            LOGGER.error("❌ Error saving Chat Messages config: {}", e.getMessage());
        }
    }
}
