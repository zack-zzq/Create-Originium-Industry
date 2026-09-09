package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.PersistSchema;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class OriDustData implements INBTSerializable<CompoundTag> {
    /**
     * Current {@code chunk_oridust_data} schema. Missing {@code version} is
     * {@link PersistSchema#UNVERSIONED}; v0 and v1 both use {@code DustLevel}.
     */
    public static final int SCHEMA_VERSION = 1;
    public static final String NBT_VERSION = "version";
    public static final String NBT_KEY = "DustLevel";

    private int dustLevel = 0;

    /**
     * Legacy per-chunk attachment payload (persist contract). Dust is now
     * stored in {@link OriDustSavedData}; this type remains registered so old
     * worlds can be migrated on chunk load, after which the attachment is zeroed.
     */
    public OriDustData() {}

    public int getDustLevel() {
        return dustLevel;
    }

    public void setDustLevel(int level) {
        this.dustLevel = Mth.clamp(level, 0, COIConfig.MAX_DUST_LEVEL.get());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        PersistSchema.write(tag, NBT_VERSION, SCHEMA_VERSION);
        tag.putInt(NBT_KEY, this.dustLevel);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        CompoundTag payload = migrate(nbt, PersistSchema.read(nbt, NBT_VERSION));
        if (payload.contains(NBT_KEY, CompoundTag.TAG_INT)) {
            this.dustLevel = payload.getInt(NBT_KEY);
        } else {
            this.dustLevel = 0;
        }
    }

    /**
     * Upgrade hook for the legacy chunk attachment. {@code 0} is the
     * unversioned {@code DustLevel}-only payload. v0→v1 is identity.
     */
    public static CompoundTag migrate(CompoundTag nbt, int fromVersion) {
        int version = Math.max(fromVersion, PersistSchema.UNVERSIONED);
        if (version > SCHEMA_VERSION) {
            CreateOriginiumIndustry.LOGGER.warn(
                    "chunk_oridust_data version {} is newer than supported {}; reading known fields.",
                    version, SCHEMA_VERSION);
            return nbt;
        }
        while (version < SCHEMA_VERSION) {
            version = upgrade(nbt, version);
        }
        return nbt;
    }

    private static int upgrade(CompoundTag nbt, int fromVersion) {
        return switch (fromVersion) {
            case PersistSchema.UNVERSIONED -> {
                PersistSchema.write(nbt, NBT_VERSION, 1);
                yield 1;
            }
            default -> throw new IllegalStateException(
                    "No chunk_oridust_data upgrade from version " + fromVersion);
        };
    }
}
