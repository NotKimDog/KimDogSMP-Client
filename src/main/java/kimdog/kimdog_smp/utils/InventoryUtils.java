package kimdog.kimdog_smp.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Inventory utility functions for VeinMiner
 * Handles item management and inventory operations
 */
public class InventoryUtils {

    /**
     * Check if a tool is damaged
     */
    public static boolean isDamaged(ItemStack stack) {
        return stack.isDamageable() && stack.getDamage() > 0;
    }

    /**
     * Get tool durability percentage
     */
    public static int getDurabilityPercent(ItemStack stack) {
        if (!stack.isDamageable()) return 100;
        int durability = stack.getMaxDamage() - stack.getDamage();
        return (durability * 100) / stack.getMaxDamage();
    }

    /**
     * Check if player has room in inventory
     */
    public static boolean hasInventorySpace(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count matching items in inventory
     */
    public static int countItems(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Remove items from inventory
     */
    public static void removeItems(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        int toRemove = amount;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                int removed = Math.min(toRemove, stack.getCount());
                stack.decrement(removed);
                toRemove -= removed;
                if (toRemove <= 0) break;
            }
        }
    }

    /**
     * Check if item is a tool (pickaxe, axe, shovel, etc)
     */
    public static boolean isMiningTool(ItemStack stack) {
        String name = stack.getItem().getTranslationKey().toLowerCase();
        return name.contains("pickaxe") || name.contains("axe") ||
               name.contains("shovel") || name.contains("hoe") ||
               name.contains("sword");
    }

    /**
     * Get inventory size
     */
    public static int getInventorySize(ServerPlayerEntity player) {
        return player.getInventory().size();
    }

    /**
     * Get item from inventory
     */
    public static ItemStack getItem(ServerPlayerEntity player, int slot) {
        return player.getInventory().getStack(slot);
    }

    /**
     * Set item in inventory
     */
    public static void setItem(ServerPlayerEntity player, int slot, ItemStack stack) {
        player.getInventory().setStack(slot, stack);
    }
}
