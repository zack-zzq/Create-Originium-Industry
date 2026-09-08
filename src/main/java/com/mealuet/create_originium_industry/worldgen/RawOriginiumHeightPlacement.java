package com.mealuet.create_originium_industry.worldgen;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIWorldGen;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * Triangle height band from {@link COIConfig} minY/maxY.
 */
public final class RawOriginiumHeightPlacement extends PlacementModifier {

    public static final RawOriginiumHeightPlacement INSTANCE = new RawOriginiumHeightPlacement();
    public static final MapCodec<RawOriginiumHeightPlacement> CODEC = MapCodec.unit(INSTANCE);

    private RawOriginiumHeightPlacement() {}

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        if (!COIConfig.rawOriginiumOreEnabled()) {
            return Stream.empty();
        }
        int min = COIConfig.rawOriginiumMinY();
        int max = COIConfig.rawOriginiumMaxY();
        return HeightRangePlacement.triangle(
                VerticalAnchor.absolute(min),
                VerticalAnchor.absolute(max)
        ).getPositions(context, random, pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return COIWorldGen.RAW_ORIGINIUM_HEIGHT.get();
    }
}
