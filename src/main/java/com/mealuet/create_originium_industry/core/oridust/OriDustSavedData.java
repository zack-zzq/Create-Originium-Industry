package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Overworld {@link SavedData} store for originium dust.
 * <p>
 * This is the sole source of truth for dust levels. Keys are logical
 * {@link ChunkPos#toLong()} values (Sable/Aeronautics remaps plot-grid
 * positions to logical overworld coords before they reach this class).
 * Persistence does not require the corresponding chunk to be loaded —
 * that was the failure mode of the old chunk-attachment cache.
 * <p>
 * Stored on the Overworld {@link net.minecraft.world.level.storage.DimensionDataStorage}
 * even when a write originates from another server level.
 */
public class OriDustSavedData extends SavedData {

    public static final String DATA_NAME = "create_originium_industry_ori_dust";
    private static final int FORMAT_VERSION = 1;

    private static final String NBT_VERSION = "Version";
    private static final String NBT_CHUNK_KEYS = "ChunkKeys";
    private static final String NBT_DUST_VALUES = "DustValues";
    private static final String NBT_MIGRATED = "MigratedChunks";

    public static final Factory<OriDustSavedData> FACTORY = new Factory<>(
            OriDustSavedData::new,
            OriDustSavedData::load
    );

    private final Long2IntOpenHashMap dustByChunk = new Long2IntOpenHashMap();
    /**
     * Logical chunks whose legacy {@code CHUNK_DUST_TYPE} attachment has already
     * been merged. Prevents double-counting if a chunk unloads before the
     * zeroed attachment is written to disk.
     */
    private final LongOpenHashSet migratedChunks = new LongOpenHashSet();

    public OriDustSavedData() {
        this.dustByChunk.defaultReturnValue(0);
    }

    /**
     * Returns the Overworld dust store for this server, creating it if needed.
     */
    public static OriDustSavedData get(ServerLevel anyLevel) {
        ServerLevel overworld = anyLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            overworld = anyLevel.getServer().overworld();
        }
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static OriDustSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        OriDustSavedData data = new OriDustSavedData();
        long[] keys = tag.getLongArray(NBT_CHUNK_KEYS);
        int[] values = tag.getIntArray(NBT_DUST_VALUES);
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; i++) {
            if (values[i] > 0) {
                data.dustByChunk.put(keys[i], values[i]);
            }
        }
        for (long migrated : tag.getLongArray(NBT_MIGRATED)) {
            data.migratedChunks.add(migrated);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(NBT_VERSION, FORMAT_VERSION);

        int size = this.dustByChunk.size();
        long[] keys = new long[size];
        int[] values = new int[size];
        int i = 0;
        for (Long2IntMap.Entry entry : this.dustByChunk.long2IntEntrySet()) {
            keys[i] = entry.getLongKey();
            values[i] = entry.getIntValue();
            i++;
        }
        tag.putLongArray(NBT_CHUNK_KEYS, keys);
        tag.putIntArray(NBT_DUST_VALUES, values);
        tag.putLongArray(NBT_MIGRATED, this.migratedChunks.toLongArray());
        return tag;
    }

    public int get(ChunkPos pos) {
        return this.dustByChunk.get(pos.toLong());
    }

    public int get(long chunkKey) {
        return this.dustByChunk.get(chunkKey);
    }

    /**
     * Sets the dust level, clamped to {@code [0, maxDustLevel]}. Zero values
     * are removed from the map so unloaded clean chunks do not bloat NBT.
     *
     * @return the clamped value that was stored
     */
    public int set(ChunkPos pos, int dust) {
        int clamped = clamp(dust);
        long key = pos.toLong();
        if (clamped <= 0) {
            if (this.dustByChunk.remove(key) != 0) {
                this.setDirty();
            }
            return 0;
        }
        int previous = this.dustByChunk.put(key, clamped);
        if (previous != clamped) {
            this.setDirty();
        }
        return clamped;
    }

    public int add(ChunkPos pos, int amount) {
        return set(pos, get(pos) + amount);
    }

    /**
     * Merges a legacy chunk-attachment value into this store exactly once per
     * logical chunk. Subsequent loads of the same attachment are ignored even
     * if the attachment was not yet zeroed on disk.
     */
    public void mergeLegacyAttachment(ChunkPos pos, int legacyAmount) {
        long key = pos.toLong();
        if (!this.migratedChunks.add(key)) {
            return;
        }
        if (legacyAmount > 0) {
            int merged = clamp(get(pos) + legacyAmount);
            if (merged > 0) {
                this.dustByChunk.put(key, merged);
            } else {
                this.dustByChunk.remove(key);
            }
        }
        this.setDirty();
    }

    public boolean isMigrated(ChunkPos pos) {
        return this.migratedChunks.contains(pos.toLong());
    }

    public boolean isEmpty() {
        return this.dustByChunk.isEmpty();
    }

    private static int clamp(int dust) {
        return Mth.clamp(dust, 0, COIConfig.MAX_DUST_LEVEL.get());
    }
}
