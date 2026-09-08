package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.recipeviewer.BasinProcessDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.DustEmissionDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.ReactorInfoDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.RecipeViewerEntries;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Coverage for issue #45: recipe-viewer pages for basin-process gates,
 * dust-emission datapack entries, and reactor fuel/cooling. Asserts the
 * JEI-free data layer so GameTestServer does not need JEI installed.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class RecipeViewerGameTests {

    private RecipeViewerGameTests() {}

    @GameTest(template = "empty", batch = "recipe_viewer")
    public static void basinProcessPagesCoverDatapackGates(GameTestHelper helper) {
        List<BasinProcessDisplay> pages = RecipeViewerEntries.basinProcesses(recipes(helper));
        helper.assertValueEqual(pages.size(), 3, "three gated basin processes");

        BasinProcessDisplay filter = require(helper, pages, "mixing/filtered_molten_originium");
        helper.assertTrue(filter.spec().requireSieve(), "filter require_sieve");
        helper.assertFalse(filter.spec().requireCoolingChamber(), "filter no chamber");
        helper.assertFalse(filter.fluidInputs().isEmpty(), "filter shows molten input");
        helper.assertFalse(filter.fluidOutputs().isEmpty(), "filter shows filtered output");

        BasinProcessDisplay supercool = require(helper, pages, "mixing/purest_originium_supercooling");
        helper.assertTrue(supercool.spec().requireCoolingChamber(), "supercool chamber");
        helper.assertTrue(supercool.spec().rejectHeat(), "supercool reject_heat");
        helper.assertFalse(supercool.itemOutputs().isEmpty(), "supercool shows purest output");

        BasinProcessDisplay recover = require(helper, pages, "mixing/purest_molten_supercooling");
        helper.assertTrue(recover.spec().requireCoolingChamber(), "recovery chamber");
        helper.assertTrue(recover.spec().rejectHeat(), "recovery reject_heat");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipe_viewer")
    public static void dustEmissionPagesCoverDatapackEntries(GameTestHelper helper) {
        List<DustEmissionDisplay> pages = RecipeViewerEntries.dustEmissions(recipes(helper));
        helper.assertTrue(pages.size() >= 15, "recipe + tag emission entries");

        DustEmissionDisplay milling = requireDust(helper, pages, DustEmissionDisplay.Kind.RECIPE,
                "milling/raw_originium_milling");
        helper.assertValueEqual(milling.amount(), COIConfig.DUST_FROM_MILLING.get(), "milling uses config override");
        helper.assertFalse(milling.itemInputs().isEmpty(), "milling shows raw input");

        DustEmissionDisplay catalyst = requireDust(helper, pages, DustEmissionDisplay.Kind.RECIPE,
                "mixing/catalyst_mixing");
        helper.assertValueEqual(catalyst.amount(), 0, "catalyst is an explicit non-emitter");

        DustEmissionDisplay tag = requireDust(helper, pages, DustEmissionDisplay.Kind.ITEM_TAG,
                "dust_producing");
        helper.assertValueEqual(tag.amount(), 40, "dust_producing tag amount");
        helper.assertFalse(tag.itemInputs().isEmpty(), "tag enumerates as an ingredient");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipe_viewer")
    public static void reactorPagesSurfaceFuelAndCooling(GameTestHelper helper) {
        List<ReactorInfoDisplay> pages = RecipeViewerEntries.reactorInfo();
        helper.assertValueEqual(pages.size(), 3, "fuel + cooling + structure");

        ReactorInfoDisplay fuel = page(helper, pages, ReactorInfoDisplay.Kind.FUEL);
        helper.assertTrue(
                fuel.items().stream().anyMatch(stack -> stack.is(COIItems.PUREST_ORIGINIUM.get())),
                "fuel page includes purest originium"
        );
        helper.assertTrue(
                fuel.items().stream().anyMatch(stack -> stack.is(COIBlocks.POWER_CORE.get().asItem())),
                "fuel page includes power core"
        );
        helper.assertValueEqual(COIConfig.reactorFuelTicksPerItem(), 1200, "default fuel ticks for JEI copy");

        ReactorInfoDisplay cooling = page(helper, pages, ReactorInfoDisplay.Kind.COOLING);
        helper.assertTrue(
                cooling.fluids().stream().anyMatch(stack -> stack.getFluid().isSame(COIFluids.ORIGINIUM_COOLANT.getSource())),
                "cooling page includes originium coolant"
        );
        helper.assertTrue(
                cooling.fluids().stream().anyMatch(stack -> stack.getFluid().isSame(Fluids.WATER)),
                "cooling page includes water"
        );
        helper.assertTrue(
                cooling.fluids().stream().anyMatch(stack -> stack.getFluid().isSame(COIFluids.HOT_WATER.getSource())),
                "cooling page includes hot water"
        );

        ReactorInfoDisplay structure = page(helper, pages, ReactorInfoDisplay.Kind.STRUCTURE);
        helper.assertTrue(
                structure.items().stream().anyMatch(stack -> stack.is(COIBlocks.COOLING_CHAMBER.get().asItem())),
                "structure page includes cooling chamber"
        );
        helper.assertTrue(
                structure.items().stream().anyMatch(stack -> stack.is(COIBlocks.CORE_HOUSING.get().asItem())),
                "structure page includes housing"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipe_viewer")
    public static void jeiPluginShipsWithoutHardDependency(GameTestHelper helper) {
        helper.assertFalse(
                CreateOriginiumIndustry.class.getName().contains("compat.jei"),
                "main mod class is not the JEI plugin"
        );
        helper.assertTrue(
                RecipeViewerGameTests.class.getResource(
                        "/com/mealuet/create_originium_industry/compat/jei/COIJeiPlugin.class") != null,
                "JEI plugin class is compiled into the mod"
        );
        helper.succeed();
    }

    private static RecipeManager recipes(GameTestHelper helper) {
        return helper.getLevel().getRecipeManager();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }

    private static BasinProcessDisplay require(GameTestHelper helper, List<BasinProcessDisplay> pages, String path) {
        return pages.stream()
                .filter(page -> page.recipeId().equals(id(path)))
                .findFirst()
                .orElseGet(() -> {
                    helper.fail("missing basin process page: " + path);
                    return pages.getFirst();
                });
    }

    private static DustEmissionDisplay requireDust(
            GameTestHelper helper, List<DustEmissionDisplay> pages, DustEmissionDisplay.Kind kind, String path) {
        ResourceLocation expected = id(path);
        return pages.stream()
                .filter(page -> page.kind() == kind && page.id().equals(expected))
                .findFirst()
                .orElseGet(() -> {
                    helper.fail("missing dust emission page: " + kind + " " + path);
                    return pages.getFirst();
                });
    }

    private static ReactorInfoDisplay page(GameTestHelper helper, List<ReactorInfoDisplay> pages,
                                           ReactorInfoDisplay.Kind kind) {
        return pages.stream()
                .filter(page -> page.kind() == kind)
                .findFirst()
                .orElseGet(() -> {
                    helper.fail("missing reactor page: " + kind);
                    return pages.getFirst();
                });
    }
}
