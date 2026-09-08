package com.mealuet.create_originium_industry.core.purest;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Shared contract for originium accidents (M2 superheat miss, M3 reactor).
 * Meltdown / accident dumps chunk dust, remelts remaining {@code purest_originium}
 * into a world leak of {@code purest_molten_originium}, and converts leftover
 * tank fluids to hot water at the call site. It never explodes blocks or
 * spawns TNT.
 */
public final class MeltdownPolicy {

    private static final Direction[] LEAK_FACES = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN
    };

    private MeltdownPolicy() {}

    public static boolean explodesBlocks() {
        return false;
    }

    public static boolean spawnsTnt() {
        return false;
    }

    /**
     * How many source blocks of {@code purest_molten_originium} a meltdown
     * should try to place. Remaining fuel remelts into the leak (at least one
     * source whenever a dump is requested); {@code meltdownMoltenSources} caps
     * the spray. Zero cap disables the world leak.
     */
    public static int moltenSourceCount(int remainingFuel) {
        int cap = Math.max(0, COIConfig.reactorMeltdownMoltenSources());
        if (cap <= 0) {
            return 0;
        }
        return Math.min(cap, Math.max(1, Math.max(0, remainingFuel)));
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

    /**
     * World-side accident leak: place {@code purest_molten_originium} source
     * blocks in replaceable neighbours. Never replaces the origin block, never
     * calls {@link Level#explode}, never spawns {@link PrimedTnt}.
     *
     * @return number of source blocks actually placed
     */
    public static int leakMolten(ServerLevel level, BlockPos origin, int remainingFuel) {
        if (level == null || origin == null) {
            return 0;
        }
        if (explodesBlocks() || spawnsTnt()) {
            return 0;
        }
        int want = moltenSourceCount(remainingFuel);
        if (want <= 0) {
            return 0;
        }
        Fluid fluid = COIFluids.PUREST_MOLTEN_ORIGINIUM.getSource();
        BlockState source = fluid.defaultFluidState().createLegacyBlock();
        int placed = 0;
        for (Direction direction : LEAK_FACES) {
            if (placed >= want) {
                break;
            }
            if (tryPlaceSource(level, origin.relative(direction), source)) {
                placed++;
            }
        }
        if (placed < want) {
            placed += fillRing(level, origin, source, want - placed);
        }
        return placed;
    }

    private static int fillRing(ServerLevel level, BlockPos origin, BlockState source, int remaining) {
        int placed = 0;
        for (int radius = 2; radius <= 3 && placed < remaining; radius++) {
            for (int dy = 0; dy <= 1 && placed < remaining; dy++) {
                for (int dx = -radius; dx <= radius && placed < remaining; dx++) {
                    for (int dz = -radius; dz <= radius && placed < remaining; dz++) {
                        if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                            continue;
                        }
                        if (tryPlaceSource(level, origin.offset(dx, dy, dz), source)) {
                            placed++;
                        }
                    }
                }
            }
        }
        return placed;
    }

    private static boolean tryPlaceSource(ServerLevel level, BlockPos pos, BlockState source) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState current = level.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced()) {
            return false;
        }
        return level.setBlock(pos, source, Block.UPDATE_ALL);
    }
}
