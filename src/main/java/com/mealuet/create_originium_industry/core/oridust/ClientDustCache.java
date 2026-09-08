package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.world.level.ChunkPos;

/**
 * Local player's last received nearby dust snapshot. Safe to class-load on a
 * dedicated server (stays empty); only packet handlers write it.
 */
public final class ClientDustCache {

    private static final DustObservation OBSERVATION = new DustObservation();

    private ClientDustCache() {}

    public static void apply(long[] keys, int[] values) {
        OBSERVATION.apply(keys, values);
    }

    public static int get(ChunkPos pos) {
        return OBSERVATION.get(pos);
    }

    public static int get(long chunkKey) {
        return OBSERVATION.get(chunkKey);
    }

    public static boolean isKnown(ChunkPos pos) {
        return OBSERVATION.isKnown(pos);
    }

    public static int getOrFallback(ChunkPos pos, int fallback) {
        return OBSERVATION.getOrFallback(pos, fallback);
    }

    public static boolean hasAny() {
        return !OBSERVATION.isEmpty() || OBSERVATION.knownCount() > 0;
    }

    public static void clear() {
        OBSERVATION.clear();
    }
}
