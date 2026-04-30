package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles periodic dust diffusion between loaded chunks.
 * Dust flows from high-concentration chunks to low-concentration neighbors.
 */
public class DustDiffusionEngine {

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == Level.OVERWORLD) {
            if (serverLevel.getGameTime() % COIConfig.DIFFUSION_INTERVAL.get() == 0) {
                handleDustDiffusion(serverLevel);
            }
        }
    }

    private static void handleDustDiffusion(ServerLevel level) {
        if (DustCacheManager.isEmpty()) return;

        Map<ChunkPos, Integer> snapshot = DustCacheManager.getSnapshot();
        Map<ChunkPos, Integer> pendingUpdates = new ConcurrentHashMap<>();

        // 1. Calculate diffusion deltas based on current snapshot
        snapshot.forEach((pos, currentDust) -> {
            if (currentDust <= 0) return;

            int neighborTotalDust = 0;
            int neighborCount = 0;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                ChunkPos neighborPos = new ChunkPos(pos.x + dir.getStepX(), pos.z + dir.getStepZ());
                if (DustCacheManager.isChunkLoaded(neighborPos)) {
                    neighborTotalDust += snapshot.getOrDefault(neighborPos, 0);
                    neighborCount++;
                }
            }

            if (neighborCount > 0) {
                int totalSystemDust = currentDust + neighborTotalDust;
                int averageDust = totalSystemDust / (neighborCount + 1);
                int dustToMovePerNeighbor = Math.max(0, (currentDust - averageDust) / (neighborCount * 2 + 2));

                int totalDustLeaving = 0;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    ChunkPos neighborPos = new ChunkPos(pos.x + dir.getStepX(), pos.z + dir.getStepZ());
                    if (DustCacheManager.isChunkLoaded(neighborPos)) {
                        int neighborDust = snapshot.getOrDefault(neighborPos, 0);
                        if (currentDust > neighborDust && dustToMovePerNeighbor > 0) {
                            pendingUpdates.merge(neighborPos, dustToMovePerNeighbor, Integer::sum);
                            totalDustLeaving += dustToMovePerNeighbor;
                        }
                    }
                }
                if (totalDustLeaving > 0) {
                    pendingUpdates.merge(pos, -totalDustLeaving, Integer::sum);
                }
            }
        });

        // 2. Apply updates via DustCacheManager (syncs both cache and persistent data)
        pendingUpdates.forEach((pos, dustChange) -> {
            int originalLevel = DustCacheManager.getDustLevel(pos);
            int newLevel = Math.max(0, originalLevel + dustChange);
            DustCacheManager.syncDustLevel(level, pos, newLevel);
        });
    }
}
