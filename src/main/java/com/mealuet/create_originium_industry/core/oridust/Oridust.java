package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.core.oridust.internal.DustCacheManager;
import com.mealuet.create_originium_industry.core.oridust.internal.DustDiffusionEngine;
import com.mealuet.create_originium_industry.core.oridust.internal.DustSyncTracker;
import com.mealuet.create_originium_industry.core.oridust.internal.PlayerDeathDustHandler;
import com.mealuet.create_originium_industry.core.oridust.internal.PlayerExposureHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;

/**
 * Game-bus registration and debug / GameTest entry points for the dust system.
 * <p>
 * Machine hooks, blocks, and commands should use {@link OriginiumDustManager},
 * {@link IOridustProducer}, {@link IDustPurifier}, and {@link PlayerExposure}
 * instead of this type.
 */
public final class Oridust {

    private Oridust() {}

    /**
     * Registers SavedData lifecycle, diffusion, client sync, emission datapack
     * reload, exposure ticks, and death-burst handlers on the NeoForge game bus.
     */
    public static void registerGameEvents(IEventBus gameBus) {
        gameBus.register(DustCacheManager.class);
        gameBus.register(DustDiffusionEngine.class);
        gameBus.register(DustSyncTracker.class);
        gameBus.register(DustEmissionIndex.class);
        gameBus.register(PlayerExposureHandler.class);
        gameBus.register(PlayerDeathDustHandler.class);
    }

    /**
     * One diffusion + decay pass over the current active set.
     *
     * @return active-set size after the snapshot
     */
    public static int runActiveSetCycle(ServerLevel level) {
        return DustDiffusionEngine.runActiveSetCycle(level);
    }

    /**
     * Immediately flush nearby dust + local exposure to clients.
     */
    public static void flushClientSync(ServerLevel overworld) {
        DustSyncTracker.flushNow(overworld);
    }

    /**
     * Merge a legacy {@code chunk_oridust_data} attachment into SavedData.
     */
    public static void migrateLoadedChunk(ServerLevel level, LevelChunk chunk) {
        DustCacheManager.migrateLoadedChunk(level, chunk);
    }

    /**
     * Drops a recent-write TTL entry without touching SavedData.
     */
    public static void unmarkWritten(ChunkPos pos) {
        DustCacheManager.unmarkWritten(pos);
    }

    public static int activeChunkCount() {
        return DustCacheManager.lastActiveCount();
    }

    public static int recentWriteCount() {
        return DustCacheManager.recentWriteCount();
    }
}
