package com.mealuet.create_originium_industry.client.ponder;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Create 6 / Ponder 1.0 plugin. Scenes use {@code CreateSceneBuilder}
 * and {@code PonderIndex.addPlugin}, matching Create's own registration.
 */
public class COIPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateOriginiumIndustry.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> scenes =
                helper.withKeyFunction(entry -> entry.getId());

        scenes.forComponents(COIBlocks.DUST_METER, COIItems.RAW_ORIGINIUM, COIItems.ORIGINIUM_DUST, AllBlocks.MILLSTONE)
                .addStoryBoard(COIPonderKeys.DUST_GENERATION, DustScenes::generation, COIPonderTags.ORIGINIUM_INDUSTRY);

        scenes.forComponents(
                        COIBlocks.DUST_FILTER,
                        COIBlocks.DUST_SIEVE,
                        COIBlocks.ALLOY_SIEVE,
                        COIBlocks.DUST_NOZZLE)
                .addStoryBoard(COIPonderKeys.DUST_FILTER, DustScenes::filterRecovery, COIPonderTags.ORIGINIUM_INDUSTRY);

        scenes.forComponents(
                        COIBlocks.COOLING_CHAMBER,
                        COIBlocks.DUST_SIEVE,
                        COIItems.PUREST_ORIGINIUM)
                .addStoryBoard(COIPonderKeys.PUREST_SUPERCOOLING, PurestScenes::supercooling, COIPonderTags.ORIGINIUM_INDUSTRY);

        scenes.forComponents(
                        COIBlocks.POWER_CORE,
                        COIBlocks.SUPER_COOLING_CHAMBER,
                        COIBlocks.CORE_HOUSING,
                        COIBlocks.COOLING_CHAMBER)
                .addStoryBoard(COIPonderKeys.POWER_CORE, ReactorScenes::stability, COIPonderTags.ORIGINIUM_INDUSTRY);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(COIPonderTags.ORIGINIUM_INDUSTRY)
                .addToIndex()
                .item(COIBlocks.POWER_CORE.get(), true, false)
                .title(COIPonderKeys.copy(COIPonderKeys.TAG_TITLE))
                .description(COIPonderKeys.copy(COIPonderKeys.TAG_DESCRIPTION))
                .register();

        PonderTagRegistrationHelper<ItemProviderEntry<?, ?>> items =
                helper.withKeyFunction(entry -> entry.getId());
        items.addToTag(COIPonderTags.ORIGINIUM_INDUSTRY)
                .add(COIBlocks.DUST_FILTER)
                .add(COIBlocks.DUST_SIEVE)
                .add(COIBlocks.ALLOY_SIEVE)
                .add(COIBlocks.DUST_NOZZLE)
                .add(COIBlocks.DUST_METER)
                .add(COIBlocks.COOLING_CHAMBER)
                .add(COIBlocks.SUPER_COOLING_CHAMBER)
                .add(COIBlocks.POWER_CORE)
                .add(COIBlocks.CORE_HOUSING)
                .add(COIBlocks.ALLOY_CASING)
                .add(COIItems.RAW_ORIGINIUM)
                .add(COIItems.ORIGINIUM_DUST)
                .add(COIItems.PUREST_ORIGINIUM);

        items.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
                .add(COIBlocks.POWER_CORE);
        items.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(COIBlocks.DUST_FILTER);
    }
}
