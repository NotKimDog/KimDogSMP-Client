package kimdog.kimdog_smp.veinminer;

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
import java.util.List;

public class VeinMinerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final Path CONFIG_DIR = Path.of("config/kimdog_smp");
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("veinminer.json").toFile();
    private static VeinMinerConfig instance;

    public boolean enabled = true;
    public String activation = "always"; // "sneak" | "toggle" | "always"
    public int maxBlocks = 64;
    public int maxRange = 32;
    public boolean requireTool = true;
    public boolean requirePickaxe = true; // Specifically check for pickaxe, not other tools
    public boolean checkOreTag = true; // Check if block is in #minecraft:mineable/pickaxe tag
    public boolean mineAllOres = true; // Mine all adjacent ore types (not just same ore)
    public boolean silkTouchRespect = true;
    public boolean applyFortune = true;
    public List<String> blacklist = new ArrayList<>();
    public boolean permissionRequired = false;
    public String permissionNode = "kimdog.veinminer.use";
    public String toggleKey = "key.kimdog.veinminer.toggle";

    // New enhancement settings
    public boolean consolidateDrops = true; // Drop all items in one spot at origin
    public int breakDelayMs = 50; // Delay between breaking blocks (ms) - prevents lag
    public boolean enableParticles = true; // Show fancy particles
    public boolean playSoundEffects = true; // Play sound effects on break
    public int particleCount = 25; // Number of particles per block
    public String particleEffect = "rainbow"; // "rainbow" | "ore" | "enchant" | "smoke"

    // NEW FEATURE FLAGS
    public boolean enableEnchantmentBonuses = true; // Efficiency/Unbreaking/Fortune bonuses
    public boolean enableStreakSystem = true; // Consecutive vein mining streak multiplier
    public int streakXpMultiplier = 25; // XP bonus per streak (e.g., 5 streak = 5*25% = 125% XP)
    public boolean enableStatTracking = true; // Track player vein mining stats
    public boolean enableCascadeEffect = true; // Blocks break in wave pattern
    public boolean enableAchievements = true; // Mini achievement system
    public boolean enableLuckSystem = true; // Chance-based bonuses
    public int luckChance = 15; // % chance for lucky bonus (extra blocks/XP)

    // QUEST SETTINGS
    public boolean enableQuests = true; // Enable daily quests
    public boolean displayQuestOnScreen = true; // Show quest progress on screen

    // ORE EFFECT SETTINGS
    public boolean enableOreSpecificEffects = true; // Each ore has unique particles/sounds
    public boolean enableOreParticles = true; // Show ore-specific particle effects
    public boolean enableOreSounds = true; // Play ore-specific sounds

    // NEW UNIQUE FEATURES
    public boolean enableComboSystem = true; // Break blocks in sequence for combo
    public int comboTimeout = 3000; // ms before combo resets
    public boolean enablePowerUps = true; // Random power-ups during mining
    public boolean enableVeinPreview = true; // Show outline of vein before mining
    public boolean enableMagnetMode = true; // Auto-collect drops
    public int magnetRadius = 8; // Blocks radius for auto-collect

    // VISUAL ENHANCEMENTS
    public boolean enableBlockHighlight = true; // Highlight connected ore blocks
    public boolean enableRainbowTrail = true; // Rainbow trail effect
    public boolean enableExplosionEffect = false; // Explosion visual (no damage)
    public boolean enableLightningEffect = false; // Lightning strike visual on large veins
    public int lightningVeinSize = 20; // Min vein size for lightning

    // AUDIO ENHANCEMENTS
    public boolean enableSoundVariations = true; // Different sounds per ore type
    public boolean enableAmbientSounds = true; // Background mining sounds
    public double soundVolume = 1.0; // 0.0-1.0

    // PERFORMANCE & REWARDS
    public boolean enableAutoSmelt = false; // Auto-smelt ores (requires fortune)
    public boolean enableDoubleDropChance = true; // Chance for 2x drops
    public int doubleDropChance = 5; // % chance
    public boolean enableXpBoost = true; // Boost XP from vein mining
    public double xpBoostMultiplier = 1.5; // 1.5x XP

    // COOLDOWN & LIMITS
    public boolean enableCooldown = false; // Cooldown between vein mines
    public int cooldownSeconds = 5; // Cooldown duration
    public boolean enableDurabilityMultiplier = true; // Tools last longer
    public double durabilityMultiplier = 0.5; // 50% durability per block (instead of 100%)

    // SPECIAL MODES
    public boolean enableGhostMode = false; // Mine without breaking blocks (creative only)
    public boolean enableChainReaction = true; // Breaking one triggers nearby veins
    public int chainReactionRadius = 3; // Blocks radius for chain reaction
    public boolean enableTeleportDrops = true; // Drops teleport to player inventory

    // SEASONAL/EVENT FEATURES
    public boolean enableSeasonalEffects = true; // Special effects during holidays
    public boolean enableRareOreBonus = true; // Extra rewards for rare ores
    public int rareOreMultiplier = 2; // 2x rewards for diamonds/emeralds

    // AUTO-REPAIR SYSTEM
    public boolean enableAutoRepair = true; // Auto-repair tools using XP
    public int autoRepairCost = 5; // XP levels per repair
    public int autoRepairThreshold = 10; // Repair when durability below this %

    // VEIN PREVIEW SYSTEM
    public boolean enableVeinPreviewParticles = true; // Show particles around vein before mining
    public int previewDuration = 2000; // ms to show preview
    public String previewParticleType = "glow"; // Particle type for preview

    // SMART MODE
    public boolean enableSmartMode = true; // Auto-adjust settings based on vein size
    public int smartModeSmallVein = 10; // Veins under this = fast mode
    public int smartModeLargeVein = 30; // Veins over this = slow mode for effect

    // LEADERBOARDS
    public boolean enableLeaderboards = true; // Track top miners
    public boolean announceLeaderboardChanges = true; // Announce when someone tops the board
    public int leaderboardSize = 10; // Top X players to track

    // DAILY REWARDS
    public boolean enableDailyRewards = true; // Give daily mining rewards
    public int dailyRewardEmeralds = 50; // Emeralds for first vein of the day
    public int dailyStreakBonus = 10; // Extra emeralds per consecutive day
    public int dailyMilestoneBlocks = 500; // Blocks to mine for daily milestone

    // MULTIPLAYER BONUSES
    public boolean enableMultiplayerBonus = true; // Bonus when mining near others
    public int multiplayerRadius = 32; // Blocks radius to detect other players
    public float multiplayerXpBonus = 0.25f; // 25% XP bonus per nearby player
    public int maxMultiplayerBonus = 2; // Max number of players to count

    // ORE RARITY DETECTOR
    public boolean enableRarityDetector = true; // Announce rare ore finds
    public boolean detectDiamond = true; // Announce diamond finds
    public boolean detectEmerald = true; // Announce emerald finds
    public boolean detectAncientDebris = true; // Announce ancient debris
    public int rarityAnnounceRadius = 50; // Announce to players within radius

    // MINING MILESTONES
    public boolean enableMilestones = true; // Reward at specific block counts
    public int[] milestoneBlocks = {100, 500, 1000, 5000, 10000, 50000}; // Milestone thresholds
    public int[] milestoneRewards = {25, 100, 250, 500, 1000, 5000}; // Emerald rewards

    // TOOL ENHANCEMENT
    public boolean enableToolEnhancement = true; // Tools get better with use
    public int enhancementLevels = 5; // Max enhancement level
    public int blocksPerEnhancement = 1000; // Blocks to mine per level

    // VEIN LINKING
    public boolean enableVeinLinking = true; // Link nearby veins together
    public int veinLinkDistance = 5; // Max distance to link veins
    public boolean showVeinLinks = true; // Show particle links between veins

    // PRECISION MODE
    public boolean enablePrecisionMode = false; // Click blocks to add/remove from vein
    public String precisionModeKey = "key.sneak"; // Key to enable precision mode

    // FATIGUE SYSTEM (balance feature)
    public boolean enableFatigue = false; // Mining gets slower over time
    public int fatigueBlockThreshold = 200; // Blocks before fatigue kicks in
    public int fatigueRecoveryTime = 300; // Seconds to recover from fatigue

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static VeinMinerConfig INSTANCE;

    public VeinMinerConfig() {
    }

    public static VeinMinerConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            File dir = CONFIG_DIR.toFile();
            if (!dir.exists()) {
                LOGGER.info(" Creating config directory: {}", CONFIG_DIR);
                dir.mkdirs();
            }
            File f = CONFIG_FILE;
            if (!f.exists()) {
                LOGGER.info("  Creating default configuration file: {}", CONFIG_FILE);
                INSTANCE = new VeinMinerConfig();
                save();
                logConfig();
                return;
            }
            try (FileReader reader = new FileReader(f)) {
                INSTANCE = GSON.fromJson(reader, VeinMinerConfig.class);
                LOGGER.info(" Configuration loaded from: {}", CONFIG_FILE);
                logConfig();
            }
        } catch (Exception e) {
            LOGGER.error(" Error loading config, using defaults:", e);
            INSTANCE = new VeinMinerConfig();
        }
    }

    public static void save() {
        try {
            File dir = CONFIG_DIR.toFile();
            if (!dir.exists()) Files.createDirectories(CONFIG_DIR);
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(get(), writer);
                LOGGER.info(" Configuration saved to: {}", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.error(" Error saving config:", e);
        }
    }

    private static void logConfig() {
        VeinMinerConfig cfg = get();
        LOGGER.info("");
        LOGGER.info("          VeinMiner Configuration Summary                  ");
        LOGGER.info("");
        LOGGER.info(" Enabled: {}", cfg.enabled);
        LOGGER.info("  Activation Mode: {}", cfg.activation);
        LOGGER.info(" Max Blocks: {}", cfg.maxBlocks);
        LOGGER.info(" Max Range: {}", cfg.maxRange);
        LOGGER.info(" Require Tool: {}", cfg.requireTool);
        LOGGER.info("  Require Pickaxe: {}", cfg.requirePickaxe);
        LOGGER.info("  Check Ore Tag: {}", cfg.checkOreTag);
        LOGGER.info(" Silk Touch Respect: {}", cfg.silkTouchRespect);
        LOGGER.info(" Apply Fortune: {}", cfg.applyFortune);
        LOGGER.info(" Blacklisted Blocks: {}", cfg.blacklist.size());
        LOGGER.info("");
        LOGGER.info(" Enhancement Settings:");
        LOGGER.info(" Consolidate Drops: {}", cfg.consolidateDrops);
        LOGGER.info("  Break Delay: {}ms", cfg.breakDelayMs);
        LOGGER.info(" Enable Particles: {}", cfg.enableParticles);
        LOGGER.info(" Play Sound Effects: {}", cfg.playSoundEffects);
        LOGGER.info(" Particle Effect: {} ({} per block)", cfg.particleEffect, cfg.particleCount);
        LOGGER.info("");
    }
}
