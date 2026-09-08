package com.mealuet.create_originium_industry.effect;

import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class OriDustSicknessEffect extends MobEffect {

    public OriDustSicknessEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % effectInterval() == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Apply increasing slowness and weakness based on amplifier
        // Level 0 (Amplifier 0): Slowness 1
        // Level 1 (Amplifier 1): Slowness 1, Weakness 1
        // Level 2 (Amplifier 2): Slowness 2, Weakness 1
        // Level 3 (Amplifier 3): Slowness 2, Weakness 2
        // Level 4 (Amplifier 4): Slowness 3, Weakness 2

        int interval = effectInterval();
        int slownessAmplifier = amplifier / 2;
        int weaknessAmplifier = (amplifier > 0) ? (amplifier - 1) / 2 : -1;

        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, interval + 5, slownessAmplifier, true, false, true));

        if (weaknessAmplifier >= 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, interval + 5, weaknessAmplifier, true, false, true));
        }

        return true;
    }

    private static int effectInterval() {
        return COIConfig.COMMON_SPEC.isLoaded() ? COIConfig.SICKNESS_EFFECT_INTERVAL.get() : 20;
    }
}
