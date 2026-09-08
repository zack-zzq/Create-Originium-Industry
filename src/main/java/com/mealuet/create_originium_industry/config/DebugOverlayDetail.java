package com.mealuet.create_originium_industry.config;

/**
 * Extra client debug HUD detail. Off by default so singleplayer is uncluttered.
 * FULL shows {@link com.mealuet.create_originium_industry.core.oridust.VisibleDust}
 * numbers once {@code multiplayer.syncDustToClients} is on.
 */
public enum DebugOverlayDetail {
    OFF,
    COMPACT,
    FULL
}
