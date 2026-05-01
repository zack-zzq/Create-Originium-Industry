package com.mealuet.create_originium_industry.command;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.*;
import com.mealuet.create_originium_industry.index.COIAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/**
 * Debug commands for Create: Originium Industry development and testing.
 * All commands require OP permission level 2.
 * <p>
 * Command tree:
 * <pre>
 * /coi_debug
 *   dust
 *     get                    - View current chunk dust level
 *     set <amount>           - Set current chunk dust level
 *     add <amount>           - Add dust to current chunk
 *     clear                  - Clear current chunk dust
 *     scan <radius>          - Scan surrounding chunks
 *   exposure
 *     get                    - View player exposure/infection (P3)
 *     set <amount>           - Set player exposure (P3)
 *   infection
 *     set <amount>           - Set player infection (P3)
 *   reactor
 *     status                 - View reactor status (P6)
 *     stabilize              - Force stabilize reactor (P6)
 * </pre>
 */
public class COIDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("coi_debug")
                .requires(source -> source.hasPermission(2) && COIConfig.ENABLE_DEBUG_COMMANDS.get())
                .then(Commands.literal("dust")
                    .then(Commands.literal("get").executes(COIDebugCommand::dustGet))
                    .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(COIDebugCommand::dustSet)))
                    .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(COIDebugCommand::dustAdd)))
                    .then(Commands.literal("clear").executes(COIDebugCommand::dustClear))
                    .then(Commands.literal("scan")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 8))
                            .executes(COIDebugCommand::dustScan)))
                )
                .then(Commands.literal("exposure")
                    .then(Commands.literal("get").executes(COIDebugCommand::exposureGet))
                    .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(COIDebugCommand::exposureSet)))
                )
                .then(Commands.literal("infection")
                    .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(COIDebugCommand::infectionSet)))
                )
                .then(Commands.literal("reactor")
                    .then(Commands.literal("status").executes(COIDebugCommand::reactorStatus))
                    .then(Commands.literal("stabilize").executes(COIDebugCommand::reactorStabilize))
                )
        );
    }

    // ==================== Dust Commands ====================

    private static int dustGet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        ChunkPos chunkPos = player.chunkPosition();
        int dust = OriginiumDustManager.getDust(chunkPos);
        DustLevel level = DustLevel.fromDust(dust);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.get",
                String.valueOf(chunkPos.x), String.valueOf(chunkPos.z), dust
        ).append(" ").append(Component.translatable(level.getLangKey())
                .withColor(dustLevelColor(level))), false);
        return 1;
    }

    private static int dustSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        ServerLevel level = player.serverLevel();
        ChunkPos chunkPos = player.chunkPosition();

        OriginiumDustManager.setDust(level, chunkPos, amount, DustReason.DEBUG);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.set",
                String.valueOf(chunkPos.x), String.valueOf(chunkPos.z), amount
        ), true);
        return 1;
    }

    private static int dustAdd(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        ServerLevel level = player.serverLevel();
        ChunkPos chunkPos = player.chunkPosition();

        OriginiumDustManager.addDust(level, chunkPos, amount, DustReason.DEBUG);
        int newLevel = OriginiumDustManager.getDust(chunkPos);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.add",
                amount, String.valueOf(chunkPos.x), String.valueOf(chunkPos.z), newLevel
        ), true);
        return 1;
    }

    private static int dustClear(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        ServerLevel level = player.serverLevel();
        ChunkPos chunkPos = player.chunkPosition();

        OriginiumDustManager.clearDust(level, chunkPos, DustReason.DEBUG);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.clear",
                String.valueOf(chunkPos.x), String.valueOf(chunkPos.z)
        ), true);
        return 1;
    }

    private static int dustScan(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int radius = IntegerArgumentType.getInteger(ctx, "radius");
        ChunkPos center = player.chunkPosition();

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.scan", radius
        ), false);

        boolean found = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                int dust = OriginiumDustManager.getDust(pos);
                if (dust > 0) {
                    found = true;
                    DustLevel dustLevel = DustLevel.fromDust(dust);
                    final int dustFinal = dust;
                    final String cx = String.valueOf(pos.x);
                    final String cz = String.valueOf(pos.z);
                    final int dist = Math.abs(dx) + Math.abs(dz);
                    ctx.getSource().sendSuccess(() -> Component.translatable(
                            "commands.coi_debug.dust.scan.entry", cx, cz, dustFinal
                    ).append(" ").append(Component.translatable(dustLevel.getLangKey())
                            .withColor(dustLevelColor(dustLevel)))
                     .append(Component.literal(" (d=" + dist + ")")), false);
                }
            }
        }

        if (!found) {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.coi_debug.dust.scan.none"
            ), false);
        }

        return 1;
    }

    // ==================== Exposure Commands ====================

    private static int exposureGet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        PlayerExposureData data = COIAttachments.getPlayerExposure(player);
        ChunkPos chunkPos = player.chunkPosition();
        int dust = OriginiumDustManager.getDust(chunkPos);
        int exposure = data.getExposure();
        int infection = data.getInfection();

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.exposure.get", exposure, infection, dust
        ), false);
        return 1;
    }

    private static int exposureSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        PlayerExposureData data = COIAttachments.getPlayerExposure(player);
        data.setExposure(amount);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.exposure.set", amount
        ), true);
        return 1;
    }

    // ==================== Infection Commands ====================

    private static int infectionSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        PlayerExposureData data = COIAttachments.getPlayerExposure(player);
        data.setInfection(amount);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.infection.set", amount
        ), true);
        return 1;
    }

    // ==================== Reactor Commands ====================

    private static int reactorStatus(CommandContext<CommandSourceStack> ctx) {
        // TODO: P6 — read reactor status from targeted block entity
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.reactor.status"
        ), false);
        return 1;
    }

    private static int reactorStabilize(CommandContext<CommandSourceStack> ctx) {
        // TODO: P6 — force stabilize targeted reactor
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.reactor.stabilize"
        ), false);
        return 1;
    }

    // ==================== Helpers ====================

    /**
     * Returns an ARGB color int for chat coloring based on dust level.
     */
    private static int dustLevelColor(DustLevel level) {
        return switch (level) {
            case SAFE -> 0x55FF55;      // green
            case LOW -> 0xFFFF55;       // yellow
            case MEDIUM -> 0xFFAA00;    // orange
            case HIGH -> 0xFF5555;      // red
            case CRITICAL -> 0xAA0000;  // dark red
        };
    }
}
