package com.mealuet.create_originium_industry.core.perf;

import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.Oridust;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Repeatable 100-machine dust load: a 10×10 grid of recently written
 * chunks with a checkerboard concentration so diffusion actually transfers.
 */
public final class PerfLoad {

    public static final int GRID = 10;
    public static final int HIGH_DUST = 4000;
    public static final int LOW_DUST = 800;

    private PerfLoad() {}

    public static List<ChunkPos> machineChunks(ChunkPos center) {
        List<ChunkPos> chunks = new ArrayList<>(PerfProbe.STATED_MACHINE_COUNT);
        int originX = center.x - (GRID / 2);
        int originZ = center.z - (GRID / 2);
        for (int z = 0; z < GRID; z++) {
            for (int x = 0; x < GRID; x++) {
                chunks.add(new ChunkPos(originX + x, originZ + z));
            }
        }
        return chunks;
    }

    public static int seedMachineDust(ServerLevel level, ChunkPos center) {
        List<ChunkPos> chunks = machineChunks(center);
        for (int i = 0; i < chunks.size(); i++) {
            int dust = (i & 1) == 0 ? HIGH_DUST : LOW_DUST;
            OriginiumDustManager.setDust(level, chunks.get(i), dust, DustReason.MACHINE_PROCESSING);
        }
        return chunks.size();
    }

    public static void clearMachineDust(ServerLevel level, ChunkPos center) {
        for (ChunkPos pos : machineChunks(center)) {
            OriginiumDustManager.clearDust(level, pos, DustReason.DEBUG);
            Oridust.unmarkWritten(pos);
        }
    }
}
