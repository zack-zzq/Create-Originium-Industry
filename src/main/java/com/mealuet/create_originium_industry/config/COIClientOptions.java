package com.mealuet.create_originium_industry.config;

/**
 * Safe accessors for {@link COIConfig#CLIENT_SPEC}.
 * <p>
 * Client config is not loaded on a dedicated server. Callers (including
 * future particle/HUD code) must use these helpers instead of {@code .get()}
 * so a missing spec never crashes.
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
}
