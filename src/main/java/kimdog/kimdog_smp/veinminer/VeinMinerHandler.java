package kimdog.kimdog_smp.veinminer;

import kimdog.kimdog_smp.veinminer.network.VeinMinerNetworking;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VeinMinerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld)) return;
            if (!(player instanceof ServerPlayerEntity)) return;

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            VeinMinerConfig cfg = VeinMinerConfig.get();
            if (!cfg.enabled) return;

            ItemStack main = player.getMainHandStack();
            if (main.isEmpty()) return;

            boolean activationOk = false;
            if ("always".equals(cfg.activation)) activationOk = true;
            else if ("sneak".equals(cfg.activation) && player.isSneaking()) activationOk = true;
            else if ("toggle".equals(cfg.activation)) {
                Boolean toggled = VeinMinerNetworking.getToggleForPlayer(serverPlayer.getUuid());
                activationOk = toggled != null && toggled;
            }
            if (!activationOk) return;

            if (cfg.requireTool) {
                Item item = main.getItem();
                if (cfg.requirePickaxe) {
                    TagKey<Item> pickaxeToolTag = TagKey.of(
                        Registries.ITEM.getKey(),
                        Identifier.ofVanilla("tools/pickaxes")
                    );

                    if (!item.getDefaultStack().isIn(pickaxeToolTag)) {
                        String itemName = Registries.ITEM.getId(item).toString().toLowerCase();
                        if (!itemName.contains("pickaxe")) {
                            LOGGER.debug(" Tool is not a pickaxe: {}", itemName);
                            return;
                        }
                    }
                }
            }

            if (state.isAir()) return;

            // CRITICAL CHECK: Only activate on ORE blocks
            String blockName = Registries.BLOCK.getId(state.getBlock()).getPath();
            if (!isOreBlock(blockName)) {
                LOGGER.debug(" Block {} is not an ore, VeinMiner not activated", blockName);
                return; // Not an ore, don't activate VeinMiner
            }

            // QOL: Show welcome message for new players
            kimdog.kimdog_smp.veinminer.qol.SmartNotifications.showWelcomeIfNew(serverPlayer);

            Predicate<BlockState> matchPredicate = createMatchPredicate(state);

            // Apply player upgrades
            kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.PlayerUpgrades upgrades =
                kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.getPlayerUpgrades(serverPlayer.getUuid());

            int maxBlocks = cfg.maxBlocks + (upgrades.maxBlocksLevel * 64); // Each level adds 64 blocks
            int maxRange = cfg.maxRange + (upgrades.maxRangeLevel * 32); // Each level adds 32 range

            List<BlockPos> vein = new ArrayList<>();
            List<BlockPos> found = VeinSearch.search(world, pos, state, maxBlocks - 1, maxRange, matchPredicate);
            vein.addAll(found);

            // QOL: Anti-lag check
            int veinSize = vein.size() + 1;
            if (kimdog.kimdog_smp.veinminer.qol.AntiLagSystem.shouldThrottle(serverPlayer, veinSize)) {
                LOGGER.warn(" Throttled {} due to spam mining", serverPlayer.getName().getString());
                return;
            }

            // QOL: Optimize vein size for performance
            veinSize = kimdog.kimdog_smp.veinminer.qol.AntiLagSystem.getOptimizedVeinSize(veinSize, serverPlayer);
            if (vein.size() + 1 > veinSize) {
                vein = vein.subList(0, veinSize - 1);
            }

            // QOL: Check durability and warn
            kimdog.kimdog_smp.veinminer.qol.QoLFeatures.notifyLowDurability(serverPlayer, main);

            LOGGER.info("  VeinMiner activated for {} by {} [Upgrades: Blocks+{}, Range+{}]",
                state.getBlock(), serverPlayer.getName().getString(),
                upgrades.maxBlocksLevel * 64, upgrades.maxRangeLevel * 32);
            LOGGER.info(" Found {} adjacent blocks to break (max: {})", vein.size(), maxBlocks - 1);
            LOGGER.info(" Mining vein at: {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());

            spawnActivationAnimation((ServerWorld) world, pos, cfg);

            if (!vein.isEmpty()) {
                applyBreaks(serverPlayer, (ServerWorld) world, vein, main, pos, upgrades);
            } else {
                LOGGER.info(" Single block broken (no adjacent ores found)");
            }
        });
    }

    private static Predicate<BlockState> createMatchPredicate(BlockState originState) {
        VeinMinerConfig cfg = VeinMinerConfig.get();
        Block originBlock = originState.getBlock();
        String originBlockName = Registries.BLOCK.getId(originBlock).getPath();

        // Check if origin block is an ore
        boolean originIsOre = originBlockName.endsWith("_ore");

        if (cfg.checkOreTag) {
            TagKey<Block> oreTag = TagKey.of(
                Registries.BLOCK.getKey(),
                Identifier.ofVanilla("mineable/pickaxe")
            );

            if (!originBlock.getDefaultState().isIn(oreTag)) {
                LOGGER.warn("  Block {} is not in #minecraft:mineable/pickaxe tag", originBlock);
            }
        }

        // Extract the base ore type (e.g., "diamond_ore" from both "diamond_ore" and "deepslate_diamond_ore")
        String baseOreType = getBaseOreType(originBlockName);

        return (blockState) -> {
            Block block = blockState.getBlock();

            // Allow exact match
            if (block == originBlock) return true;

            String blockName = Registries.BLOCK.getId(block).getPath();

            // If mineAllOres is enabled AND origin is an ore, mine ANY adjacent ore type
            if (cfg.mineAllOres && originIsOre) {
                // Check if this block is also an ore (ends with _ore)
                if (blockName.endsWith("_ore")) {
                    if (cfg.checkOreTag) {
                        TagKey<Block> oreTag = TagKey.of(
                            Registries.BLOCK.getKey(),
                            Identifier.ofVanilla("mineable/pickaxe")
                        );
                        return blockState.isIn(oreTag);
                    }
                    return true;
                }
            }

            // Allow matching between regular and deepslate variants of same ore
            String blockBaseType = getBaseOreType(blockName);

            // If both blocks share the same base ore type, they should mine together
            if (baseOreType.equals(blockBaseType)) {
                if (cfg.checkOreTag) {
                    TagKey<Block> oreTag = TagKey.of(
                        Registries.BLOCK.getKey(),
                        Identifier.ofVanilla("mineable/pickaxe")
                    );
                    return blockState.isIn(oreTag);
                }
                return true;
            }

            return false;
        };
    }

    /**
     * Extract base ore type from block name, handling deepslate variants
     * Examples:
     * - "diamond_ore" -> "diamond_ore"
     * - "deepslate_diamond_ore" -> "diamond_ore"
     * - "coal_ore" -> "coal_ore"
     * - "deepslate_coal_ore" -> "coal_ore"
     */
    private static String getBaseOreType(String blockName) {
        if (blockName.startsWith("deepslate_")) {
            return blockName.substring("deepslate_".length());
        }
        return blockName;
    }

    /**
     * Check if a block is actually an ore block
     * This prevents VeinMiner from activating on stone, cobblestone, etc.
     */
    private static boolean isOreBlock(String blockName) {
        // Must end with "_ore" to be an ore
        if (!blockName.endsWith("_ore")) {
            return false;
        }

        // List of known ore types (add more as needed)
        return blockName.contains("coal") ||
               blockName.contains("iron") ||
               blockName.contains("copper") ||
               blockName.contains("gold") ||
               blockName.contains("diamond") ||
               blockName.contains("emerald") ||
               blockName.contains("lapis") ||
               blockName.contains("redstone") ||
               blockName.contains("quartz") ||
               blockName.contains("debris"); // ancient_debris
    }

    private static void applyBreaks(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, ItemStack tool, BlockPos originPos, kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.PlayerUpgrades upgrades) {
        VeinMinerConfig cfg = VeinMinerConfig.get();

        // Apply speed upgrade to reduce delay
        int breakDelay = cfg.breakDelayMs;
        if (upgrades.speedLevel > 0) {
            breakDelay = Math.max(10, cfg.breakDelayMs - (upgrades.speedLevel * 10)); // Each level reduces 10ms
        }

        // Check if cascade effect is enabled
        if (cfg.enableCascadeEffect && breakDelay > 0) {
            applyBreaksWithDelay(player, world, positions, tool, originPos, upgrades, breakDelay);
        } else {
            applyBreaksInstantly(player, world, positions, tool, originPos, upgrades);
        }
    }

    private static void applyBreaksInstantly(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, ItemStack tool, BlockPos originPos, kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.PlayerUpgrades upgrades) {
        VeinMinerConfig cfg = VeinMinerConfig.get();

        // Use the delayed method instead if cascade is enabled
        if (cfg.enableCascadeEffect || cfg.breakDelayMs > 0) {
            int breakDelay = cfg.breakDelayMs;
            if (upgrades.speedLevel > 0) {
                breakDelay = Math.max(10, cfg.breakDelayMs - (upgrades.speedLevel * 10));
            }
            applyBreaksWithDelay(player, world, positions, tool, originPos, upgrades, breakDelay);
            return;
        }

        // Original instant breaking for when cascade is disabled
        int broken = 0;
        int totalXp = 0;
        String firstOreType = "";

        float durabilityMult = cfg.enableEnchantmentBonuses ? EnchantmentBonusSystem.getDurabilityMultiplier(tool) : 1.0f;

        // Apply durability upgrade
        if (cfg.enableDurabilityMultiplier) {
            durabilityMult *= cfg.durabilityMultiplier;
        }

        VeinMinerStats.PlayerStats stats = cfg.enableStatTracking ? VeinMinerStats.getStats(player) : null;
        float streakMultiplier = 1.0f;
        if (cfg.enableStreakSystem && stats != null) {
            streakMultiplier = 1.0f + ((stats.currentStreak / 100.0f) * (cfg.streakXpMultiplier / 100.0f));
        }

        // Apply XP upgrade multiplier
        float xpUpgradeMultiplier = 1.0f + (upgrades.xpMultiplierLevel * 0.4f); // Each level adds 40% XP

        boolean luckyBreak = cfg.enableLuckSystem && Math.random() * 100 < cfg.luckChance;

        LOGGER.info(" Breaking {} blocks instantly... [Speed: Lv{}, XP: x{}, Particles: Lv{}]",
            positions.size(), upgrades.speedLevel, String.format("%.1f", xpUpgradeMultiplier), upgrades.particleLevel);

        String streakText = stats != null && stats.currentStreak > 0 ? "  Streak: " + stats.currentStreak : "";
        player.sendMessage(net.minecraft.text.Text.literal(" Mining " + positions.size() + " blocks!" + streakText)
            .formatted(net.minecraft.util.Formatting.AQUA));

        List<net.minecraft.entity.ItemEntity> allDrops = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++) {
            BlockPos p = positions.get(i);
            BlockState state = world.getBlockState(p);
            if (state.isAir()) continue;
            BlockEntity be = world.getBlockEntity(p);
            if (be != null) continue;

            String oreType = state.getBlock().toString().toLowerCase();
            if (i == 0) firstOreType = oreType;

            // Apply particle upgrade level
            if (cfg.enableParticles && upgrades.particleLevel > 0) {
                int particleMultiplier = upgrades.particleLevel + 1; // Level 0=1x, 1=2x, 2=3x, 3=4x
                for (int pMult = 0; pMult < particleMultiplier; pMult++) {
                    spawnCustomParticles(world, p, state, cfg);
                }
            } else if (cfg.enableParticles) {
                spawnCustomParticles(world, p, state, cfg);
            }

            if (cfg.playSoundEffects) {
                playSoundEffect(world, p);
            }

            int blockXp = calculateXpForBlock(state);
            blockXp = cfg.enableEnchantmentBonuses ? EnchantmentBonusSystem.getXpBonus(tool, blockXp) : blockXp;
            blockXp = (int) (blockXp * streakMultiplier * xpUpgradeMultiplier); // Apply upgrade multiplier

            if (luckyBreak && Math.random() > 0.7) {
                blockXp = (int) (blockXp * 1.5f);
                LOGGER.info(" LUCKY HIT! +50% XP!");
                player.sendMessage(net.minecraft.text.Text.literal(" LUCKY HIT! +50% XP bonus!").formatted(net.minecraft.util.Formatting.GOLD));
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/lucky_break");
            }

            if (cfg.enableStatTracking && !tool.getEnchantments().isEmpty() && broken == 1) {
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/enchanted_pickaxe");
            }

            if (cfg.consolidateDrops && broken == 1) {
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/consolidated_collector");
            }

            totalXp += blockXp;

            if (cfg.consolidateDrops) {
                var items = Block.getDroppedStacks(state, world, p, be, player, tool);
                for (ItemStack stack : items) {
                    net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, stack);
                    allDrops.add(itemEntity);
                }
            } else {
                Block.dropStacks(state, world, p, be, player, tool);
            }

            world.setBlockState(p, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
            broken++;
        }

        if (cfg.consolidateDrops && !allDrops.isEmpty() && !positions.isEmpty()) {
            BlockPos dropOrigin = positions.get(0);
            for (net.minecraft.entity.ItemEntity itemEntity : allDrops) {
                itemEntity.setPosition(dropOrigin.getX() + 0.5, dropOrigin.getY() + 1.0, dropOrigin.getZ() + 0.5);
                world.spawnEntity(itemEntity);
            }
            LOGGER.info(" Consolidated {} item stacks at origin", allDrops.size());
        }

        if (broken > 0) {
            LOGGER.info(" Successfully broke {} blocks!", broken);

            String completionMsg = String.format(" Vein complete! Broke %d blocks for %d XP!", broken, totalXp);
            player.sendMessage(net.minecraft.text.Text.literal(completionMsg).formatted(net.minecraft.util.Formatting.GREEN));

            if (totalXp > 0) {
                dropXpOrbs(world, originPos, totalXp);
                LOGGER.info(" Dropped {} XP total", totalXp);
            }

            spawnCompletionAnimation(world, originPos, broken, cfg);

            if (cfg.enableStatTracking && stats != null) {
                VeinMinerStats.recordVein(player, broken, totalXp, firstOreType);
                checkAchievements(player, stats, broken, firstOreType, world);

                // QOL: Progressive tutorial tips
                kimdog.kimdog_smp.veinminer.qol.SmartNotifications.checkProgressiveTips(player);

                // QOL: Show streak info
                if (stats.currentStreak > 0 && stats.currentStreak % 5 == 0) {
                    kimdog.kimdog_smp.veinminer.qol.QoLFeatures.showStreakInfo(player, stats.currentStreak);
                }

                if (cfg.enableQuests) {
                    String oreType = kimdog.kimdog_smp.veinminer.effects.OreEffects.getOreType(world.getBlockState(originPos));
                    kimdog.kimdog_smp.veinminer.quests.VeinMinerQuests.incrementOreCount(player, oreType);
                }
            }

            // QOL: Auto-repair check
            if (cfg.enableAutoRepair) {
                kimdog.kimdog_smp.veinminer.systems.AutoRepairSystem.tryAutoRepair(player, tool, cfg);
            }

            try {
                if (tool.isDamageable()) {
                    int damageToTake = (int) (broken * durabilityMult);
                    int newDamage = tool.getDamage() + damageToTake;
                    if (newDamage >= tool.getMaxDamage()) {
                        LOGGER.warn("  Tool durability exceeded! Tool has been destroyed!");
                        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    } else {
                        tool.setDamage(newDamage);
                        int durabilityLeft = tool.getMaxDamage() - newDamage;
                        LOGGER.info("  Tool durability: {}/{}", durabilityLeft, tool.getMaxDamage());
                    }
                }
            } catch (Exception e) {
                LOGGER.error(" Error handling tool durability:", e);
            }
            LOGGER.info("");
        }
    }

    private static void applyBreaksWithDelay(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, ItemStack tool, BlockPos originPos, kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager.PlayerUpgrades upgrades, int breakDelay) {
        VeinMinerConfig cfg = VeinMinerConfig.get();

        int delayTicks = Math.max(1, breakDelay / 50); // Convert ms to ticks (50ms = 1 tick)

        LOGGER.info(" Starting to break {} blocks with {} tick delay... [Upgrades Applied: Speed Lv{}, XP x{}]",
            positions.size(), delayTicks, upgrades.speedLevel, 1.0f + (upgrades.xpMultiplierLevel * 0.4f));

        VeinMinerStats.PlayerStats stats = cfg.enableStatTracking ? VeinMinerStats.getStats(player) : null;
        final float streakMultiplier;
        if (cfg.enableStreakSystem && stats != null) {
            streakMultiplier = 1.0f + ((stats.currentStreak / 100.0f) * (cfg.streakXpMultiplier / 100.0f));
        } else {
            streakMultiplier = 1.0f;
        }

        // Apply XP upgrade multiplier
        final float xpUpgradeMultiplier = 1.0f + (upgrades.xpMultiplierLevel * 0.4f);

        String streakText = stats != null && stats.currentStreak > 0 ? "  Streak: " + stats.currentStreak : "";
        player.sendMessage(net.minecraft.text.Text.literal(" Mining " + positions.size() + " blocks!" + streakText)
            .formatted(net.minecraft.util.Formatting.AQUA));

        final List<net.minecraft.entity.ItemEntity> allDrops = new ArrayList<>();
        final int[] totalXp = {0};
        final int[] broken = {0};
        final String[] firstOreType = {""};

        // Schedule each block break with increasing delay
        for (int i = 0; i < positions.size(); i++) {
            final int index = i;
            final BlockPos blockPos = positions.get(i);
            final int scheduledTick = delayTicks * (index + 1);

            // Use Minecraft's server scheduler
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    world.getServer().execute(() -> {
                        BlockState state = world.getBlockState(blockPos);
                        if (state.isAir()) return;

                        LOGGER.info(" Breaking block {} at {}, {}, {}", index + 1, blockPos.getX(), blockPos.getY(), blockPos.getZ());

                        String oreType = state.getBlock().toString().toLowerCase();
                        if (index == 0) firstOreType[0] = oreType;

                        // Particle trail from origin to current block
                        if (cfg.enableParticles) {
                            spawnParticleTrail(world, originPos, blockPos, index, positions.size());

                            // Apply particle upgrade
                            int particleMultiplier = Math.max(1, upgrades.particleLevel + 1);
                            for (int pMult = 0; pMult < particleMultiplier; pMult++) {
                                spawnCustomParticles(world, blockPos, state, cfg);
                            }

                            spawnBlockBreakIndicator(world, blockPos, index, positions.size());
                        }

                        // Ore-specific effects
                        if (cfg.enableOreSpecificEffects) {
                            if (cfg.enableOreParticles) {
                                kimdog.kimdog_smp.veinminer.effects.OreEffects.spawnOreParticles(world, blockPos,
                                    kimdog.kimdog_smp.veinminer.effects.OreEffects.getOreType(state));
                            }
                            if (cfg.enableOreSounds) {
                                kimdog.kimdog_smp.veinminer.effects.OreEffects.playSoundForOre(world, blockPos,
                                    kimdog.kimdog_smp.veinminer.effects.OreEffects.getOreType(state));
                            }
                        } else if (cfg.playSoundEffects) {
                            playSoundEffect(world, blockPos);
                        }

                        // Calculate XP with upgrades
                        int blockXp = calculateXpForBlock(state);
                        blockXp = cfg.enableEnchantmentBonuses ? EnchantmentBonusSystem.getXpBonus(tool, blockXp) : blockXp;
                        blockXp = (int) (blockXp * streakMultiplier * xpUpgradeMultiplier); // Apply upgrade multiplier
                        totalXp[0] += blockXp;

                        // Get drops before breaking
                        BlockEntity be = world.getBlockEntity(blockPos);
                        List<ItemStack> drops = Block.getDroppedStacks(state, world, blockPos, be, player, tool);

                        // Store drops for consolidation or drop immediately
                        if (cfg.consolidateDrops) {
                            for (ItemStack stack : drops) {
                                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                                    world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, stack);
                                allDrops.add(itemEntity);
                            }
                        } else {
                            for (ItemStack stack : drops) {
                                Block.dropStack(world, blockPos, stack);
                            }
                        }

                        // Break the block
                        world.setBlockState(blockPos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
                        broken[0]++;

                        // Show cascade effect
                        if (cfg.enableCascadeEffect) {
                            kimdog.kimdog_smp.veinminer.systems.VisualEffects.spawnCascadeEffect(world, blockPos, index);
                        }

                        // If this is the last block, finish up
                        if (index == positions.size() - 1) {
                            finalizeVeinMining(player, world, originPos, allDrops, totalXp[0], broken[0], firstOreType[0], tool, stats, cfg);
                        }
                    });
                }
            }, scheduledTick * 50L); // Convert ticks back to milliseconds
        }
    }

    private static void finalizeVeinMining(ServerPlayerEntity player, ServerWorld world, BlockPos originPos,
                                           List<net.minecraft.entity.ItemEntity> allDrops, int totalXp, int broken,
                                           String firstOreType, ItemStack tool, VeinMinerStats.PlayerStats stats, VeinMinerConfig cfg) {
        // Consolidate drops at origin
        if (cfg.consolidateDrops && !allDrops.isEmpty()) {
            for (net.minecraft.entity.ItemEntity itemEntity : allDrops) {
                itemEntity.setPosition(originPos.getX() + 0.5, originPos.getY() + 1.0, originPos.getZ() + 0.5);
                world.spawnEntity(itemEntity);
            }
            LOGGER.info(" Consolidated {} item stacks at origin", allDrops.size());
        }

        // Drop XP
        if (totalXp > 0) {
            dropXpOrbs(world, originPos, totalXp);
            LOGGER.info(" Dropped {} XP total", totalXp);
        }

        // Completion message
        String completionMsg = String.format(" Vein complete! Broke %d blocks for %d XP!", broken, totalXp);
        player.sendMessage(net.minecraft.text.Text.literal(completionMsg).formatted(net.minecraft.util.Formatting.GREEN));

        // Completion animation
        spawnCompletionAnimation(world, originPos, broken, cfg);

        // Track stats
        if (cfg.enableStatTracking && stats != null) {
            VeinMinerStats.recordVein(player, broken, totalXp, firstOreType);
            checkAchievements(player, stats, broken, firstOreType, world);

            if (cfg.enableQuests) {
                String oreType = kimdog.kimdog_smp.veinminer.effects.OreEffects.getOreType(world.getBlockState(originPos));
                kimdog.kimdog_smp.veinminer.quests.VeinMinerQuests.incrementOreCount(player, oreType);
            }
        }

        // Handle tool durability
        try {
            if (tool.isDamageable()) {
                double durabilityMult = cfg.enableDurabilityMultiplier ? cfg.durabilityMultiplier : 1.0;
                int damageToTake = (int) (broken * durabilityMult);
                int newDamage = tool.getDamage() + damageToTake;
                if (newDamage >= tool.getMaxDamage()) {
                    LOGGER.warn("  Tool durability exceeded! Tool has been destroyed!");
                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                } else {
                    tool.setDamage(newDamage);
                    int durabilityLeft = tool.getMaxDamage() - newDamage;
                    LOGGER.info("  Tool durability: {}/{}", durabilityLeft, tool.getMaxDamage());
                }
            }
        } catch (Exception e) {
            LOGGER.error(" Error handling tool durability:", e);
        }

        LOGGER.info(" Successfully broke {} blocks!", broken);
        LOGGER.info("");
    }

    private static int calculateXpForBlock(BlockState state) {
        try {
            String blockName = state.getBlock().toString().toLowerCase();
            int rand = (int) (Math.random() * 2);

            if (blockName.contains("diamond")) return 3 + rand;
            if (blockName.contains("emerald")) return 3 + rand;
            if (blockName.contains("gold") || blockName.contains("lapis")) return 2 + rand;
            if (blockName.contains("redstone")) return 1 + rand;
            if (blockName.contains("coal")) return rand;
            if (blockName.contains("iron") || blockName.contains("copper")) return rand;

            return 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private static void dropXpOrbs(ServerWorld world, BlockPos pos, int xp) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            while (xp > 0) {
                int xpAmount = Math.min(xp, 100);
                net.minecraft.entity.ExperienceOrbEntity xpOrb = new net.minecraft.entity.ExperienceOrbEntity(world, x, y, z, xpAmount);
                world.spawnEntity(xpOrb);
                xp -= xpAmount;
            }
        } catch (Exception e) {
            LOGGER.debug("XP dropping error: {}", e.getMessage());
        }
    }

    private static void spawnCustomParticles(ServerWorld world, BlockPos pos, BlockState state, VeinMinerConfig cfg) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        int count = cfg.particleCount;

        try {
            switch (cfg.particleEffect.toLowerCase()) {
                case "rainbow":
                    for (int i = 0; i < count / 2; i++) {
                        world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, 1, 0.3, 0.3, 0.3, 0.15);
                    }
                    break;
                case "ore":
                    world.spawnParticles(new net.minecraft.particle.BlockStateParticleEffect(net.minecraft.particle.ParticleTypes.BLOCK, state), x, y, z, count, 0.5, 0.5, 0.5, 0.15);
                    break;
                case "enchant":
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, y, z, count / 2, 0.4, 0.4, 0.4, 0.2);
                    break;
                case "smoke":
                default:
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, x, y, z, count / 2, 0.3, 0.3, 0.3, 0.1);
                    break;
            }
        } catch (Exception e) {
            LOGGER.debug("Particle effect error: {}", e.getMessage());
        }
    }

    private static void playSoundEffect(ServerWorld world, BlockPos pos) {
        try {
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 0.9f + (float) Math.random() * 0.2f);

            if (Math.random() > 0.5) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 0.6f, 1.2f + (float) Math.random() * 0.3f);
            }
        } catch (Exception e) {
            LOGGER.debug("Sound effect error: {}", e.getMessage());
        }
    }

    private static void spawnActivationAnimation(ServerWorld world, BlockPos pos, VeinMinerConfig cfg) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double px = x + Math.cos(angle) * 0.8;
                double pz = z + Math.sin(angle) * 0.8;
                world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, px, y, pz, 1, 0.05, 0.05, 0.05, 0.15);
            }

            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, net.minecraft.sound.SoundCategory.BLOCKS, 0.8f, 1.5f);
        } catch (Exception e) {
            LOGGER.debug("Activation animation error: {}", e.getMessage());
        }
    }

    private static void spawnCompletionAnimation(ServerWorld world, BlockPos pos, int blocksDestroyed, VeinMinerConfig cfg) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            int particleCount = Math.min(blocksDestroyed * 3, 50);

            for (int i = 0; i < particleCount; i++) {
                double angle1 = Math.random() * Math.PI * 2;
                double angle2 = Math.random() * Math.PI;
                double radius = 0.3 + Math.random() * 0.5;

                double px = x + Math.cos(angle1) * Math.sin(angle2) * radius;
                double py = y + Math.cos(angle2) * radius;
                double pz = z + Math.sin(angle1) * Math.sin(angle2) * radius;

                world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, px, py, pz, 1, 0.05, 0.05, 0.05, 0.2);
            }

            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_DEACTIVATE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.2f);
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 0.8f, 2.0f);
        } catch (Exception e) {
            LOGGER.debug("Completion animation error: {}", e.getMessage());
        }
    }

    private static void checkAchievements(ServerPlayerEntity player, VeinMinerStats.PlayerStats stats, int blocksDestroyed, String oreType, ServerWorld world) {
        try {
            VeinMinerConfig cfg = VeinMinerConfig.get();
            if (!cfg.enableAchievements) return;

            net.minecraft.util.Formatting gold = net.minecraft.util.Formatting.GOLD;
            net.minecraft.util.Formatting green = net.minecraft.util.Formatting.GREEN;
            net.minecraft.util.Formatting red = net.minecraft.util.Formatting.RED;

            if (blocksDestroyed >= 10 && stats.largestVeinSize == blocksDestroyed) {
                String msg = " " + player.getName().getString() + " unlocked: MEGA VEIN! (" + blocksDestroyed + " blocks)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(gold), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Mega Vein! (10+ blocks in one vein)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/mega_vein");
            }
            if (blocksDestroyed >= 20 && stats.largestVeinSize == blocksDestroyed) {
                String msg = " " + player.getName().getString() + " unlocked: LEGENDARY VEIN! (" + blocksDestroyed + " blocks)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(gold), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Legendary Vein! (20+ blocks in one vein)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/legendary_vein");
            }
            if (blocksDestroyed >= 30 && stats.largestVeinSize == blocksDestroyed) {
                String msg = " " + player.getName().getString() + " unlocked: EPIC VEIN! (" + blocksDestroyed + " blocks!!)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(gold), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Epic Vein! (30+ blocks in one vein)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/epic_vein");
            }
            if (stats.diamondVeinsFound == 5) {
                String msg = " " + player.getName().getString() + " unlocked: DIAMOND PROSPECTOR! (5 diamond veins)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(gold), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Diamond Prospector! (5 diamond veins)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/diamond_prospector");
            }
            if (stats.emeraldVeinsFound == 5) {
                String msg = " " + player.getName().getString() + " unlocked: EMERALD COLLECTOR! (5 emerald veins)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(green), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Emerald Collector! (5 emerald veins)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/emerald_collector");
            }
            if (stats.currentStreak == 10) {
                String msg = " " + player.getName().getString() + " is on FIRE! (10 vein streak)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(red), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Hot Streak! (10 consecutive veins)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/hot_streak");
            }
            if (stats.currentStreak == 15) {
                String msg = " " + player.getName().getString() + " is BLAZING! (15 vein streak!)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(red), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Blazing Streak! (15 consecutive veins)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/blazing_streak");
            }
            if (stats.currentStreak == 25) {
                String msg = " " + player.getName().getString() + " is a VEIN MINING LEGEND! (25 vein streak!!)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(gold), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: UNSTOPPABLE! (25 consecutive veins)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/unstoppable");
            }
            if (stats.totalBlocksMined == 1000) {
                String msg = " " + player.getName().getString() + " unlocked: ORE MASTER! (1000 blocks mined)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(green), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Ore Master! (1000 total blocks mined)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/ore_master");
            }
            if (stats.totalBlocksMined == 5000) {
                String msg = " " + player.getName().getString() + " unlocked: ORE LEGEND! (5000 blocks mined!!)";
                world.getServer().getPlayerManager().broadcast(net.minecraft.text.Text.literal(msg).formatted(green), false);
                LOGGER.info(" ACHIEVEMENT UNLOCKED: Ore Legend! (5000 total blocks mined)");
                grantAdvancement(player, world.getServer(), "kimdog_smp:veinminer/ore_legend");
            }

            if (stats.currentStreak > 1 && stats.currentStreak % 3 == 0) {
                String msg = " " + player.getName().getString() + " streak: " + stats.currentStreak + " consecutive veins!";
                player.sendMessage(net.minecraft.text.Text.literal(msg).formatted(red));
            }
        } catch (Exception e) {
            LOGGER.debug("Achievement check error: {}", e.getMessage());
        }
    }

    private static void grantAdvancement(ServerPlayerEntity player, net.minecraft.server.MinecraftServer server, String advancementId) {
        try {
            net.minecraft.advancement.AdvancementEntry advancement = server.getAdvancementLoader().get(Identifier.of(advancementId));
            if (advancement != null) {
                net.minecraft.advancement.AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getUnobtainedCriteria()) {
                        progress.obtain(criterion);
                    }
                }
                LOGGER.info(" Advancement granted to {}: {}", player.getName().getString(), advancementId);
            }
        } catch (Exception e) {
            LOGGER.debug("Advancement grant error: {}", e.getMessage());
        }
    }

    /**
     * Spawn a particle trail from origin to target block
     */
    private static void spawnParticleTrail(ServerWorld world, BlockPos from, BlockPos to, int index, int total) {
        double x1 = from.getX() + 0.5;
        double y1 = from.getY() + 0.5;
        double z1 = from.getZ() + 0.5;

        double x2 = to.getX() + 0.5;
        double y2 = to.getY() + 0.5;
        double z2 = to.getZ() + 0.5;

        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        int particles = Math.max(3, (int)(distance * 3));

        // Create gradient effect based on progress
        float progress = (float) index / total;

        for (int i = 0; i < particles; i++) {
            double t = (double) i / particles;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            // Different particle types based on progress
            if (progress < 0.33f) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
            } else if (progress < 0.66f) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
            } else {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
            }
        }
    }

    /**
     * Spawn visual indicator showing which block is being broken
     */
    private static void spawnBlockBreakIndicator(ServerWorld world, BlockPos pos, int index, int total) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        float progress = (float) index / total;

        // Rotating ring of particles around the block
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2 + (System.currentTimeMillis() / 100.0);
            double radius = 0.6;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;

            // Color changes based on progress
            if (progress < 0.25f) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, px, y, pz, 1, 0.0, 0.0, 0.0, 0.0);
            } else if (progress < 0.5f) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, px, y, pz, 1, 0.0, 0.1, 0.0, 0.01);
            } else if (progress < 0.75f) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, px, y, pz, 1, 0.0, 0.1, 0.0, 0.01);
            } else {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, px, y, pz, 1, 0.0, 0.1, 0.0, 0.01);
            }
        }

        // Number indicator particles (height = progress)
        double heightIndicator = y + (progress * 2.0);
        world.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT, x, heightIndicator, z, 3, 0.2, 0.1, 0.2, 0.05);
    }

    /**
     * Spawn beam effect from start to end of vein
     */
    private static void spawnVeinBeam(ServerWorld world, BlockPos start, BlockPos end) {
        double x1 = start.getX() + 0.5;
        double y1 = start.getY() + 0.5;
        double z1 = start.getZ() + 0.5;

        double x2 = end.getX() + 0.5;
        double y2 = end.getY() + 0.5;
        double z2 = end.getZ() + 0.5;

        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        int beamParticles = Math.max(10, (int)(distance * 5));

        for (int i = 0; i < beamParticles; i++) {
            double t = (double) i / beamParticles;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            // Thick beam with multiple particle rings
            for (int ring = 0; ring < 4; ring++) {
                double angle = (ring / 4.0) * Math.PI * 2;
                double radius = 0.15;
                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;

                world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, px, y, pz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Sound effect for beam
        world.playSound(null, start.getX(), start.getY(), start.getZ(),
            net.minecraft.sound.SoundEvents.BLOCK_BEACON_POWER_SELECT,
            net.minecraft.sound.SoundCategory.BLOCKS, 0.7f, 1.5f);
    }

    /**
     * Spawn explosion-like effect at vein boundaries
     */
    private static void spawnBoundaryEffect(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Expanding sphere of particles
        for (int i = 0; i < 20; i++) {
            double theta = Math.random() * Math.PI;
            double phi = Math.random() * Math.PI * 2;
            double radius = 0.8;

            double px = x + Math.sin(theta) * Math.cos(phi) * radius;
            double py = y + Math.cos(theta) * radius;
            double pz = z + Math.sin(theta) * Math.sin(phi) * radius;

            world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, px, py, pz, 1, 0.0, 0.0, 0.0, 0.1);
        }
    }
}
