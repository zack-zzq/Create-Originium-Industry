package com.mealuet.create_originium_industry.worldgen;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIWorldGen;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

/**
 * Vein attempts per chunk from {@link COIConfig#rawOriginiumVeinsPerChunk()}.
 */
public final class RawOriginiumCountPlacement extends RepeatingPlacement {

    public static final RawOriginiumCountPlacement INSTANCE = new RawOriginiumCountPlacement();
    public static final MapCodec<RawOriginiumCountPlacement> CODEC = MapCodec.unit(INSTANCE);

    private RawOriginiumCountPlacement() {}

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        if (!COIConfig.rawOriginiumOreEnabled()) {
            return 0;
        }
        return COIConfig.rawOriginiumVeinsPerChunk();
    }

    @Override
    public PlacementModifierType<?> type() {
        return COIWorldGen.RAW_ORIGINIUM_COUNT.get();
    }
}
