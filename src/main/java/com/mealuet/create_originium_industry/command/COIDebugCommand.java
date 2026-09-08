package com.mealuet.create_originium_industry.command;

import com.mealuet.create_originium_industry.advancement.COIAdvancements;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.core.oridust.*;
import com.mealuet.create_originium_industry.index.COIAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
 *     status                 - View targeted power core (or config knobs)
 *     stabilize              - Force stabilize targeted power core
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

        ChunkPos chunkPos = WorldSpace.toDustChunk(player);
        int dust = OriginiumDustManager.getDust(player.serverLevel(), chunkPos);
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
        ChunkPos chunkPos = WorldSpace.toDustChunk(player);

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
        ChunkPos chunkPos = WorldSpace.toDustChunk(player);

        OriginiumDustManager.addDust(level, chunkPos, amount, DustReason.DEBUG);
        int newLevel = OriginiumDustManager.getDust(level, chunkPos);

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
        ChunkPos chunkPos = WorldSpace.toDustChunk(player);

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
        ChunkPos center = WorldSpace.toDustChunk(player);
        ServerLevel level = player.serverLevel();

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.dust.scan", radius
        ), false);

        boolean found = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                int dust = OriginiumDustManager.getDust(level, pos);
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
        ChunkPos chunkPos = WorldSpace.toDustChunk(player);
        int dust = OriginiumDustManager.getDust(player.serverLevel(), chunkPos);
        int exposure = data.getExposure();
        int infection = data.getInfection();

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.exposure.get", exposure, infection, dust
        ), false);
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.infection.stage",
                Component.translatable(data.getInfectionStage().getLangKey())
        ), false);
        return 1;
    }

    private static int exposureSet(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        PlayerExposureData data = COIAttachments.getPlayerExposure(player);
        data.setExposure(amount);
        DustSyncTracker.markExposureDirty(player);
        if (amount > 0) {
            COIAdvancements.dustExposure(player);
        }

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
        DustSyncTracker.markExposureDirty(player);

        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.infection.set", amount
        ), true);
        return 1;
    }

    // ==================== Reactor Commands ====================

    private static int reactorStatus(CommandContext<CommandSourceStack> ctx) {
        PowerCoreBlockEntity core = targetedCore(ctx);
        if (core != null) {
            var snap = core.snapshot();
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.coi_debug.reactor.live",
                    String.format("%.1f", snap.stability()),
                    String.format("%.1f", snap.heat()),
                    String.format("%.1f", snap.capacity()),
                    String.format("%.1f", snap.cooling()),
                    String.format("%.2f", core.instability()),
                    core.fuelCount(),
                    String.valueOf(core.shutdown())
            ), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.coi_debug.reactor.status"
            ), false);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.reactor.status.values",
                COIConfig.REACTOR_CORE_HEAT.get(),
                String.format("%.2f", COIConfig.REACTOR_MOLTEN_HEAT_CAPACITY.get()),
                String.format("%.2f", COIConfig.REACTOR_PUREST_HEAT_CAPACITY.get()),
                String.format("%.2f", COIConfig.REACTOR_COOLING_MULTIPLIER.get()),
                String.format("%.3f", COIConfig.REACTOR_INSTABILITY_GAIN.get()),
                String.format("%.1f", COIConfig.REACTOR_MELTDOWN_THRESHOLD.get()),
                String.valueOf(COIConfig.ENABLE_REACTOR_MELTDOWN.get()),
                COIConfig.REACTOR_MELTDOWN_DUST_BURST.get()
        ), false);
        return 1;
    }

    private static int reactorStabilize(CommandContext<CommandSourceStack> ctx) {
        PowerCoreBlockEntity core = targetedCore(ctx);
        if (core == null) {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.coi_debug.reactor.none"
            ), false);
            return 0;
        }
        core.forceStabilize();
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.coi_debug.reactor.stabilize"
        ), true);
        return 1;
    }

    private static PowerCoreBlockEntity targetedCore(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return null;
        }
        HitResult hit = player.pick(20.0, 0.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
            return null;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockEntity be = player.level().getBlockEntity(pos);
        return be instanceof PowerCoreBlockEntity core ? core : null;
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
