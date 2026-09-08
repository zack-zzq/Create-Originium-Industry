package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.PersistSchema;
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

    /**
     * Current {@code player_exposure_data} schema. Missing {@code version} is
     * {@link PersistSchema#UNVERSIONED}; v0 and v1 both use {@code Exposure}
     * and {@code Infection}.
     */
    public static final int SCHEMA_VERSION = 1;
    public static final String NBT_VERSION = "version";
    public static final String NBT_EXPOSURE = "Exposure";
    public static final String NBT_INFECTION = "Infection";

    private int exposure = 0;
    private int infection = 0;

    public PlayerExposureData() {}

    // --- Exposure ---

    public int getExposure() {
        return exposure;
    }

    public void setExposure(int value) {
        this.exposure = Mth.clamp(value, 0, COIConfig.maxExposure());
    }

    public void addExposure(int amount) {
        setExposure(this.exposure + amount);
    }

    // --- Infection ---

    public int getInfection() {
        return infection;
    }

    public void setInfection(int value) {
        this.infection = Mth.clamp(value, 0, COIConfig.maxInfection());
    }

    public void addInfection(int amount) {
        setInfection(this.infection + amount);
    }

    public InfectionStage getInfectionStage() {
        return InfectionStage.fromInfection(this.infection);
    }

    /**
     * Singleplayer-friendly death handling: scale stored values by the
     * configured retain fractions (0 = clear, 1 = keep). Call after
     * {@code copyOnDeath} has copied this attachment onto the clone.
     */
    public void applyDeathRetention() {
        setExposure(retain(this.exposure, deathRetain(COIConfig.DEATH_EXPOSURE_RETAIN, 0.0)));
        setInfection(retain(this.infection, deathRetain(COIConfig.DEATH_INFECTION_RETAIN, 0.25)));
    }

    /**
     * {@code fraction <= 0} clears; {@code >= 1} keeps the value.
     */
    public static int retain(int value, double fraction) {
        if (value <= 0 || fraction <= 0.0) {
            return 0;
        }
        if (fraction >= 1.0) {
            return value;
        }
        return Math.max(0, (int) Math.round(value * fraction));
    }

    private static double deathRetain(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
        return COIConfig.COMMON_SPEC.isLoaded() ? value.get() : fallback;
    }

    // --- Serialization ---

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        PersistSchema.write(tag, NBT_VERSION, SCHEMA_VERSION);
        tag.putInt(NBT_EXPOSURE, this.exposure);
        tag.putInt(NBT_INFECTION, this.infection);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        CompoundTag payload = migrate(nbt, PersistSchema.read(nbt, NBT_VERSION));
        if (payload.contains(NBT_EXPOSURE, CompoundTag.TAG_INT)) {
            this.exposure = payload.getInt(NBT_EXPOSURE);
        }
        if (payload.contains(NBT_INFECTION, CompoundTag.TAG_INT)) {
            this.infection = payload.getInt(NBT_INFECTION);
        }
    }

    /**
     * Upgrade hook for player exposure/infection. {@code 0} is the unversioned
     * {@code Exposure}/{@code Infection} payload. v0→v1 is identity.
     */
    public static CompoundTag migrate(CompoundTag nbt, int fromVersion) {
        int version = Math.max(fromVersion, PersistSchema.UNVERSIONED);
        if (version > SCHEMA_VERSION) {
            CreateOriginiumIndustry.LOGGER.warn(
                    "player_exposure_data version {} is newer than supported {}; reading known fields.",
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
                    "No player_exposure_data upgrade from version " + fromVersion);
        };
    }

    public PlayerExposureData copy() {
        PlayerExposureData copy = new PlayerExposureData();
        copy.setExposure(this.exposure);
        copy.setInfection(this.infection);
        return copy;
    }
}
