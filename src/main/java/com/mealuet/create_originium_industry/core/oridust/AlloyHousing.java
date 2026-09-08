package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pollution-resistant housing: originium alloy casings and core housing
 * adjacent to a processing machine cut gross emission before purifiers run.
 * No GUI — placing the blocks is the interaction.
 */
public final class AlloyHousing {

    private AlloyHousing() {}

    /**
     * Emission remaining after adjacent housing. Never increases {@code incoming}.
     */
    public static int reduceEmission(ServerLevel level, BlockPos pos, int incoming) {
        if (level == null || pos == null || incoming <= 0) {
            return Math.max(0, incoming);
        }
        double factor = 1.0 - reductionAt(level, pos);
        int sealed = (int) Math.round(incoming * factor);
        return Math.max(0, Math.min(incoming, sealed));
    }

    /**
     * Fraction of emission removed by adjacent housing, clamped to
     * {@code alloy_parts.housingMaxReduction}.
     */
    public static double reductionAt(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0.0;
        }
        double reduction = 0.0;
        for (Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            reduction += faceReduction(state);
        }
        double cap = COIConfig.HOUSING_MAX_REDUCTION.get();
        return Math.max(0.0, Math.min(cap, reduction));
    }

    public static double faceReduction(BlockState state) {
        if (state == null) {
            return 0.0;
        }
        // Core housing is the denser M3 shell; it is also in the casing tags.
        if (state.is(COIBlocks.CORE_HOUSING.get())) {
            return COIConfig.CORE_HOUSING_REDUCTION_PER_FACE.get();
        }
        if (state.is(COITags.Blocks.POLLUTION_RESISTANT) || state.is(COITags.Blocks.REACTOR_HOUSING)) {
            return COIConfig.CASING_HOUSING_REDUCTION_PER_FACE.get();
        }
        return 0.0;
    }
}
