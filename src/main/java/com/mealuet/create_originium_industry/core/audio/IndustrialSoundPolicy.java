package com.mealuet.create_originium_industry.core.audio;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.reactor.StabilityMath;

/**
 * Side-agnostic industrial SFX rules. Client loops use these; GameTests cover
 * the same branches without a physical client.
 */
public final class IndustrialSoundPolicy {

    public enum ReactorCue {
        NONE,
        STEADY,
        ALARM
    }

    private IndustrialSoundPolicy() {}

    /**
     * Kinetic filter is working when a sieve is inserted and the shaft is spinning.
     */
    public static boolean filterWorking(boolean hasSieve, float speed) {
        return hasSieve && Math.abs(speed) > 0.01f;
    }

    public static boolean highDustAmbience(DustLevel level) {
        return level == DustLevel.HIGH || level == DustLevel.CRITICAL;
    }

    public static float dustAmbienceGain(DustLevel level) {
        return switch (level) {
            case HIGH -> 0.55f;
            case CRITICAL -> 1.0f;
            default -> 0.0f;
        };
    }

    /**
     * Alarm when the core is generating and either S is negative or instability
     * has crossed the warning knob. Steady otherwise while generating.
     */
    public static ReactorCue reactorCue(boolean generating, double stability, double instability) {
        if (!generating) {
            return ReactorCue.NONE;
        }
        if (instability >= COIConfig.reactorInstabilityWarning()) {
            return ReactorCue.ALARM;
        }
        if (StabilityMath.sign(stability) == StabilityMath.Sign.NEGATIVE) {
            return ReactorCue.ALARM;
        }
        return ReactorCue.STEADY;
    }

    public static boolean shouldPlayCaptureOneShot(long gameTime, long lastPlayedTick, int intervalTicks) {
        int interval = Math.max(1, intervalTicks);
        return gameTime - lastPlayedTick >= interval;
    }
}
