package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.internal.ClientDustCache;
import com.mealuet.create_originium_industry.core.oridust.internal.ClientExposureCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Side-aware read API for dust meter goggles, debug overlay, and tooltips.
 * <p>
 * Server: Overworld {@link OriDustSavedData} / player attachments.<br>
 * Client: last nearby sync packet / local-player exposure packet.
 */
public final class VisibleDust {

    private VisibleDust() {}

    public static int chunkDust(Level level, ChunkPos pos) {
        if (level instanceof ServerLevel serverLevel && !level.isClientSide) {
            return OriginiumDustManager.getDust(serverLevel, pos);
        }
        return ClientDustCache.get(pos);
    }

    public static int chunkDustAt(Level level, BlockPos pos) {
        return chunkDust(level, WorldSpace.toDustChunk(level, pos));
    }

    public static int chunkDustAt(Player player) {
        return chunkDust(player.level(), WorldSpace.toDustChunk(player));
    }

    /**
     * Client goggles: use the synced cache when this chunk has been in a
     * packet, otherwise the block-entity snapshot from {@code notifyUpdate}.
     */
    public static int chunkDustOrFallback(Level level, ChunkPos pos, int fallback) {
        if (level instanceof ServerLevel serverLevel && !level.isClientSide) {
            return OriginiumDustManager.getDust(serverLevel, pos);
        }
        return ClientDustCache.getOrFallback(pos, fallback);
    }

    public static DustLevel risk(Level level, ChunkPos pos) {
        return DustLevel.fromDust(chunkDust(level, pos));
    }

    public static int exposure(Player player) {
        if (!player.level().isClientSide) {
            return PlayerExposure.getExposure(player);
        }
        return ClientExposureCache.exposure();
    }

    public static int infection(Player player) {
        if (!player.level().isClientSide) {
            return PlayerExposure.getInfection(player);
        }
        return ClientExposureCache.infection();
    }

    public static InfectionStage infectionStage(Player player) {
        return InfectionStage.fromInfection(infection(player));
    }

    /**
     * {@code true} when the common spec is loaded and
     * {@code multiplayer.syncDustToClients} is on (default after #17).
     */
    public static boolean dustSyncEnabled() {
        return COIConfig.syncDustToClients();
    }

    /**
     * Apply a nearby dust sync packet to the local client cache.
     */
    public static void applyClientDust(long[] keys, int[] values) {
        ClientDustCache.apply(keys, values);
    }

    /**
     * Apply a local-player exposure sync packet to the client cache.
     */
    public static void applyClientExposure(int exposure, int infection) {
        ClientExposureCache.apply(exposure, infection);
    }

    /**
     * Drop client dust / exposure caches (logout or GameTests).
     */
    public static void clearClientCaches() {
        ClientDustCache.clear();
        ClientExposureCache.clear();
    }
}
