package com.mealuet.create_originium_industry.compat.recipeviewer;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Synthetic power-core pages: fuel, coolant conversion, and housing/chambers.
 * Reactor simulation is not a datapack recipe type.
 */
public record ReactorInfoDisplay(
        Kind kind,
        List<ItemStack> items,
        List<FluidStack> fluids
) {
    public enum Kind {
        FUEL,
        COOLING,
        STRUCTURE
    }

    public static List<ReactorInfoDisplay> all() {
        int tank = Math.max(1, COIConfig.reactorTankCapacity());
        Fluid coolant = COIFluids.ORIGINIUM_COOLANT.getSource();
        Fluid hotWater = COIFluids.HOT_WATER.getSource();
        return List.of(
                new ReactorInfoDisplay(
                        Kind.FUEL,
                        List.of(
                                COIItems.PUREST_ORIGINIUM.asStack(),
                                COIBlocks.POWER_CORE.asStack()
                        ),
                        List.of()
                ),
                new ReactorInfoDisplay(
                        Kind.COOLING,
                        List.of(),
                        List.of(
                                new FluidStack(coolant, tank),
                                new FluidStack(Fluids.WATER, tank),
                                new FluidStack(hotWater, tank)
                        )
                ),
                new ReactorInfoDisplay(
                        Kind.STRUCTURE,
                        List.of(
                                COIBlocks.POWER_CORE.asStack(),
                                COIBlocks.CORE_HOUSING.asStack(),
                                COIBlocks.ALLOY_CASING.asStack(),
                                COIBlocks.COOLING_CHAMBER.asStack(),
                                COIBlocks.SUPER_COOLING_CHAMBER.asStack(),
                                new ItemStack(Items.BUCKET)
                        ),
                        List.of(new FluidStack(coolant, tank))
                )
        );
    }

    public String titleKey() {
        return switch (kind) {
            case FUEL -> "jei.create_originium_industry.reactor.fuel";
            case COOLING -> "jei.create_originium_industry.reactor.cooling";
            case STRUCTURE -> "jei.create_originium_industry.reactor.structure";
        };
    }

    public String bodyKey() {
        return titleKey() + ".body";
    }
}
