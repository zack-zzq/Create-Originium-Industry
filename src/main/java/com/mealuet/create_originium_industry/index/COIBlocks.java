package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.CoolingChamberBlock;
import com.mealuet.create_originium_industry.block.DustFilterBlock;
import com.mealuet.create_originium_industry.block.DustMeterBlock;
import com.mealuet.create_originium_industry.block.DustNozzleBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * Block registrations for Create: Originium Industry.
 */
public class COIBlocks {

    public static final BlockEntry<DustFilterBlock> DUST_FILTER = CreateOriginiumIndustry.REGISTRATE
            .block("originium_dust_filter", DustFilterBlock::new)
            .properties(p -> p
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(),
                    prov.models().cubeAll(ctx.getName(), prov.modLoc("block/originium_dust_filter"))))
            .simpleItem()
            .register();

    /**
     * Process sieve attachment. Same registry path as the former placeholder item
     * so existing stacks stay {@code originium_dust_sieve}.
     */
    public static final BlockEntry<ProcessSieveBlock> DUST_SIEVE = CreateOriginiumIndustry.REGISTRATE
            .block("originium_dust_sieve", ProcessSieveBlock::new)
            .properties(p -> p
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    /**
     * Alloy filter upgrade. New id — not a rename of {@link #DUST_SIEVE}.
     * Same attachment behaviour; higher durability and capture.
     */
    public static final BlockEntry<ProcessSieveBlock> ALLOY_SIEVE = CreateOriginiumIndustry.REGISTRATE
            .block("originium_alloy_sieve", ProcessSieveBlock::new)
            .properties(p -> p
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    /**
     * Pollution-resistant Create-style casing. Adjacent faces seal process
     * emission ({@code pollution_resistant} / {@code reactor_housing}).
     */
    public static final BlockEntry<Block> ALLOY_CASING = CreateOriginiumIndustry.REGISTRATE
            .block("originium_alloy_casing", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
            .simpleItem()
            .register();

    /**
     * M3 reactor shell precursor. Denser housing than the casing; crafted from
     * casings plus a cooling chamber.
     */
    public static final BlockEntry<Block> CORE_HOUSING = CreateOriginiumIndustry.REGISTRATE
            .block("originium_core_housing", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(4.0f, 8.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
            .simpleItem()
            .register();

    /**
     * Encased Fan nozzle. Same registry path as the former placeholder item.
     */
    public static final BlockEntry<DustNozzleBlock> DUST_NOZZLE = CreateOriginiumIndustry.REGISTRATE
            .block("originium_dust_nozzle", DustNozzleBlock::new)
            .properties(p -> p
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    /**
     * Basin supercooling attachment. New id — not a rename of any frozen path.
     */
    public static final BlockEntry<CoolingChamberBlock> COOLING_CHAMBER = CreateOriginiumIndustry.REGISTRATE
            .block("originium_cooling_chamber", CoolingChamberBlock::new)
            .properties(p -> p
                    .strength(1.5f)
                    .sound(SoundType.COPPER)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    public static final BlockEntry<DustMeterBlock> DUST_METER = CreateOriginiumIndustry.REGISTRATE
            .block("originium_dust_meter", DustMeterBlock::new)
            .properties(p -> p
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    /**
     * Overworld stone ore. New id — does not replace the frozen {@code raw_originium} item.
     * Drops {@code raw_originium} (silk touch keeps the block).
     */
    public static final BlockEntry<Block> RAW_ORIGINIUM_ORE = CreateOriginiumIndustry.REGISTRATE
            .block("raw_originium_ore", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.STONE)
            )
            .simpleItem()
            .register();

    /**
     * Deepslate variant of {@link #RAW_ORIGINIUM_ORE}. Same drops, higher hardness.
     */
    public static final BlockEntry<Block> DEEPSLATE_RAW_ORIGINIUM_ORE = CreateOriginiumIndustry.REGISTRATE
            .block("deepslate_raw_originium_ore", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f)
                    .sound(SoundType.DEEPSLATE)
            )
            .simpleItem()
            .register();

    public static void register() {
        // Force class loading to trigger static field initialization
    }
}
