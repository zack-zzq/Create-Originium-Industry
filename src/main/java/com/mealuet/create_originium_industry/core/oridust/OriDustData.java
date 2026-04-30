package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class OriDustData implements INBTSerializable<CompoundTag> {
    private int dustLevel = 0;
    private static final String NBT_KEY = "DustLevel";

    public OriDustData() {}

    public int getDustLevel() {
        return dustLevel;
    }

    public void setDustLevel(int level) {
        this.dustLevel = Mth.clamp(level, 0, COIConfig.MAX_DUST_LEVEL.get());
    }

    public void addDust(int amount) {
        setDustLevel(this.dustLevel + amount);
    }

    public void removeDust(int amount) {
        setDustLevel(this.dustLevel - amount);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_KEY, this.dustLevel);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        if (nbt.contains(NBT_KEY, CompoundTag.TAG_INT)) {
            this.dustLevel = nbt.getInt(NBT_KEY);
        } else {
            this.dustLevel = 0;
        }
    }

    public OriDustData copy() {
        OriDustData copy = new OriDustData();
        copy.setDustLevel(this.dustLevel);
        return copy;
    }
}
