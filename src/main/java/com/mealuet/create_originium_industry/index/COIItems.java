package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.item.*;
import com.tterrag.registrate.util.entry.ItemEntry;
public class COIItems {
    public static final ItemEntry<OriginiumAlloyIngotItem> ORIGINIUM_ALLOY_INGOT = CreateOriginiumIndustry.REGISTRATE.item("originium_alloy_ingot", OriginiumAlloyIngotItem::new)
            .register();
    public static final ItemEntry<OriginiumItem> ORIGINIUM = CreateOriginiumIndustry.REGISTRATE.item("originium", OriginiumItem::new)
            .register();
    public static final ItemEntry<OriginiumShardItem> ORIGINIUM_SHARD = CreateOriginiumIndustry.REGISTRATE.item("originium_shard", OriginiumShardItem::new)
            .register();
    public static final ItemEntry<PurestOriginiumItem> PUREST_ORIGINIUM = CreateOriginiumIndustry.REGISTRATE.item("purest_originium", PurestOriginiumItem::new)
            .register();
    public static final ItemEntry<RawOriginiumItem> RAW_ORIGINIUM = CreateOriginiumIndustry.REGISTRATE.item("raw_originium", RawOriginiumItem::new)
            .register();
    public static final ItemEntry<OriginiumDustItem> ORIGINIUM_DUST = CreateOriginiumIndustry.REGISTRATE.item("originium_dust", OriginiumDustItem::new)
            .register();
    public static final ItemEntry<OriginiumDebugWandItem> ORIGINIUM_DEBUG_WAND = CreateOriginiumIndustry.REGISTRATE.item("originium_debug_wand", OriginiumDebugWandItem::new)
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/originium_dust")))
            .register();
    public static final ItemEntry<OriginiumProtectionItem> ORIGINIUM_RESPIRATOR =
            CreateOriginiumIndustry.REGISTRATE.item("originium_respirator",
                            p -> new OriginiumProtectionItem(p, net.minecraft.world.entity.EquipmentSlot.HEAD))
                    .register();
    public static final ItemEntry<OriginiumProtectionItem> ORIGINIUM_FILTER_CANISTER =
            CreateOriginiumIndustry.REGISTRATE.item("originium_filter_canister",
                            p -> new OriginiumProtectionItem(p, net.minecraft.world.entity.EquipmentSlot.CHEST))
                    .register();

    public static void register() {}
}
