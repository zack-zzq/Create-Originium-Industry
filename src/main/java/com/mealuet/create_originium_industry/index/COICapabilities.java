package com.mealuet.create_originium_industry.index;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Block capabilities. Power-core tanks accept pipes and buckets (no GUI).
 */
public final class COICapabilities {

    private COICapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                COIBlockEntityTypes.POWER_CORE.get(),
                (be, side) -> be.fluidHandler()
        );
    }
}
