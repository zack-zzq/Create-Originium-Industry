package com.mealuet.create_originium_industry.core.purest;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;

/**
 * Shared contract for originium accidents (M2 superheat miss, M3 reactor).
 * Meltdown / accident dumps chunk dust (and consumes remaining purest fuel
 * at the call site) back into the factory loop. It never explodes blocks
 * or spawns TNT.
 */
public final class MeltdownPolicy {

    private MeltdownPolicy() {}

    public static boolean explodesBlocks() {
        return false;
    }

    public static boolean spawnsTnt() {
        return false;
    }

    /**
     * Dust pressure into the chunk. Never calls {@link Level#explode} and
     * never spawns {@link PrimedTnt}, regardless of feature toggles.
     */
    public static int dumpDust(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        if (explodesBlocks() || spawnsTnt()) {
            return 0;
        }
        int burst = Math.max(0, COIConfig.reactorMeltdownDustBurst());
        if (burst > 0) {
            OriginiumDustManager.addDustAt(level, pos, burst, DustReason.REACTOR);
        }
        return burst;
    }
}
