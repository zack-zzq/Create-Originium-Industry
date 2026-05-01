package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

/**
 * BlockEntity type registrations for Create: Originium Industry.
 */
public class COIBlockEntityTypes {

    public static final BlockEntityEntry<DustFilterBlockEntity> DUST_FILTER = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_dust_filter", DustFilterBlockEntity::new)
            .validBlock(COIBlocks.DUST_FILTER)
            .register();

    public static void register() {
        // Force class loading to trigger static field initialization
    }
}
