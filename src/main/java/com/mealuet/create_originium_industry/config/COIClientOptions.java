package com.mealuet.create_originium_industry.config;

/**
 * Safe accessors for {@link COIConfig#CLIENT_SPEC}.
 * <p>
 * Client config is not loaded on a dedicated server. Callers (including
 * particle, HUD, and industrial SFX code) must use these helpers instead of
 * {@code .get()} so a missing spec never crashes.
 * <p>
 * Defaults match the CLIENT spec so singleplayer looks the same before the
 * first config file is written.
 */
public final class COIClientOptions {

    private COIClientOptions() {}

    public static boolean reduceFlicker() {
        return COIConfig.CLIENT_SPEC.isLoaded() && COIConfig.REDUCE_FLICKER.get();
    }

    public static boolean simplifyParticles() {
        return COIConfig.CLIENT_SPEC.isLoaded() && COIConfig.SIMPLIFY_PARTICLES.get();
    }

    public static double particleDensity() {
        if (simplifyParticles()) {
            return 0.0;
        }
        if (!COIConfig.CLIENT_SPEC.isLoaded()) {
            return 0.4;
        }
        return COIConfig.PARTICLE_DENSITY.get();
    }

    public static boolean highContrastIndicators() {
        return COIConfig.CLIENT_SPEC.isLoaded() && COIConfig.HIGH_CONTRAST_INDICATORS.get();
    }

    /**
     * Icon / subtitle channel for critical states. Off by default so existing
     * color-coded goggles and HUD stay unchanged.
     */
    public static boolean nonColorAlerts() {
        return COIConfig.CLIENT_SPEC.isLoaded() && COIConfig.NON_COLOR_ALERTS.get();
    }

    public static UiDetailLevel uiDetailLevel() {
        if (!COIConfig.CLIENT_SPEC.isLoaded()) {
            return UiDetailLevel.STANDARD;
        }
        return COIConfig.UI_DETAIL_LEVEL.get();
    }

    public static DebugOverlayDetail debugOverlayDetail() {
        if (!COIConfig.CLIENT_SPEC.isLoaded()) {
            return DebugOverlayDetail.OFF;
        }
        return COIConfig.DEBUG_OVERLAY_DETAIL.get();
    }

    public static boolean showSicknessHud() {
        boolean show = !COIConfig.CLIENT_SPEC.isLoaded() || COIConfig.SHOW_SICKNESS_HUD.get();
        return show && uiDetailLevel() != UiDetailLevel.MINIMAL;
    }

    /**
     * Scale for ambient client particles (0 = none). Already applies
     * {@link #simplifyParticles()}.
     */
    public static int particleCount(int baseCount) {
        double density = particleDensity();
        if (density <= 0.0 || baseCount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(baseCount * density));
    }

    public static boolean verboseUi() {
        return uiDetailLevel() == UiDetailLevel.VERBOSE;
    }

    /**
     * Master industrial-SFX toggle. Missing client spec (dedicated server /
     * GameTest) keeps sounds conceptually on so helpers stay defined.
     */
    public static boolean industrialSoundsEnabled() {
        return !COIConfig.CLIENT_SPEC.isLoaded() || COIConfig.ENABLE_INDUSTRIAL_SOUNDS.get();
    }

    /**
     * 0–1 volume scale for factory loops. 0 when sounds are disabled.
     */
    public static double soundDensity() {
        if (!industrialSoundsEnabled()) {
            return 0.0;
        }
        if (!COIConfig.CLIENT_SPEC.isLoaded()) {
            return 1.0;
        }
        return COIConfig.SOUND_DENSITY.get();
    }

    public static float machineSoundVolume() {
        return (float) soundDensity();
    }

    /**
     * High-dust ambience: muted by {@link #simplifyParticles()}, scaled by
     * {@link #particleDensity()} and {@link #soundDensity()}.
     */
    public static float ambientDustSoundVolume() {
        if (!industrialSoundsEnabled() || simplifyParticles()) {
            return 0.0F;
        }
        return (float) (soundDensity() * particleDensity());
    }

    /**
     * Alarm / pulse cadence. {@link #reduceFlicker()} holds a steady level
     * instead of flashing the volume.
     */
    public static boolean pulseAudio() {
        return !reduceFlicker();
    }

    public static int soundPeriod(int baseTicks) {
        int base = Math.max(1, baseTicks);
        return reduceFlicker() ? base * 2 : base;
    }

    /**
     * Client ash-particle spawn interval. Reduce-flicker doubles the wait so
     * the fog strobes less.
     */
    public static int particlePeriod() {
        return reduceFlicker() ? 10 : 4;
    }
}
