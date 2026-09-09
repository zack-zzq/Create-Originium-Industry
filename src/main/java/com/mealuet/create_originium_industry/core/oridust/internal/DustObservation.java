package com.mealuet.create_originium_industry.core.oridust.internal;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.world.level.ChunkPos;

/**
 * Client-side (or test) snapshot of chunk dust received over the network.
 * Missing keys read as 0; {@link #isKnown} distinguishes "never synced"
 * from "synced as clean" so the dust meter can fall back to its BE copy.
 */
public final class DustObservation {

    private final Long2IntOpenHashMap dust = new Long2IntOpenHashMap();
    private final LongOpenHashSet known = new LongOpenHashSet();

    public DustObservation() {
        this.dust.defaultReturnValue(0);
    }

    public void apply(long[] keys, int[] values) {
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; i++) {
            known.add(keys[i]);
            if (values[i] <= 0) {
                dust.remove(keys[i]);
            } else {
                dust.put(keys[i], values[i]);
            }
        }
    }

    public int get(ChunkPos pos) {
        return get(pos.toLong());
    }

    public int get(long chunkKey) {
        return dust.get(chunkKey);
    }

    public boolean isKnown(ChunkPos pos) {
        return known.contains(pos.toLong());
    }

    public boolean isKnown(long chunkKey) {
        return known.contains(chunkKey);
    }

    /**
     * Synced value when this chunk has been in a packet; otherwise {@code fallback}
     * (the dust-meter block-entity snapshot).
     */
    public int getOrFallback(ChunkPos pos, int fallback) {
        return isKnown(pos) ? get(pos) : fallback;
    }

    public boolean isEmpty() {
        return dust.isEmpty();
    }

    public int knownCount() {
        return known.size();
    }

    public void clear() {
        dust.clear();
        known.clear();
    }
}
