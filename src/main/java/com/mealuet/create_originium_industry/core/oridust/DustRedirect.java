package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Conserved chunk-to-chunk dust movement used by the Encased Fan nozzle.
 * Dust is never voided: anything removed from the source is written to the
 * destination (clamped by {@code maxDustLevel} headroom).
 */
public final class DustRedirect {

    public record Transfer(int moved, ChunkPos from, ChunkPos to) {}

    private DustRedirect() {}

    /**
     * Move up to {@code requested} dust from {@code fromPos}'s logical chunk
     * into the neighbouring chunk in {@code flow}. Vertical flow is split
     * across the four horizontal neighbours (dilution).
     *
     * @return total dust units moved
     */
    public static int redirect(ServerLevel level, BlockPos fromPos, Direction flow, int requested) {
        if (level == null || fromPos == null || flow == null || requested <= 0) {
            return 0;
        }
        if (flow.getAxis().isVertical()) {
            int each = requested / 4;
            if (each <= 0) {
                return 0;
            }
            int total = 0;
            for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                total += moveToNeighbour(level, fromPos, horizontal, each).moved();
            }
            return total;
        }
        return moveToNeighbour(level, fromPos, flow, requested).moved();
    }

    public static Transfer moveToNeighbour(ServerLevel level, BlockPos fromPos, Direction flow, int requested) {
        ChunkPos from = WorldSpace.toDustChunk(level, fromPos);
        ChunkPos to = neighbourChunk(from, flow);
        if (from.equals(to) || requested <= 0) {
            return new Transfer(0, from, to);
        }
        int available = OriginiumDustManager.getDust(level, from);
        int dest = OriginiumDustManager.getDust(level, to);
        int room = Math.max(0, COIConfig.MAX_DUST_LEVEL.get() - dest);
        int moved = Math.min(requested, Math.min(available, room));
        if (moved <= 0) {
            return new Transfer(0, from, to);
        }
        OriginiumDustManager.addDust(level, from, -moved, DustReason.DIFFUSER);
        OriginiumDustManager.addDust(level, to, moved, DustReason.DIFFUSER);
        return new Transfer(moved, from, to);
    }

    public static ChunkPos neighbourChunk(ChunkPos from, Direction flow) {
        if (from == null || flow == null || flow.getAxis().isVertical()) {
            return from;
        }
        return new ChunkPos(from.x + flow.getStepX(), from.z + flow.getStepZ());
    }
}
