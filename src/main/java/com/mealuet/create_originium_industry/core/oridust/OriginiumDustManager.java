package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Central public API for the originium dust system.
 * <p>
 * All external code (commands, machine hooks, reactor, filters) should use this
 * manager instead of directly accessing {@link DustCacheManager} or
 * {@link DustDiffusionEngine}. This ensures consistent logging, clamping,
 * and future extensibility.
 *
 * <h3>Usage examples:</h3>
 * <pre>
 * OriginiumDustManager.addDust(serverLevel, chunkPos, 100, DustReason.MACHINE_PROCESSING);
 * int dust = OriginiumDustManager.getDust(serverLevel, chunkPos);
 * OriginiumDustManager.clearDust(serverLevel, chunkPos, DustReason.DEBUG);
 * </pre>
 */
public final class OriginiumDustManager {

    private OriginiumDustManager() {}

    // ==================== Query ====================

    /**
     * Gets the dust level for a chunk. Works for both cached (loaded) and
     * uncached chunks — returns 0 for unloaded chunks.
     */
    public static int getDust(ServerLevel level, ChunkPos pos) {
        return DustCacheManager.getDustLevel(pos);
    }

    /**
     * Gets the dust level for the chunk at the caller's position.
     * Convenience overload.
     */
    public static int getDust(ChunkPos pos) {
        return DustCacheManager.getDustLevel(pos);
    }

    /**
     * Returns the classified risk level for a chunk.
     */
    public static DustLevel getDustLevel(ChunkPos pos) {
        return DustLevel.fromDust(getDust(pos));
    }

    /**
     * Checks if a chunk currently has dust data loaded in the cache.
     */
    public static boolean isTracked(ChunkPos pos) {
        return DustCacheManager.isChunkLoaded(pos);
    }

    // ==================== Mutation ====================

    /**
     * Adds dust to a chunk. Amount is clamped to [0, maxDustLevel].
     *
     * @param level  the server level (must be server side)
     * @param pos    the chunk position
     * @param amount amount of dust to add (can be negative to remove)
     * @param reason why the dust is being added
     */
    public static void addDust(ServerLevel level, ChunkPos pos, int amount, DustReason reason) {
        int current = DustCacheManager.getDustLevel(pos);
        int maxDust = COIConfig.MAX_DUST_LEVEL.get();
        int newLevel = Math.max(0, Math.min(maxDust, current + amount));

        if (newLevel != current) {
            DustCacheManager.syncDustLevel(level, pos, newLevel);
            logDustChange(pos, current, newLevel, reason);
        }
    }

    /**
     * Sets the dust level for a chunk directly. Clamped to [0, maxDustLevel].
     *
     * @param level    the server level
     * @param pos      the chunk position
     * @param newDust  the new dust level
     * @param reason   why the dust is being set
     */
    public static void setDust(ServerLevel level, ChunkPos pos, int newDust, DustReason reason) {
        int maxDust = COIConfig.MAX_DUST_LEVEL.get();
        int clamped = Math.max(0, Math.min(maxDust, newDust));
        int current = DustCacheManager.getDustLevel(pos);

        DustCacheManager.syncDustLevel(level, pos, clamped);
        logDustChange(pos, current, clamped, reason);
    }

    /**
     * Clears all dust from a chunk (sets to 0).
     */
    public static void clearDust(ServerLevel level, ChunkPos pos, DustReason reason) {
        int current = DustCacheManager.getDustLevel(pos);
        if (current > 0) {
            DustCacheManager.syncDustLevel(level, pos, 0);
            logDustChange(pos, current, 0, reason);
        }
    }

    // ==================== Internal ====================

    /**
     * Logs dust level changes when debug logging is enabled.
     */
    private static void logDustChange(ChunkPos pos, int oldLevel, int newLevel, DustReason reason) {
        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Chunk [{}, {}]: {} -> {} (reason: {})",
                    pos.x, pos.z, oldLevel, newLevel, reason.getId()
            );
        }
    }
}
