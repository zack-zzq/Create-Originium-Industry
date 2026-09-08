package com.mealuet.create_originium_industry.config;

/**
 * Dedicated-server policy for how originium dust simulation expands beyond
 * the singleplayer active set. Singleplayer always uses the base
 * {@code dust_diffusion} knobs; this enum only changes dedicated servers.
 * <p>
 * Defaults to {@link #PLAYER_LOCAL} so a dedicated server behaves like
 * singleplayer unless an operator opts into a more aggressive or frozen policy.
 */
public enum PollutionSpreadStrategy {
    /**
     * Same active-set rules as singleplayer: player radius plus recently
     * written machine chunks and their 4-neighbors.
     */
    PLAYER_LOCAL,
    /**
     * Faster diffusion and a larger player radius on dedicated servers, so
     * pollution keeps moving even when players are spread out.
     */
    AGGRESSIVE,
    /**
     * Isolated machines do not bleed into neighbors. Only chunks inside the
     * player radius (plus the written chunk itself) simulate; far pollution
     * stays frozen until someone approaches.
     */
    FROZEN_FAR;

    public double dedicatedDiffusionScale() {
        return switch (this) {
            case PLAYER_LOCAL, FROZEN_FAR -> 1.0;
            case AGGRESSIVE -> 1.5;
        };
    }

    public int dedicatedRadiusBonus() {
        return switch (this) {
            case PLAYER_LOCAL, FROZEN_FAR -> 0;
            case AGGRESSIVE -> 4;
        };
    }

    /**
     * When true, recently written keys do not auto-include 4-neighbors in the
     * active set (dust stays in the machine chunk until a player is nearby).
     */
    public boolean isolateMachineSpread() {
        return this == FROZEN_FAR;
    }
}
