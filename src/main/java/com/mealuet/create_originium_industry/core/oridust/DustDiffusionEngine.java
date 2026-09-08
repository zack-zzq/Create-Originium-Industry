package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.perf.PerfProbe;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles periodic dust diffusion and natural decay for the <em>active set</em>.
 * <p>
 * SavedData can hold dust for unloaded / far-away logical chunks. Walking every
 * key every tick would be unbounded, so this engine only considers the active
 * set built by {@link DustCacheManager}:
 * <ul>
 *   <li>logical chunks within {@link COIConfig#INIT_CHUNK_RADIUS} of a player</li>
 *   <li>recently written keys plus their 4-neighbors</li>
 * </ul>
 * Diffusion only runs between neighbors that are both in that set.
 * <p>
 * Decay uses the same active set. Far pollution is left frozen until a player
 * comes near or the chunk is written again (machines, filters, death burst).
 *
 * <h3>Diffusion algorithm (unchanged):</h3>
 * <ol>
 *   <li>For each active chunk with dust &gt; 0, calculate pressure difference
 *       with each active neighbor</li>
 *   <li>Transfer a fraction of the difference (controlled by {@code diffusionRate})</li>
 *   <li>Apply a diffusion decay factor: neighbor receives
 *       {@code 1 - diffusionLossFactor} of what the source loses</li>
 *   <li>After diffusion, apply natural decay to active chunks with dust &gt; 0</li>
 * </ol>
 */
public class DustDiffusionEngine {

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != Level.OVERWORLD) return;
        if (!COIConfig.ENABLE_DUST_DIFFUSION.get()) return;
        if (serverLevel.getGameTime() % COIConfig.DIFFUSION_INTERVAL.get() != 0) return;

        runActiveSetCycle(serverLevel);
    }

    /**
     * One diffusion + decay pass over the current active set. Used by the
     * world tick, GameTests, and {@code /coi_debug perf}.
     *
     * @return active-set size after the snapshot
     */
    public static int runActiveSetCycle(ServerLevel level) {
        long start = System.nanoTime();
        Map<ChunkPos, Integer> snapshot = DustCacheManager.snapshotActive(level);
        handleDustDiffusion(level, snapshot);
        handleDustDecay(level, snapshot.keySet());
        PerfProbe.recordDustCycle(snapshot.size(), System.nanoTime() - start);
        return snapshot.size();
    }

    /**
     * Performs one tick of dust diffusion between active-set neighbors.
     * Uses a snapshot-then-apply pattern to avoid concurrent modification.
     */
    private static void handleDustDiffusion(ServerLevel level, Map<ChunkPos, Integer> snapshot) {
        if (snapshot.isEmpty()) return;

        Map<ChunkPos, Integer> deltas = new HashMap<>();

        boolean dedicated = COIConfig.isDedicated(level.getServer());
        double diffusionRate = COIConfig.effectiveDiffusionRate(dedicated);
        double receiveFactor = 1.0 - COIConfig.DIFFUSION_LOSS_FACTOR.get();
        int minDifference = COIConfig.DIFFUSION_MIN_DIFFERENCE.get();
        int transferDivisor = Math.max(1, COIConfig.DIFFUSION_TRANSFER_DIVISOR.get());

        snapshot.forEach((pos, currentDust) -> {
            if (currentDust <= 0) return;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                ChunkPos neighborPos = new ChunkPos(pos.x + dir.getStepX(), pos.z + dir.getStepZ());
                if (!snapshot.containsKey(neighborPos)) continue;

                int neighborDust = snapshot.getOrDefault(neighborPos, 0);
                int difference = currentDust - neighborDust;

                // Only diffuse from high to low, with a minimum threshold
                // to avoid low-level noise oscillation
                if (difference < minDifference) continue;

                // Transfer amount: fraction of difference, scaled by config rate
                // Divide by transferDivisor (default 128) for gradual diffusion:
                //   5000 dust → ~39 per neighbor per cycle
                //   500 dust  → ~3 per neighbor per cycle
                int transfer = (int) (difference * diffusionRate / (double) transferDivisor);
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

        int maxDust = COIConfig.MAX_DUST_LEVEL.get();
        deltas.forEach((pos, delta) -> {
            int current = OriDustSavedData.get(level).get(pos);
            int newLevel = Math.max(0, Math.min(maxDust, current + delta));
            if (newLevel != current) {
                OriginiumDustManager.applySimulated(level, pos, newLevel);
            }
        });

        if (COIConfig.ENABLE_DEBUG_LOGGING.get() && !deltas.isEmpty()) {
            CreateOriginiumIndustry.LOGGER.debug("[OriDust] Diffusion tick: {} chunks affected", deltas.size());
        }
    }

    /**
     * Applies natural dust decay to the active set only.
     * Inactive (far) pollution does not decay until it becomes active.
     * Re-reads SavedData so decay sees post-diffusion values.
     */
    private static void handleDustDecay(ServerLevel level, Set<ChunkPos> active) {
        int decayRate = COIConfig.DUST_DECAY_RATE.get();
        if (decayRate <= 0) return;

        OriDustSavedData data = OriDustSavedData.get(level);
        for (ChunkPos pos : active) {
            int dust = data.get(pos);
            if (dust > 0) {
                int newLevel = Math.max(0, dust - decayRate);
                if (newLevel != dust) {
                    OriginiumDustManager.applySimulated(level, pos, newLevel);
                }
            }
        }
    }
}
