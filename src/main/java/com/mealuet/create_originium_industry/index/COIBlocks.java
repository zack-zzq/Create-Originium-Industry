package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

    public static void register() {
        // Force class loading to trigger static field initialization
    }
}
