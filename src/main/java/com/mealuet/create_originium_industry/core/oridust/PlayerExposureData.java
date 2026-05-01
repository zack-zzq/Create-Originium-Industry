package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Per-player persistent data for originium exposure system.
 * <ul>
 *   <li><b>Exposure</b> (暴露值): Short-term accumulation from being in dusty chunks.
 *       Rises when in high-dust areas, decays naturally when in clean areas.
 *       Drives the {@code ori_dust_sickness} effect intensity.</li>
 *   <li><b>Infection</b> (感染值): Long-term accumulation that represents permanent
 *       contamination. Rises slowly when exposure is high, does NOT naturally decay.
 *       At high levels causes persistent debuffs even in clean areas.</li>
 * </ul>
 * Stored as a NeoForge Attachment on the player entity.
 */
public class PlayerExposureData implements INBTSerializable<CompoundTag> {

    private static final String NBT_EXPOSURE = "Exposure";
    private static final String NBT_INFECTION = "Infection";

    private int exposure = 0;
    private int infection = 0;

    public PlayerExposureData() {}

    // --- Exposure ---

    public int getExposure() {
        return exposure;
    }

    public void setExposure(int value) {
        this.exposure = Mth.clamp(value, 0, 10000);
    }

    public void addExposure(int amount) {
        setExposure(this.exposure + amount);
    }

    // --- Infection ---

    public int getInfection() {
        return infection;
    }

    public void setInfection(int value) {
        this.infection = Mth.clamp(value, 0, 10000);
    }

    public void addInfection(int amount) {
        setInfection(this.infection + amount);
    }

    // --- Serialization ---

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_EXPOSURE, this.exposure);
        tag.putInt(NBT_INFECTION, this.infection);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        if (nbt.contains(NBT_EXPOSURE, CompoundTag.TAG_INT)) {
            this.exposure = nbt.getInt(NBT_EXPOSURE);
        }
        if (nbt.contains(NBT_INFECTION, CompoundTag.TAG_INT)) {
            this.infection = nbt.getInt(NBT_INFECTION);
        }
    }

    public PlayerExposureData copy() {
        PlayerExposureData copy = new PlayerExposureData();
        copy.setExposure(this.exposure);
        copy.setInfection(this.infection);
        return copy;
    }
}
