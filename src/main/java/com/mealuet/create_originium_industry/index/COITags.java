package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Centralised tag key constants for Create: Originium Industry.
 */
public class COITags {

    public static class Items {
        /** All originium material items (raw, shard, crystal, dust, purest) */
        public static final TagKey<Item> ORIGINIUM_MATERIALS = tag("originium_materials");
        /** Items that produce dust when processed by Create machines */
        public static final TagKey<Item> DUST_PRODUCING = tag("dust_producing");
        /**
         * Protection equipment that reduces exposure/infection gain.
         * Shipped values: originium_respirator, originium_filter_canister,
         * originium_sealed_canister.
         */
        public static final TagKey<Item> ORIGINIUM_PROTECTION = tag("originium_protection");
        /**
         * Alloy-grade protection that adds {@code alloy_parts.sealedProtectionBonus}
         * on top of the piece-count formula.
         */
        public static final TagKey<Item> REINFORCED_PROTECTION = tag("originium_reinforced_protection");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, name));
        }
    }

    public static class Blocks {
        /** Blocks that act as sources of originium dust */
        public static final TagKey<Block> DUST_SOURCES = tag("dust_sources");
        /** Blocks that can filter/remove originium dust from a chunk */
        public static final TagKey<Block> DUST_FILTERS = tag("dust_filters");
        /** Stone / deepslate raw originium ore blocks */
        public static final TagKey<Block> RAW_ORIGINIUM_ORES = tag("raw_originium_ores");
        /**
         * Pollution-resistant factory housing. Adjacent faces reduce machine
         * emission ({@link com.mealuet.create_originium_industry.core.oridust.AlloyHousing}).
         */
        public static final TagKey<Block> POLLUTION_RESISTANT = tag("pollution_resistant");
        /**
         * M3 reactor shell contract. Alloy casing and core housing ship here so
         * the power core can require this tag without renaming ids.
         */
        public static final TagKey<Block> REACTOR_HOUSING = tag("reactor_housing");
        /**
         * Cooling chambers that contribute {@code M} when attached to a power core.
         */
        public static final TagKey<Block> REACTOR_COOLING = tag("reactor_cooling");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, name));
        }
    }

    public static class Fluids {
        /** All originium-based fluids */
        public static final TagKey<Fluid> ORIGINIUM_FLUIDS = tag("originium_fluids");

        private static TagKey<Fluid> tag(String name) {
            return TagKey.create(Registries.FLUID,
                    ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, name));
        }
    }

    private COITags() {}
}
