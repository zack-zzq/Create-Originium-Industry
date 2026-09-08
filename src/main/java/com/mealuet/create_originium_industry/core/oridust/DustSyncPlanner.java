package com.mealuet.create_originium_industry.core.oridust;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.ChunkPos;

import java.util.function.LongToIntFunction;

/**
 * Pure nearby / dirty-set planner for chunk-dust client sync.
 * <p>
 * Never walks the full SavedData map. A packet contains only logical chunks
 * inside the observer's Chebyshev radius, and only when they are dirty or
 * (on a window refresh) differ from the last value sent to that observer.
 */
public final class DustSyncPlanner {

    /**
     * Sentinel stored in per-observer last-sent maps: this chunk has never
     * been included in a packet for that player.
     */
    public static final int NEVER_SENT = Integer.MIN_VALUE;

    private DustSyncPlanner() {}

    public record DustDelta(long[] keys, int[] values) {
        public boolean isEmpty() {
            return keys.length == 0;
        }

        public int size() {
            return keys.length;
        }

        public int valueOf(long key, int fallback) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    return values[i];
                }
            }
            return fallback;
        }
    }

    public static boolean inRadius(ChunkPos center, int radius, int chunkX, int chunkZ) {
        return Math.max(Math.abs(center.x - chunkX), Math.abs(center.z - chunkZ)) <= radius;
    }

    /**
     * @param forceWindow {@code true} on login or when the observer's logical
     *                    chunk changed — include every nearby value that differs
     *                    from last-sent (skip never-sent zeros). {@code false}
     *                    while standing still: dirty nearby chunks only.
     */
    public static DustDelta plan(
            ChunkPos center,
            int radius,
            LongSet dirty,
            LongToIntFunction lastSent,
            LongToIntFunction currentDust,
            boolean forceWindow
    ) {
        LongArrayList keys = new LongArrayList();
        IntArrayList values = new IntArrayList();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = center.x + dx;
                int z = center.z + dz;
                long key = ChunkPos.asLong(x, z);
                int current = currentDust.applyAsInt(key);
                int last = lastSent.applyAsInt(key);
                boolean dirtyHere = dirty.contains(key);
                if (!shouldSend(current, last, dirtyHere, forceWindow)) {
                    continue;
                }
                keys.add(key);
                values.add(current);
            }
        }
        return new DustDelta(keys.toLongArray(), values.toIntArray());
    }

    public static boolean shouldSend(int current, int last, boolean dirty, boolean forceWindow) {
        if (forceWindow) {
            if (last == NEVER_SENT && current == 0) {
                return false;
            }
            return last != current;
        }
        if (!dirty) {
            return false;
        }
        return last != current;
    }

    public static void applySent(Long2IntMap lastSent, DustDelta delta) {
        for (int i = 0; i < delta.keys.length; i++) {
            lastSent.put(delta.keys[i], delta.values[i]);
        }
    }

    /**
     * Drops remembered <em>zeros</em> outside the window. Non-zero last-sent
     * values are kept so a later return can send a clear (0) instead of
     * leaving a stale client reading.
     */
    public static void pruneOutsideRadius(Long2IntMap lastSent, ChunkPos center, int radius) {
        lastSent.long2IntEntrySet().removeIf(entry -> {
            ChunkPos pos = new ChunkPos(entry.getLongKey());
            if (inRadius(center, radius, pos.x, pos.z)) {
                return false;
            }
            return entry.getIntValue() <= 0;
        });
    }
}
