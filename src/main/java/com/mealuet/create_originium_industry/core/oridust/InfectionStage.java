package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Long-term infection course (结晶/病程). Separate from
 * {@code ori_dust_sickness}, which stays the short-term exposure-layer effect.
 * <p>
 * Four playable tiers: weakness → restricted action → growth → cost/benefit.
 * Thresholds come from the additive {@code infection} config section.
 */
public enum InfectionStage {
    NONE("none"),
    WEAKNESS("weakness"),
    RESTRICTED("restricted"),
    GROWTH("growth"),
    BARGAIN("bargain");

    private static final int FALLBACK_WEAKNESS = 200;
    private static final int FALLBACK_RESTRICTED = 800;
    private static final int FALLBACK_GROWTH = 2500;
    private static final int FALLBACK_BARGAIN = 6000;

    private final String id;

    InfectionStage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLangKey() {
        return "infection_stage.create_originium_industry." + id;
    }

    public boolean hasSymptoms() {
        return this != NONE;
    }

    /**
     * Maps stored infection to a stage. Disabled config or zero infection
     * always returns {@link #NONE}.
     */
    public static InfectionStage fromInfection(int infection) {
        if (infection <= 0 || !stagesEnabled()) {
            return NONE;
        }
        if (infection >= threshold(COIConfig.INFECTION_STAGE_BARGAIN, FALLBACK_BARGAIN)) {
            return BARGAIN;
        }
        if (infection >= threshold(COIConfig.INFECTION_STAGE_GROWTH, FALLBACK_GROWTH)) {
            return GROWTH;
        }
        if (infection >= threshold(COIConfig.INFECTION_STAGE_RESTRICTED, FALLBACK_RESTRICTED)) {
            return RESTRICTED;
        }
        if (infection >= threshold(COIConfig.INFECTION_STAGE_WEAKNESS, FALLBACK_WEAKNESS)) {
            return WEAKNESS;
        }
        return NONE;
    }

    /**
     * Applies this stage's vanilla effects. No custom screen; icons are the
     * readable feedback. Bargain is a genuine trade: haste/strength vs hunger.
     */
    public void apply(LivingEntity entity, int durationTicks) {
        if (entity == null || this == NONE || durationTicks <= 0) {
            return;
        }
        int duration = durationTicks;
        switch (this) {
            case WEAKNESS -> entity.addEffect(effect(MobEffects.WEAKNESS, duration, 0));
            case RESTRICTED -> {
                entity.addEffect(effect(MobEffects.WEAKNESS, duration, 0));
                entity.addEffect(effect(MobEffects.DIG_SLOWDOWN, duration, 0));
            }
            case GROWTH -> {
                entity.addEffect(effect(MobEffects.DIG_SLOWDOWN, duration, 1));
                entity.addEffect(effect(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
                entity.addEffect(effect(MobEffects.HUNGER, duration, 0));
            }
            case BARGAIN -> {
                entity.addEffect(effect(MobEffects.DIG_SPEED, duration, 0));
                entity.addEffect(effect(MobEffects.DAMAGE_BOOST, duration, 0));
                entity.addEffect(effect(MobEffects.HUNGER, duration, 1));
            }
            default -> {
            }
        }
    }

    private static MobEffectInstance effect(
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> type,
            int duration,
            int amplifier
    ) {
        return new MobEffectInstance(type, duration, amplifier, true, false, true);
    }

    private static boolean stagesEnabled() {
        return !COIConfig.COMMON_SPEC.isLoaded() || COIConfig.ENABLE_INFECTION_STAGES.get();
    }

    private static int threshold(ModConfigSpec.IntValue value, int fallback) {
        return COIConfig.COMMON_SPEC.isLoaded() ? value.get() : fallback;
    }
}
