package com.mealuet.create_originium_industry.util;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public class FluidRegisterHelper {
    public static FluidEntry<BaseFlowingFluid.Flowing> createFluid(
            String name,
            int viscosity,
            int density,
            int levelDecreasePerBlock,
            int tickRate,
            int slopeFindDistance,
            float explosionResistance
    ) {
        FluidBuilder<BaseFlowingFluid.Flowing, CreateRegistrate> fluidBuilder = CreateOriginiumIndustry.REGISTRATE.fluid(
                        name,
                        ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, String.format("fluid/%s_still", name)),
                        ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, String.format("fluid/%s_flow", name)),
                        NoColorFluidAttributes::new
                )
                .properties(properties -> properties
                        .viscosity(viscosity)
                        .density(density))
                .fluidProperties(properties -> properties
                        .levelDecreasePerBlock(levelDecreasePerBlock)
                        .tickRate(tickRate)
                        .slopeFindDistance(slopeFindDistance)
                        .explosionResistance(explosionResistance))
                .source(BaseFlowingFluid.Flowing.Source::new);
        // FluidBuilder.create() already calls defaultBlock() — a LiquidBlock with
        // level=0..15 is registered on fluidBuilder.register(). Client assets live
        // under assets/create_originium_industry/blockstates|models/block/.

        fluidBuilder
                .bucket()
                .properties(properties -> properties.stacksTo(1))
                .register();

        return fluidBuilder.register();
    }

    public static FluidEntry<BaseFlowingFluid.Flowing> createDensityFluid(String name) {
        return createFluid(
                name,
                2200,
                1400,
                2,
                25,
                3,
                100f
        );
    }

    public static FluidEntry<BaseFlowingFluid.Flowing> createLightFluid(String name) {
        return createFluid(
                name,
                1500,
                1400,
                2,
                15,
                6,
                100f
        );
    }
}
