package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles periodic dust diffusion and natural decay for loaded chunks.
 * <p>
 * Diffusion algorithm:
 * <ol>
 *   <li>For each chunk with dust > 0, calculate pressure difference with each loaded neighbor</li>
 *   <li>Transfer a fraction of the difference (controlled by {@code diffusionRate})</li>
 *   <li>Apply a diffusion decay factor: neighbor only receives 80% of what source loses
 *       (20% is lost to simulate environmental absorption)</li>
 *   <li>After diffusion, apply natural decay to all chunks with dust > 0</li>
 * </ol>
 * <p>
 * All calculations are server-side only. Only processes in the Overworld.
 */
public class DustDiffusionEngine {

    /** Fraction of dust lost during transfer (neighbor receives 1 - this fraction) */
    private static final double DIFFUSION_LOSS_FACTOR = 0.2;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;
        if (!COIConfig.ENABLE_DUST_DIFFUSION.get()) return;
        if (serverLevel.getGameTime() % COIConfig.DIFFUSION_INTERVAL.get() != 0) return;

        handleDustDiffusion(serverLevel);
        handleDustDecay(serverLevel);
    }

    /**
     * Performs one tick of dust diffusion between loaded chunks.
     * Uses a snapshot-then-apply pattern to avoid concurrent modification.
     */
    private static void handleDustDiffusion(ServerLevel level) {
        if (DustCacheManager.isEmpty()) return;

        Map<ChunkPos, Integer> snapshot = DustCacheManager.getSnapshot();
        // Accumulates net dust changes: positive = gaining, negative = losing
        Map<ChunkPos, Integer> deltas = new HashMap<>();

        double diffusionRate = COIConfig.DIFFUSION_RATE.get();
        double receiveFactor = 1.0 - DIFFUSION_LOSS_FACTOR; // 0.8 — neighbor receives this fraction

        snapshot.forEach((pos, currentDust) -> {
            if (currentDust <= 0) return;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                ChunkPos neighborPos = new ChunkPos(pos.x + dir.getStepX(), pos.z + dir.getStepZ());
                if (!DustCacheManager.isChunkLoaded(neighborPos)) continue;

                int neighborDust = snapshot.getOrDefault(neighborPos, 0);
                int difference = currentDust - neighborDust;

                // Only diffuse from high to low, with a minimum threshold
                // to avoid low-level noise oscillation
                if (difference < 50) continue;

                // Transfer amount: fraction of difference, scaled by config rate
                // Divide by 128 for very gradual diffusion:
                //   5000 dust → ~39 per neighbor per cycle
                //   500 dust  → ~3 per neighbor per cycle
                int transfer = (int) (difference * diffusionRate / 128.0);
                if (transfer <= 0) continue;

                // Don't transfer more than what the source has
                transfer = Math.min(transfer, currentDust / 4);

                // Source loses full transfer amount
                deltas.merge(pos, -transfer, Integer::sum);
                // Neighbor receives less due to diffusion loss (absorption)
                int received = Math.max(1, (int) (transfer * receiveFactor));
                deltas.merge(neighborPos, received, Integer::sum);
            }
        });

        // Apply all deltas via the manager (handles clamping and persistence)
        int maxDust = COIConfig.MAX_DUST_LEVEL.get();
        deltas.forEach((pos, delta) -> {
            int current = DustCacheManager.getDustLevel(pos);
            int newLevel = Math.max(0, Math.min(maxDust, current + delta));
            if (newLevel != current) {
                DustCacheManager.syncDustLevel(level, pos, newLevel);
            }
        });

        if (COIConfig.ENABLE_DEBUG_LOGGING.get() && !deltas.isEmpty()) {
            CreateOriginiumIndustry.LOGGER.debug("[OriDust] Diffusion tick: {} chunks affected", deltas.size());
        }
    }

    /**
     * Applies natural dust decay to all cached chunks.
     * Decay rate is controlled by {@link COIConfig#DUST_DECAY_RATE}.
     * Decay is applied after diffusion each cycle.
     */
    private static void handleDustDecay(ServerLevel level) {
        int decayRate = COIConfig.DUST_DECAY_RATE.get();
        if (decayRate <= 0) return;

        Map<ChunkPos, Integer> snapshot = DustCacheManager.getSnapshot();
        snapshot.forEach((pos, dust) -> {
            if (dust > 0) {
                int newLevel = Math.max(0, dust - decayRate);
                if (newLevel != dust) {
                    DustCacheManager.syncDustLevel(level, pos, newLevel);
                }
            }
        });
    }
}
