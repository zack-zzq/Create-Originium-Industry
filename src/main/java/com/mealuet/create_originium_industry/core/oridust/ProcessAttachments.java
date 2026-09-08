package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.index.COITags;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlock;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.simibubi.create.content.processing.basin.BasinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

/**
 * Blocks a process sieve may attach to: Create Basin / mill / mixer / crush,
 * plus datapack {@code dust_sources}.
 * The cooling chamber is Basin-only ({@link #isBasinSupport}).
 */
public final class ProcessAttachments {

    private ProcessAttachments() {}

    public static boolean isBasinSupport(LevelReader level, BlockPos pos) {
        return level != null && pos != null && BasinBlock.isBasin(level, pos);
    }

    public static boolean isSupport(LevelReader level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        if (isBasinSupport(level, pos)) {
            return true;
        }
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof MillstoneBlock
                || block instanceof MechanicalMixerBlock
                || block instanceof CrushingWheelControllerBlock) {
            return true;
        }
        return level.getBlockState(pos).is(COITags.Blocks.DUST_SOURCES);
    }
}
