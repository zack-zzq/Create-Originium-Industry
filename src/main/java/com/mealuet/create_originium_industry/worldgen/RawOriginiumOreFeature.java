package com.mealuet.create_originium_industry.worldgen;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIWorldGen;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.Optional;

/**
 * Vanilla {@link Feature#ORE} with vein size / air-discard taken from {@link COIConfig}.
 */
public final class RawOriginiumOreFeature extends Feature<NoneFeatureConfiguration> {

    public RawOriginiumOreFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!COIConfig.rawOriginiumOreEnabled()) {
            return false;
        }
        OreConfiguration config = COIWorldGen.oreConfiguration();
        return Feature.ORE.place(new FeaturePlaceContext<>(
                Optional.empty(),
                context.level(),
                context.chunkGenerator(),
                context.random(),
                context.origin(),
                config
        ));
    }
}
