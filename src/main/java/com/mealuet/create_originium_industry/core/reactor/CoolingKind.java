package com.mealuet.create_originium_industry.core.reactor;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import net.minecraft.world.level.block.Block;

/**
 * Reactor cooling-chamber grade. The basin {@code originium_cooling_chamber}
 * is the normal snow-golem chamber; {@code originium_super_cooling_chamber}
 * is the super tier. Both share the cooling-chamber block entity type.
 */
public enum CoolingKind {
    NORMAL,
    SUPER;

    public double coolingFactor() {
        return this == SUPER
                ? COIConfig.reactorSuperChamberCooling()
                : COIConfig.reactorNormalChamberCooling();
    }

    public int durability() {
        return this == SUPER
                ? COIConfig.superCoolingChamberDurability()
                : COIConfig.coolingChamberDurability();
    }

    public static CoolingKind fromBlock(Block block) {
        if (block != null && block == COIBlocks.SUPER_COOLING_CHAMBER.get()) {
            return SUPER;
        }
        return NORMAL;
    }
}
