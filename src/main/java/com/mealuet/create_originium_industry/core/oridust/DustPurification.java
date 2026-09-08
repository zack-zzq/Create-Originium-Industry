package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared purification path for machine emission and (via {@link IDustPurifier})
 * ambient chunk absorption.
 * <p>
 * Nearby search is the emission block plus its six neighbours so a Basin mixer
 * (one block above the basin) can see a future sieve attachment on the basin,
 * and so the kinetic filter can sit beside a mill or crushing wheel.
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
     * adjacent faces. Order is self, then {@link Direction} declaration order.
     */
    public static List<IDustPurifier> findNearby(ServerLevel level, BlockPos pos) {
        List<IDustPurifier> list = new ArrayList<>(7);
        if (level == null || pos == null) {
            return list;
        }
        addIfPurifier(level, pos, list);
        for (Direction direction : Direction.values()) {
            addIfPurifier(level, pos.relative(direction), list);
        }
        return list;
    }

    private static void addIfPurifier(ServerLevel level, BlockPos pos, List<IDustPurifier> list) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IDustPurifier purifier) {
            list.add(purifier);
        }
    }
}
