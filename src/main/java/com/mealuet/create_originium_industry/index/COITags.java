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
