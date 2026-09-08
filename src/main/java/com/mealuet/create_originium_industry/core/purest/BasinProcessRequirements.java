package com.mealuet.create_originium_industry.core.purest;

import com.mealuet.create_originium_industry.block.CoolingChamberBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Runtime gates for the purest line: basin sieve, cooling-chamber attachment,
 * and "no active blaze heat" for supercooling. Callers go through
 * {@link com.mealuet.create_originium_industry.mixin.BasinOperatingBlockEntityMixin}.
 */
public final class BasinProcessRequirements {

    private BasinProcessRequirements() {}

    public static boolean matches(BasinBlockEntity basin, Recipe<?> recipe) {
        if (basin == null || recipe == null) {
            return true;
        }
        Level level = basin.getLevel();
        ResourceLocation id = DustProductionHelper.resolveRecipeId(
                level instanceof ServerLevel serverLevel ? serverLevel : null, recipe);
        return matches(level, basin.getBlockPos(), id);
    }

    public static boolean matches(Level level, BlockPos basinPos, ResourceLocation recipeId) {
        BasinProcessSpec spec = BasinProcessIndex.get(recipeId);
        if (!spec.hasRequirements()) {
            return true;
        }
        if (level == null || basinPos == null) {
            return false;
        }
        if (spec.requireSieve() && findActiveSieve(level, basinPos) == null) {
            return false;
        }
        if (spec.requireCoolingChamber() && findActiveChamber(level, basinPos) == null) {
            return false;
        }
        if (spec.rejectHeat() && isActivelyHeated(level, basinPos)) {
            return false;
        }
        return true;
    }

    /**
     * Wear the cooling chamber after a matching recipe actually applied.
     * Sieve durability is already consumed through dust capture.
     */
    public static void onApplied(ServerLevel level, BlockPos basinPos, Recipe<?> recipe) {
        if (level == null || basinPos == null || recipe == null) {
            return;
        }
        ResourceLocation id = DustProductionHelper.resolveRecipeId(level, recipe);
        BasinProcessSpec spec = BasinProcessIndex.get(id);
        if (spec.requireCoolingChamber()) {
            CoolingChamberBlockEntity chamber = findActiveChamber(level, basinPos);
            if (chamber != null) {
                chamber.consumeOperation();
            }
        }
    }

    public static boolean isActivelyHeated(Level level, BlockPos basinPos) {
        BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(
                level.getBlockState(basinPos.below()));
        return heat.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING);
    }

    public static ProcessSieveBlockEntity findActiveSieve(Level level, BlockPos basinPos) {
        for (BlockPos pos : aroundBasin(basinPos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ProcessSieveBlockEntity sieve && sieve.isPurifierActive()) {
                return sieve;
            }
        }
        return null;
    }

    public static CoolingChamberBlockEntity findActiveChamber(Level level, BlockPos basinPos) {
        for (BlockPos pos : aroundBasin(basinPos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CoolingChamberBlockEntity chamber && chamber.isChamberActive()) {
                return chamber;
            }
        }
        return null;
    }

    private static BlockPos[] aroundBasin(BlockPos basinPos) {
        return new BlockPos[] {
                basinPos,
                basinPos.relative(Direction.NORTH),
                basinPos.relative(Direction.SOUTH),
                basinPos.relative(Direction.WEST),
                basinPos.relative(Direction.EAST),
                basinPos.relative(Direction.UP),
                basinPos.relative(Direction.DOWN)
        };
    }
}
