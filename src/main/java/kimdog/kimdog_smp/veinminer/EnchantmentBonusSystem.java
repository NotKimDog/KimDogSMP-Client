package kimdog.kimdog_smp.veinminer;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentBonusSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("VeinMiner Enchants");

    /**
     * Get break delay reduction based on Efficiency enchantment
     */
    public static int getBreakDelayBonus(ItemStack tool) {
        try {
            int efficiencyLevel = getEnchantmentLevel(tool, "efficiency");
            if (efficiencyLevel > 0) {
                // Each level of Efficiency reduces delay by 5ms (min 10ms)
                int reduction = efficiencyLevel * 5;
                int newDelay = Math.max(10, 50 - reduction);
                LOGGER.debug(" Efficiency {} detected! Break delay: {}ms", efficiencyLevel, newDelay);
                return newDelay;
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading efficiency: {}", e.getMessage());
        }
        return 50; // Default delay
    }

    /**
     * Get durability multiplier based on Unbreaking enchantment
     */
    public static float getDurabilityMultiplier(ItemStack tool) {
        try {
            int unbreakingLevel = getEnchantmentLevel(tool, "unbreaking");
            if (unbreakingLevel > 0) {
                // Each level reduces durability loss by 15%
                float multiplier = 1.0f - (unbreakingLevel * 0.15f);
                multiplier = Math.max(0.25f, multiplier); // Min 25% durability loss
                LOGGER.debug(" Unbreaking {} detected! Durability mult: {}x", unbreakingLevel, multiplier);
                return multiplier;
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading unbreaking: {}", e.getMessage());
        }
        return 1.0f; // No reduction
    }

    /**
     * Get XP bonus - all enchanted tools get bonus
     */
    public static int getXpBonus(ItemStack tool, int baseXp) {
        try {
            ItemEnchantmentsComponent enchantments = tool.getEnchantments();
            if (!enchantments.isEmpty()) {
                int bonus = (int) (baseXp * 1.25f); // 25% XP bonus for enchanted tools
                LOGGER.debug(" Enchanted tool detected! XP: {} -> {}", baseXp, bonus);
                return bonus;
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading enchantments: {}", e.getMessage());
        }
        return baseXp;
    }

    /**
     * Check if tool has Fortune enchantment (for better drops)
     */
    public static int getFortuneLevel(ItemStack tool) {
        try {
            int fortuneLevel = getEnchantmentLevel(tool, "fortune");
            if (fortuneLevel > 0) {
                LOGGER.debug(" Fortune {} detected!", fortuneLevel);
                return fortuneLevel;
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading fortune: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Check if tool has Silk Touch
     */
    public static boolean hasSilkTouch(ItemStack tool) {
        try {
            int silkTouch = getEnchantmentLevel(tool, "silk_touch");
            if (silkTouch > 0) {
                LOGGER.debug(" Silk Touch detected!");
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading silk touch: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if tool has luck bonus (any enchantment)
     */
    public static boolean hasLuckBonus(ItemStack tool) {
        try {
            return !tool.getEnchantments().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to get enchantment level from tool by name
     */
    private static int getEnchantmentLevel(ItemStack tool, String enchantmentName) {
        try {
            ItemEnchantmentsComponent enchantments = tool.getEnchantments();
            if (enchantments == null || enchantments.isEmpty()) {
                return 0;
            }

            // Check each enchantment
            for (var entry : enchantments.getEnchantmentEntries()) {
                String name = entry.getKey().getIdAsString();
                if (name != null && name.contains(enchantmentName)) {
                    return entry.getIntValue();
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting enchantment level for {}: {}", enchantmentName, e.getMessage());
        }
        return 0;
    }

    /**
     * Get summary of all enchantments on tool
     */
    public static String getEnchantmentSummary(ItemStack tool) {
        try {
            ItemEnchantmentsComponent enchantments = tool.getEnchantments();
            if (enchantments == null || enchantments.isEmpty()) {
                return "No enchantments";
            }

            StringBuilder summary = new StringBuilder();
            int efficiency = getEnchantmentLevel(tool, "efficiency");
            int unbreaking = getEnchantmentLevel(tool, "unbreaking");
            int fortune = getEnchantmentLevel(tool, "fortune");
            boolean silkTouch = hasSilkTouch(tool);

            if (efficiency > 0) summary.append("Efficiency ").append(efficiency).append(" ");
            if (unbreaking > 0) summary.append("Unbreaking ").append(unbreaking).append(" ");
            if (fortune > 0) summary.append("Fortune ").append(fortune).append(" ");
            if (silkTouch) summary.append("Silk Touch ");

            return summary.toString().trim();
        } catch (Exception e) {
            return "Error reading enchantments";
        }
    }
}
