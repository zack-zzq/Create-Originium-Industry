package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.PersistSchema;
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
    /**
     * Current Overworld dust-store schema. Missing {@code Version} is
     * {@link PersistSchema#UNVERSIONED}; v0 and v1 share the same array layout.
     * The key is PascalCase because this store already shipped {@code Version}.
     */
    public static final int SCHEMA_VERSION = 1;
    public static final String NBT_VERSION = "Version";
    public static final String NBT_CHUNK_KEYS = "ChunkKeys";
    public static final String NBT_DUST_VALUES = "DustValues";
    public static final String NBT_MIGRATED = "MigratedChunks";

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
        int version = PersistSchema.read(tag, NBT_VERSION);
        data.readPayload(migrate(tag, version));
        return data;
    }

    /**
     * Upgrade hook for the Overworld dust store.
     * <p>
     * {@link PersistSchema#UNVERSIONED} ({@code 0}) is any payload written before
     * {@code Version} existed. v0 already used {@code ChunkKeys}/{@code DustValues}/
     * {@code MigratedChunks}, so v0→v1 is identity. Add a step here when those
     * arrays change shape, then bump {@link #SCHEMA_VERSION}.
     */
    public static CompoundTag migrate(CompoundTag tag, int fromVersion) {
        int version = Math.max(fromVersion, PersistSchema.UNVERSIONED);
        if (version > SCHEMA_VERSION) {
            CreateOriginiumIndustry.LOGGER.warn(
                    "OriDustSavedData version {} is newer than supported {}; reading known fields.",
                    version, SCHEMA_VERSION);
            return tag;
        }
        while (version < SCHEMA_VERSION) {
            version = upgrade(tag, version);
        }
        return tag;
    }

    /**
     * One schema step. v0→v1 does not remap keys. Add a {@code case} and bump
     * {@link #SCHEMA_VERSION} when the array layout changes.
     */
    private static int upgrade(CompoundTag tag, int fromVersion) {
        return switch (fromVersion) {
            case PersistSchema.UNVERSIONED -> {
                PersistSchema.write(tag, NBT_VERSION, 1);
                yield 1;
            }
            default -> throw new IllegalStateException(
                    "No OriDustSavedData upgrade from version " + fromVersion);
        };
    }

    private void readPayload(CompoundTag tag) {
        long[] keys = tag.getLongArray(NBT_CHUNK_KEYS);
        int[] values = tag.getIntArray(NBT_DUST_VALUES);
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; i++) {
            if (values[i] > 0) {
                this.dustByChunk.put(keys[i], values[i]);
            }
        }
        for (long migrated : tag.getLongArray(NBT_MIGRATED)) {
            this.migratedChunks.add(migrated);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        PersistSchema.write(tag, NBT_VERSION, SCHEMA_VERSION);

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
