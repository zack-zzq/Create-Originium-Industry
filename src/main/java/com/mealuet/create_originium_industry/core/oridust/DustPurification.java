package com.mealuet.create_originium_industry.core.oridust;

import com.simibubi.create.content.processing.basin.BasinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared purification path for machine emission and (via {@link IDustPurifier})
 * ambient chunk absorption.
 * <p>
 * Nearby search is the emission block plus its six neighbours so a kinetic
 * filter can sit beside a mill or crushing wheel. If a neighbour is a Basin,
 * purifiers attached to that basin (its six faces) are included too — a mixer
 * one block above the basin then sees a process sieve on the basin's side.
 */
public final class DustPurification {

    private DustPurification() {}

    /**
     * Apply {@code purifiers} in list order. Each active purifier captures
     * {@code floor(remaining * factor)} of what is left (factor clamped to
     * {@code [0, 1]}). Remainder is what {@link IOridustProducer} should
     * deposit into the world.
     */
    public static PurificationResult reduceEmission(int incoming, List<IDustPurifier> purifiers) {
        int amount = Math.max(0, incoming);
        if (amount == 0 || purifiers == null || purifiers.isEmpty()) {
            return PurificationResult.unchanged(amount);
        }

        int remaining = amount;
        int capturedTotal = 0;
        int byproductTotal = 0;

        for (IDustPurifier purifier : purifiers) {
            if (purifier == null || !purifier.isPurifierActive() || remaining <= 0) {
                continue;
            }
            double factor = Math.max(0.0, Math.min(1.0, purifier.emissionCaptureFactor()));
            if (factor <= 0.0) {
                continue;
            }
            int captured = (int) Math.round(remaining * factor);
            captured = Math.min(remaining, Math.max(0, captured));
            if (captured <= 0) {
                continue;
            }
            remaining -= captured;
            capturedTotal += captured;
            int items = Math.max(0, purifier.acceptCapturedDust(captured));
            byproductTotal += items;
        }

        return new PurificationResult(amount, remaining, capturedTotal, byproductTotal);
    }

    /**
     * Reduce emission using purifiers at {@code pos} and its six neighbours.
     */
    public static PurificationResult reduceNearby(ServerLevel level, BlockPos pos, int incoming) {
        if (level == null || pos == null) {
            return PurificationResult.unchanged(Math.max(0, incoming));
        }
        return reduceEmission(incoming, findNearby(level, pos));
    }

    /**
     * Block entities implementing {@link IDustPurifier} at {@code pos} and
     * adjacent faces. Order is self, then {@link Direction} declaration order,
     * then (one hop) neighbours of any adjacent Basin.
     */
    public static List<IDustPurifier> findNearby(ServerLevel level, BlockPos pos) {
        List<IDustPurifier> list = new ArrayList<>(16);
        if (level == null || pos == null) {
            return list;
        }
        Set<BlockPos> seen = new HashSet<>();
        addIfPurifier(level, pos, list, seen);
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            addIfPurifier(level, neighbour, list, seen);
            if (BasinBlock.isBasin(level, neighbour)) {
                for (Direction aroundBasin : Direction.values()) {
                    addIfPurifier(level, neighbour.relative(aroundBasin), list, seen);
                }
            }
        }
        return list;
    }

    private static void addIfPurifier(ServerLevel level, BlockPos pos, List<IDustPurifier> list, Set<BlockPos> seen) {
        if (!seen.add(pos)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IDustPurifier purifier) {
            list.add(purifier);
        }
    }
}
