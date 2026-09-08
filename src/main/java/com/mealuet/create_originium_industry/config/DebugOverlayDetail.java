package com.mealuet.create_originium_industry.config;

/**
 * Extra client debug HUD detail. Off by default so singleplayer is uncluttered.
 * Dust numbers require client sync (see {@code multiplayer.syncDustToClients});
 * until that exists, FULL still shows effect-based status only.
 */
public enum DebugOverlayDetail {
    OFF,
    COMPACT,
    FULL
}
