package kimdog.kimdog_smp.veinminer.gui;

import kimdog.kimdog_smp.veinminer.upgrades.UpgradeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * GUI Screen for VeinMiner Upgrades
 */
public class VeinMinerUpgradeScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.of("kimdog_smp", "textures/gui/upgrade_menu.png");
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;

    private final UpgradeManager.PlayerUpgrades upgrades;
    private int backgroundX;
    private int backgroundY;

    public VeinMinerUpgradeScreen(UpgradeManager.PlayerUpgrades upgrades) {
        super(Text.literal("VeinMiner Upgrades"));
        this.upgrades = upgrades;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate centered position
        backgroundX = (this.width - GUI_WIDTH) / 2;
        backgroundY = (this.height - GUI_HEIGHT) / 2;

        // Add upgrade buttons
        addUpgradeButton("Max Blocks", 0, upgrades.maxBlocksLevel, 5,
            () -> purchaseUpgrade("maxblocks"));
        addUpgradeButton("Max Range", 1, upgrades.maxRangeLevel, 5,
            () -> purchaseUpgrade("maxrange"));
        addUpgradeButton("XP Multiplier", 2, upgrades.xpMultiplierLevel, 5,
            () -> purchaseUpgrade("xpmultiplier"));
        addUpgradeButton("Mining Speed", 3, upgrades.speedLevel, 5,
            () -> purchaseUpgrade("speed"));
        addUpgradeButton("Particle Effects", 4, upgrades.particleLevel, 3,
            () -> purchaseUpgrade("particles"));

        // Close button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Close").formatted(Formatting.RED),
            button -> this.close()
        ).dimensions(backgroundX + GUI_WIDTH / 2 - 50, backgroundY + GUI_HEIGHT - 30, 100, 20).build());
    }

    private void addUpgradeButton(String name, int index, int currentLevel, int maxLevel, Runnable onPress) {
        int x = backgroundX + 20;
        int y = backgroundY + 40 + (index * 28);
        int width = 100;
        int height = 20;

        String buttonText = currentLevel >= maxLevel ? "§aMAX" : "§eUpgrade";
        boolean canUpgrade = currentLevel < maxLevel;

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(buttonText),
            button -> {
                if (canUpgrade) {
                    onPress.run();
                    // Refresh screen
                    this.clearAndInit();
                }
            }
        ).dimensions(x + 150, y, width, height).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        // Draw custom GUI background
        context.fill(backgroundX, backgroundY, backgroundX + GUI_WIDTH, backgroundY + GUI_HEIGHT, 0xCC000000);
        context.fill(backgroundX + 2, backgroundY + 2, backgroundX + GUI_WIDTH - 2, backgroundY + GUI_HEIGHT - 2, 0xFF1a1a1a);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("⛏ VeinMiner Upgrades ⛏").formatted(Formatting.GOLD, Formatting.BOLD),
            backgroundX + GUI_WIDTH / 2, backgroundY + 10, 0xFFFFFF);

        // Draw emerald count
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("💚 Emeralds: " + upgrades.emeralds).formatted(Formatting.GREEN),
            backgroundX + 20, backgroundY + 25, 0xFFFFFF);

        // Draw upgrade info
        drawUpgradeInfo(context, "Max Blocks", 0, upgrades.maxBlocksLevel, 5, getCost("maxblocks", upgrades.maxBlocksLevel));
        drawUpgradeInfo(context, "Max Range", 1, upgrades.maxRangeLevel, 5, getCost("maxrange", upgrades.maxRangeLevel));
        drawUpgradeInfo(context, "XP Multiplier", 2, upgrades.xpMultiplierLevel, 5, getCost("xpmultiplier", upgrades.xpMultiplierLevel));
        drawUpgradeInfo(context, "Mining Speed", 3, upgrades.speedLevel, 5, getCost("speed", upgrades.speedLevel));
        drawUpgradeInfo(context, "Particle Effects", 4, upgrades.particleLevel, 3, getCost("particles", upgrades.particleLevel));

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawUpgradeInfo(DrawContext context, String name, int index, int currentLevel, int maxLevel, int cost) {
        int x = backgroundX + 20;
        int y = backgroundY + 40 + (index * 28);

        // Draw name
        context.drawTextWithShadow(this.textRenderer,
            Text.literal(name).formatted(Formatting.YELLOW),
            x, y, 0xFFFFFF);

        // Draw level
        String levelText = currentLevel >= maxLevel ?
            "§a[MAX]" :
            "§7[" + currentLevel + "/" + maxLevel + "]";
        context.drawTextWithShadow(this.textRenderer,
            Text.literal(levelText),
            x, y + 10, 0xFFFFFF);

        // Draw cost
        if (currentLevel < maxLevel) {
            String costText = "§6" + cost + " emeralds";
            boolean canAfford = upgrades.emeralds >= cost;
            context.drawTextWithShadow(this.textRenderer,
                Text.literal(costText).formatted(canAfford ? Formatting.GREEN : Formatting.RED),
                x + 90, y + 10, 0xFFFFFF);
        }

        // Draw progress bar
        drawProgressBar(context, x + 90, y - 2, 60, 8, currentLevel, maxLevel);
    }

    private void drawProgressBar(DrawContext context, int x, int y, int width, int height, int current, int max) {
        // Background
        context.fill(x, y, x + width, y + height, 0xFF333333);

        // Progress
        int progress = (int)((float)current / max * width);
        int color = current >= max ? 0xFF00FF00 : 0xFFFFAA00;
        context.fill(x, y, x + progress, y + height, color);

        // Border (manual)
        context.fill(x, y, x + width, y + 1, 0xFF000000); // Top
        context.fill(x, y + height - 1, x + width, y + height, 0xFF000000); // Bottom
        context.fill(x, y, x + 1, y + height, 0xFF000000); // Left
        context.fill(x + width - 1, y, x + width, y + height, 0xFF000000); // Right
    }

    private int getCost(String upgradeName, int currentLevel) {
        // Cost formula: base * (2^level)
        int[] baseCosts = {100, 150, 200, 100, 50}; // maxblocks, maxrange, xp, speed, particles
        int baseIndex = switch(upgradeName) {
            case "maxblocks" -> 0;
            case "maxrange" -> 1;
            case "xpmultiplier" -> 2;
            case "speed" -> 3;
            case "particles" -> 4;
            default -> 0;
        };
        return baseCosts[baseIndex] * (int)Math.pow(2, currentLevel);
    }

    private void purchaseUpgrade(String upgradeName) {
        // Send command to server to purchase upgrade
        if (this.client != null && this.client.player != null) {
            this.client.player.networkHandler.sendChatMessage("/kimdog upgrade buy " + upgradeName);
        }
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause game when GUI is open
    }
}
