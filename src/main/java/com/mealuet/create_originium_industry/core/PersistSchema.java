package com.mealuet.create_originium_industry.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Shared NBT schema-version helpers for SavedData and attachments.
 * <p>
 * A missing version field is {@link #UNVERSIONED} ({@code 0}) so worlds written
 * before the field existed still load. Writers stamp the current schema version
 * on the next save. Each persistent type owns a {@code migrate} hook that steps
 * from {@code 0} toward its current version.
 */
public final class PersistSchema {

    public static final int UNVERSIONED = 0;

    private PersistSchema() {}

    public static int read(CompoundTag nbt, String key) {
        if (nbt != null && nbt.contains(key, Tag.TAG_INT)) {
            return nbt.getInt(key);
        }
        return UNVERSIONED;
    }

    public static void write(CompoundTag nbt, String key, int version) {
        nbt.putInt(key, version);
    }
}
