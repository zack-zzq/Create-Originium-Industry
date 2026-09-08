package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.CoolingChamberBlockEntity;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.block.DustMeterBlockEntity;
import com.mealuet.create_originium_industry.block.DustNozzleBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

/**
 * BlockEntity type registrations for Create: Originium Industry.
 */
public class COIBlockEntityTypes {

    public static final BlockEntityEntry<DustFilterBlockEntity> DUST_FILTER = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_dust_filter", DustFilterBlockEntity::new)
            .validBlock(COIBlocks.DUST_FILTER)
            .register();

    public static final BlockEntityEntry<ProcessSieveBlockEntity> PROCESS_SIEVE = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_dust_sieve", ProcessSieveBlockEntity::new)
            .validBlocks(COIBlocks.DUST_SIEVE, COIBlocks.ALLOY_SIEVE)
            .register();

    public static final BlockEntityEntry<DustNozzleBlockEntity> DUST_NOZZLE = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_dust_nozzle", DustNozzleBlockEntity::new)
            .validBlock(COIBlocks.DUST_NOZZLE)
            .register();

    public static final BlockEntityEntry<CoolingChamberBlockEntity> COOLING_CHAMBER = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_cooling_chamber", CoolingChamberBlockEntity::new)
            .validBlock(COIBlocks.COOLING_CHAMBER)
            .register();

    public static final BlockEntityEntry<DustMeterBlockEntity> DUST_METER = CreateOriginiumIndustry.REGISTRATE
            .blockEntity("originium_dust_meter", DustMeterBlockEntity::new)
            .validBlock(COIBlocks.DUST_METER)
            .register();

    public static void register() {
        // Force class loading to trigger static field initialization
    }
}
