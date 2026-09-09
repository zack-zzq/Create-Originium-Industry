package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.internal.DustCacheManager;
import com.mealuet.create_originium_industry.core.oridust.internal.DustSyncTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * Central public API for the originium dust system.
 * <p>
 * All external code (commands, machine hooks, reactor, filters) should use this
 * manager instead of {@link OriDustSavedData} or
 * {@code core.oridust.internal} tick engines. This ensures consistent logging,
 * clamping, logical WorldSpace keys, and a single SavedData store.
 *
 * <h3>Usage examples:</h3>
 * <pre>
 * OriginiumDustManager.addDust(serverLevel, chunkPos, 100, DustReason.MACHINE_PROCESSING);
 * int dust = OriginiumDustManager.getDust(serverLevel, chunkPos);
 * OriginiumDustManager.clearDust(serverLevel, chunkPos, DustReason.DEBUG);
 * </pre>
 *
 * Call sites that start from a {@link BlockPos} or entity must convert through
 * {@link WorldSpace} so Sable sublevels deposit dust at the logical overworld
 * location. Overloads {@link #addDustAt} / {@link #getDustAt} do that for you.
 */
public final class OriginiumDustManager {

    private OriginiumDustManager() {}

    // ==================== Query ====================

    /**
     * Gets the dust level for a logical chunk. Reads Overworld SavedData and
     * does not require the chunk to be loaded.
     */
    public static int getDust(ServerLevel level, ChunkPos pos) {
        return OriDustSavedData.get(level).get(pos);
    }

    /**
     * Gets the dust level for a logical chunk using the running server's
     * Overworld store. Returns 0 if no server is available.
     */
    public static int getDust(ChunkPos pos) {
        return DustCacheManager.getDustLevel(pos);
    }

    /**
     * Gets dust at a world position after {@link WorldSpace} remapping.
     */
    public static int getDustAt(ServerLevel level, BlockPos pos) {
        return getDust(level, WorldSpace.toDustChunk(level, pos));
    }

    /**
     * Gets dust at an entity's logical position after {@link WorldSpace} remapping.
     */
    public static int getDustAt(Entity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return 0;
        return getDust(serverLevel, WorldSpace.toDustChunk(entity));
    }

    /**
     * Returns the classified risk level for a chunk.
     */
    public static DustLevel getDustLevel(ChunkPos pos) {
        return DustLevel.fromDust(getDust(pos));
    }

    /**
     * Checks if a chunk is currently in the diffusion active set.
     */
    public static boolean isTracked(ChunkPos pos) {
        return DustCacheManager.isChunkLoaded(pos);
    }

    // ==================== Mutation ====================

    /**
     * Adds dust to a logical chunk. Amount is clamped to [0, maxDustLevel].
     * Persists via Overworld SavedData even if the chunk is unloaded.
     *
     * @param level  the server level (Overworld SavedData is used regardless)
     * @param pos    the <em>logical</em> chunk position
     * @param amount amount of dust to add (can be negative to remove)
     * @param reason why the dust is being added
     */
    public static void addDust(ServerLevel level, ChunkPos pos, int amount, DustReason reason) {
        int current = OriDustSavedData.get(level).get(pos);
        int newLevel = OriDustSavedData.get(level).add(pos, amount);

        if (newLevel != current) {
            DustCacheManager.markWritten(level, pos);
            DustSyncTracker.markDustDirty(pos);
            logDustChange(pos, current, newLevel, reason);
        }
    }

    /**
     * {@link WorldSpace}-aware add at a block position.
     */
    public static void addDustAt(ServerLevel level, BlockPos pos, int amount, DustReason reason) {
        addDust(level, WorldSpace.toDustChunk(level, pos), amount, reason);
    }

    /**
     * {@link WorldSpace}-aware add at an arbitrary world position.
     */
    public static void addDustAt(ServerLevel level, Vec3 pos, int amount, DustReason reason) {
        addDust(level, WorldSpace.toDustChunk(level, pos), amount, reason);
    }

    /**
     * {@link WorldSpace}-aware add at an entity's position.
     */
    public static void addDustAt(Entity entity, int amount, DustReason reason) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        addDust(serverLevel, WorldSpace.toDustChunk(entity), amount, reason);
    }

    /**
     * Sets the dust level for a logical chunk directly. Clamped to [0, maxDustLevel].
     */
    public static void setDust(ServerLevel level, ChunkPos pos, int newDust, DustReason reason) {
        int current = OriDustSavedData.get(level).get(pos);
        int clamped = OriDustSavedData.get(level).set(pos, newDust);
        DustCacheManager.markWritten(level, pos);
        if (clamped != current) {
            DustSyncTracker.markDustDirty(pos);
        }
        logDustChange(pos, current, clamped, reason);
    }

    /**
     * {@link WorldSpace}-aware set at a block position.
     */
    public static void setDustAt(ServerLevel level, BlockPos pos, int newDust, DustReason reason) {
        setDust(level, WorldSpace.toDustChunk(level, pos), newDust, reason);
    }

    /**
     * Clears all dust from a logical chunk (sets to 0).
     */
    public static void clearDust(ServerLevel level, ChunkPos pos, DustReason reason) {
        int current = OriDustSavedData.get(level).get(pos);
        if (current > 0) {
            OriDustSavedData.get(level).set(pos, 0);
            DustCacheManager.markWritten(level, pos);
            DustSyncTracker.markDustDirty(pos);
            logDustChange(pos, current, 0, reason);
        }
    }

    /**
     * Applies a clamped dust value without marking the chunk as recently written.
     * Used by diffusion/decay so the active set cannot walk across the whole map.
     * Machine hooks and commands should use {@link #addDust} / {@link #setDust}.
     */
    public static void applySimulated(ServerLevel level, ChunkPos pos, int newDust) {
        int current = OriDustSavedData.get(level).get(pos);
        int clamped = OriDustSavedData.get(level).set(pos, newDust);
        if (clamped != current) {
            DustSyncTracker.markDustDirty(pos);
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
