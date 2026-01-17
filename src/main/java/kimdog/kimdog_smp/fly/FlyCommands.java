package kimdog.kimdog_smp.fly;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import com.mojang.brigadier.arguments.FloatArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class FlyCommands {
    private static final Map<UUID, FlyData> flyingPlayers = new HashMap<>();
    private static final float DEFAULT_SPEED = 0.1f;

    public static class FlyData {
        public boolean flying = false;
        public float speed = DEFAULT_SPEED;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                literal("kimdog")
                    .then(
                        literal("fly")
                            .executes(ctx -> toggleFly(ctx.getSource()))

                            .then(
                                literal("on")
                                .executes(ctx -> enableFly(ctx.getSource()))
                            )

                            .then(
                                literal("off")
                                .executes(ctx -> disableFly(ctx.getSource()))
                            )

                            .then(
                                literal("speed")
                                .then(
                                    argument("speed", FloatArgumentType.floatArg(0.0f, 2.0f))
                                    .executes(ctx -> {
                                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                                        if (player == null) {
                                            sendError(ctx.getSource(), " This command must be run by a player!");
                                            return 0;
                                        }
                                        float speed = FloatArgumentType.getFloat(ctx, "speed");
                                        FlyData data = flyingPlayers.computeIfAbsent(player.getUuid(), k -> new FlyData());
                                        data.speed = speed;
                                        sendSuccess(ctx.getSource(), " Flight speed set to " + String.format("%.2f", speed));
                                        return 1;
                                    })
                                )
                            )
                    )
            );
        });
    }

    private static int toggleFly(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            sendError(src, " This command must be run by a player!");
            return 0;
        }

        FlyData data = flyingPlayers.computeIfAbsent(player.getUuid(), k -> new FlyData());
        data.flying = !data.flying;

        if (data.flying) {
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
            sendSuccess(src, " Flight enabled!");
            return 1;
        } else {
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
            sendSuccess(src, " Flight disabled!");
            return 1;
        }
    }

    private static int enableFly(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            sendError(src, " This command must be run by a player!");
            return 0;
        }

        FlyData data = flyingPlayers.computeIfAbsent(player.getUuid(), k -> new FlyData());
        if (!data.flying) {
            data.flying = true;
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
            sendSuccess(src, " Flight enabled!");
            return 1;
        }

        sendInfo(src, " Flight is already enabled!");
        return 1;
    }

    private static int disableFly(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            sendError(src, " This command must be run by a player!");
            return 0;
        }

        FlyData data = flyingPlayers.computeIfAbsent(player.getUuid(), k -> new FlyData());
        if (data.flying) {
            data.flying = false;
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
            sendSuccess(src, " Flight disabled!");
            return 1;
        }

        sendInfo(src, " Flight is already disabled!");
        return 1;
    }

    private static void sendSuccess(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.GREEN), false);
    }

    private static void sendError(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.RED), false);
    }

    private static void sendInfo(ServerCommandSource src, String msg) {
        src.sendFeedback(() -> Text.literal(msg).formatted(Formatting.YELLOW), false);
    }
}
