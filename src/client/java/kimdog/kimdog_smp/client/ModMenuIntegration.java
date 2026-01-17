package kimdog.kimdog_smp.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import kimdog.kimdog_smp.veinminer.VeinMinerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Mod Menu integration for KimDog SMP
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    /**
     * Professional config screen with 2x2 grid layout (side-by-side categories)
     */
    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private final VeinMinerConfig config;
        private static final int COLUMN_WIDTH = 280;
        private static final int COLUMN_HEIGHT = 260;
        private static final int SECTION_HEIGHT = 22;
        private static final int BUTTON_HEIGHT = 20;
        private static final int SPACING = 4;

        public ConfigScreen(Screen parent) {
            super(Text.literal("VeinMiner Settings"));
            this.parent = parent;
            this.config = VeinMinerConfig.get();
        }

        @Override
        protected void init() {
            super.init();
            this.clearChildren();

            int centerX = this.width / 2;
            int contentStartY = 60;

            // 2x2 grid layout
            int col1X = centerX - COLUMN_WIDTH - 10;
            int col2X = centerX + 10;
            int row1Y = contentStartY;
            int row2Y = contentStartY + COLUMN_HEIGHT + 20;

            // ========== ROW 1: GENERAL & GAMEPLAY ==========
            createColumnSection(col1X, row1Y, "⚙ GENERAL", new String[]{
                "VeinMiner: " + (config.enabled ? "●" : "○"),
                "Activation: " + getActivationDesc(),
                "Max Blocks: " + config.maxBlocks
            }, new int[]{0, 1, 2});

            createColumnSection(col2X, row1Y, "🎮 GAMEPLAY", new String[]{
                "Consolidate Drops: " + (config.consolidateDrops ? "ON" : "OFF"),
                "Mine All Ores: " + (config.mineAllOres ? "ON" : "OFF")
            }, new int[]{3, 4});

            // ========== ROW 2: VISUAL & QOL ==========
            createColumnSection(col1X, row2Y, "✨ VISUAL EFFECTS", new String[]{
                "Cascade Animation: " + (config.enableCascadeEffect ? "ON" : "OFF"),
                "Particle Effects: " + (config.enableParticles ? "ON" : "OFF")
            }, new int[]{5, 6});

            createColumnSection(col2X, row2Y, "🏆 QUALITY OF LIFE", new String[]{
                "Auto-Repair: " + (config.enableAutoRepair ? "ON" : "OFF"),
                "Leaderboards: " + (config.enableLeaderboards ? "ON" : "OFF"),
                "Daily Rewards: " + (config.enableDailyRewards ? "ON" : "OFF"),
                "Achievements: " + (config.enableAchievements ? "ON" : "OFF")
            }, new int[]{7, 8, 9, 10});

            // ========== BOTTOM BUTTONS ==========
            int bottomY = this.height - 28;
            int buttonWidth = 120;
            int totalWidth = buttonWidth * 3 + 30;

            addDrawableChild(ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
            ).dimensions(centerX - totalWidth / 2, bottomY, buttonWidth, 20).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("Apply"),
                button -> {
                    VeinMinerConfig.save();
                }
            ).dimensions(centerX - buttonWidth / 2, bottomY, buttonWidth, 20).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> {
                    VeinMinerConfig.save();
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
            ).dimensions(centerX + totalWidth / 2 - buttonWidth, bottomY, buttonWidth, 20).build());
        }

        private void createColumnSection(int x, int y, String title, String[] options, int[] ids) {
            int currentY = y;

            for (int i = 0; i < options.length; i++) {
                final int index = ids[i];
                final String text = options[i];

                addDrawableChild(ButtonWidget.builder(
                    Text.literal(text),
                    button -> handleButtonClick(index)
                ).dimensions(x, currentY, COLUMN_WIDTH, BUTTON_HEIGHT).build());

                currentY += BUTTON_HEIGHT + SPACING;
            }
        }

        private void handleButtonClick(int id) {
            switch (id) {
                case 0 -> {
                    config.enabled = !config.enabled;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 1 -> {
                    config.activation = switch (config.activation) {
                        case "always" -> "sneak";
                        case "sneak" -> "toggle";
                        case "toggle" -> "always";
                        default -> "always";
                    };
                    VeinMinerConfig.save();
                    this.init();
                }
                case 2 -> {
                    config.maxBlocks = config.maxBlocks >= 512 ? 32 : config.maxBlocks + 32;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 3 -> {
                    config.consolidateDrops = !config.consolidateDrops;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 4 -> {
                    config.mineAllOres = !config.mineAllOres;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 5 -> {
                    config.enableCascadeEffect = !config.enableCascadeEffect;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 6 -> {
                    config.enableParticles = !config.enableParticles;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 7 -> {
                    config.enableAutoRepair = !config.enableAutoRepair;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 8 -> {
                    config.enableLeaderboards = !config.enableLeaderboards;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 9 -> {
                    config.enableDailyRewards = !config.enableDailyRewards;
                    VeinMinerConfig.save();
                    this.init();
                }
                case 10 -> {
                    config.enableAchievements = !config.enableAchievements;
                    VeinMinerConfig.save();
                    this.init();
                }
            }
        }

        private String getActivationDesc() {
            return switch (config.activation) {
                case "always" -> "Always Active";
                case "sneak" -> "Sneak to Activate";
                case "toggle" -> "Toggle with Command";
                default -> "Always Active";
            };
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            drawGradientBackground(context);
            super.render(context, mouseX, mouseY, delta);

            int centerX = this.width / 2;
            int contentStartY = 60;
            int col1X = centerX - COLUMN_WIDTH - 10;
            int col2X = centerX + 10;
            int row1Y = contentStartY;
            int row2Y = contentStartY + COLUMN_HEIGHT + 20;

            // ===== HEADER SECTION =====
            context.fill(0, 0, this.width, 50, 0xFF0F0F0F);
            context.fill(0, 50, this.width, 52, 0xFF404040);

            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("VeinMiner Settings"),
                centerX,
                16,
                0xFFFFFFFF
            );

            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Professional Vein Mining Configuration").formatted(Formatting.GRAY),
                centerX,
                30,
                0xFFAAAAAA
            );

            // ===== SECTION TITLES =====
            drawSectionTitle(context, "⚙ GENERAL", col1X, row1Y);
            drawSectionTitle(context, "🎮 GAMEPLAY", col2X, row1Y);
            drawSectionTitle(context, "✨ VISUAL EFFECTS", col1X, row2Y);
            drawSectionTitle(context, "🏆 QUALITY OF LIFE", col2X, row2Y);

            // ===== FOOTER =====
            context.fill(0, this.height - 40, this.width, this.height - 38, 0xFF404040);
        }

        private void drawGradientBackground(DrawContext context) {
            int topColor = 0xFF1A1A1A;
            int bottomColor = 0xFF0F0F0F;

            for (int y = 50; y < this.height - 40; y++) {
                float progress = (float) (y - 50) / (this.height - 90);
                int color = interpolateColor(topColor, bottomColor, progress);
                context.fill(0, y, this.width, y + 1, color);
            }

            int gridColor = 0x15FFFFFF;
            int gridSize = 50;
            for (int x = 0; x < this.width; x += gridSize) {
                context.fill(x, 50, x + 1, this.height - 40, gridColor);
            }
            for (int y = 50; y < this.height - 40; y += gridSize) {
                context.fill(0, y, this.width, y + 1, gridColor);
            }
        }

        private void drawSectionTitle(DrawContext context, String label, int x, int y) {
            context.fill(x - 5, y - 2, x + COLUMN_WIDTH + 5, y + 20, 0xFF2A2A2A);
            context.fill(x - 5, y - 2, x + COLUMN_WIDTH + 5, y - 1, 0xFF5CB85C);

            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(label).formatted(Formatting.BOLD),
                x + 5,
                y + 5,
                0xFF5CB85C
            );
        }

        private int interpolateColor(int color1, int color2, float progress) {
            int r1 = (color1 >> 16) & 0xFF;
            int g1 = (color1 >> 8) & 0xFF;
            int b1 = color1 & 0xFF;

            int r2 = (color2 >> 16) & 0xFF;
            int g2 = (color2 >> 8) & 0xFF;
            int b2 = color2 & 0xFF;

            int r = (int) (r1 + (r2 - r1) * progress);
            int g = (int) (g1 + (g2 - g1) * progress);
            int b = (int) (b1 + (b2 - b1) * progress);

            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }

        @Override
        public void close() {
            VeinMinerConfig.save();
            if (this.client != null) {
                this.client.setScreen(parent);
            }
        }
    }
}
