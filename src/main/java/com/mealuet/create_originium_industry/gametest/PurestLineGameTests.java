package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.CoolingChamberBlock;
import com.mealuet.create_originium_industry.block.CoolingChamberBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.ProcessAttachments;
import com.mealuet.create_originium_industry.core.purest.BasinProcessIndex;
import com.mealuet.create_originium_industry.core.purest.BasinProcessRequirements;
import com.mealuet.create_originium_industry.core.purest.BasinProcessSpec;
import com.mealuet.create_originium_industry.core.purest.MeltdownPolicy;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #5: molten → filter (basin sieve) → 培养液 culture →
 * supercooling (cooling chamber, no blaze heat) → {@code purest_originium}.
 * {@code purest_molten_originium} is the accident / late remelt fluid, not the
 * clean-route output. Meltdown dumps that fluid and dust; it never explodes.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class PurestLineGameTests {

    private PurestLineGameTests() {}

    @GameTest(template = "empty", batch = "purest_line")
    public static void survivalRecipesLoadWithExpectedHeat(GameTestHelper helper) {
        Recipe<?> filter = recipe(helper, "mixing/filtered_molten_originium");
        Recipe<?> culture = recipe(helper, "mixing/cultured_originium");
        Recipe<?> supercool = recipe(helper, "mixing/purest_originium_supercooling");
        Recipe<?> accident = recipe(helper, "mixing/purest_molten_accident");
        Recipe<?> remelt = recipe(helper, "mixing/purest_originium_melting");
        Recipe<?> recover = recipe(helper, "mixing/purest_molten_supercooling");
        Recipe<?> chamber = recipe(helper, "crafting/originium_cooling_chamber");

        helper.assertValueEqual(requiredHeat(filter), HeatCondition.NONE, "filter is unheated mixing");
        helper.assertValueEqual(requiredHeat(culture), HeatCondition.HEATED, "culture uses 培养液 + heat");
        helper.assertValueEqual(requiredHeat(supercool), HeatCondition.NONE, "supercool is unheated");
        helper.assertValueEqual(requiredHeat(accident), HeatCondition.SUPERHEATED, "accident is superheated");
        helper.assertValueEqual(requiredHeat(remelt), HeatCondition.SUPERHEATED, "remelt is superheated");
        helper.assertValueEqual(requiredHeat(recover), HeatCondition.NONE, "recover is unheated");
        helper.assertValueEqual(chamber.getType(), RecipeType.CRAFTING, "chamber is a crafting recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void recipeOutputsWalkMoltenToPurestItem(GameTestHelper helper) {
        helper.assertTrue(fluidResultIs(recipe(helper, "mixing/filtered_molten_originium"),
                COIFluids.FILTERED_MOLTEN_ORIGINIUM.getSource(), 100), "filter → filtered molten");
        helper.assertTrue(fluidResultIs(recipe(helper, "mixing/cultured_originium"),
                COIFluids.CULTURED_ORIGINIUM.getSource(), 100), "culture → cultured originium");
        helper.assertTrue(itemResultIs(recipe(helper, "mixing/purest_originium_supercooling"),
                COIItems.PUREST_ORIGINIUM.get()), "supercool → purest item");
        helper.assertTrue(fluidResultIs(recipe(helper, "mixing/purest_molten_accident"),
                COIFluids.PUREST_MOLTEN_ORIGINIUM.getSource(), 100), "accident → purest molten");
        helper.assertTrue(fluidResultIs(recipe(helper, "mixing/purest_originium_melting"),
                COIFluids.PUREST_MOLTEN_ORIGINIUM.getSource(), 100), "remelt → purest molten");
        helper.assertTrue(itemResultIs(recipe(helper, "mixing/purest_molten_supercooling"),
                COIItems.PUREST_ORIGINIUM.get()), "recover → purest item");
        helper.assertTrue(recipeUsesPackedIce(recipe(helper, "mixing/purest_originium_supercooling")),
                "clean supercool consumes packed ice");
        helper.assertTrue(recipeUsesBlueIce(recipe(helper, "mixing/purest_molten_supercooling")),
                "accident recovery consumes blue ice");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void dustCostsStayOnTheCleanRoute(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/filtered_molten_originium")),
                80, "filter dust");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/cultured_originium")),
                40, "culture dust");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_originium_supercooling")),
                30, "supercool dust (maintenance, not zero)");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_molten_accident")),
                250, "accident dumps more dust");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_originium_melting")),
                200, "remelt dust");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_molten_supercooling")),
                80, "recovery dust");
        helper.assertTrue(
                DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_molten_accident"))
                        > DustProductionHelper.getDustForRecipe(level, recipe(helper, "mixing/purest_originium_supercooling")),
                "accident is dirtier than the clean supercool");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void filterMixRequiresBasinSieve(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basinRel = new BlockPos(1, 1, 1);
        BlockPos sieveRel = new BlockPos(2, 1, 1);
        helper.setBlock(basinRel, AllBlocks.BASIN.getDefaultState());
        BlockPos basinPos = helper.absolutePos(basinRel);
        ResourceLocation filterId = id("mixing/filtered_molten_originium");

        helper.assertTrue(ProcessAttachments.isBasinSupport(level, basinPos), "basin is support");
        helper.assertFalse(
                BasinProcessRequirements.matches(level, basinPos, filterId),
                "no sieve → filter recipe blocked");

        helper.setBlock(sieveRel, COIBlocks.DUST_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));
        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "sieve BE");
        sieve.activatePurifierForGameTest();
        helper.assertTrue(
                BasinProcessRequirements.matches(level, basinPos, filterId),
                "active sieve unlocks filter mix");

        helper.assertTrue(
                BasinProcessRequirements.matches(level, basinPos, id("mixing/originium_mixing")),
                "existing melt recipe is not sieve-gated");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void supercoolingRequiresChamberAndRejectsHeat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basinRel = new BlockPos(1, 2, 1);
        BlockPos chamberRel = new BlockPos(2, 2, 1);
        BlockPos burnerRel = new BlockPos(1, 1, 1);
        helper.setBlock(basinRel, AllBlocks.BASIN.getDefaultState());
        BlockPos basinPos = helper.absolutePos(basinRel);
        ResourceLocation supercoolId = id("mixing/purest_originium_supercooling");
        ResourceLocation recoverId = id("mixing/purest_molten_supercooling");
        ResourceLocation accidentId = id("mixing/purest_molten_accident");

        helper.assertFalse(BasinProcessRequirements.matches(level, basinPos, supercoolId), "no chamber");
        helper.assertTrue(BasinProcessRequirements.matches(level, basinPos, accidentId), "accident has no chamber gate");

        helper.setBlock(chamberRel, COIBlocks.COOLING_CHAMBER.getDefaultState()
                .setValue(CoolingChamberBlock.FACING, Direction.EAST));
        CoolingChamberBlockEntity chamber = helper.getBlockEntity(chamberRel);
        helper.assertTrue(chamber != null, "chamber BE");
        chamber.activateForGameTest();
        helper.assertTrue(BasinProcessRequirements.matches(level, basinPos, supercoolId), "cold basin + chamber");
        helper.assertTrue(BasinProcessRequirements.matches(level, basinPos, recoverId), "recovery also needs chamber");
        helper.assertFalse(BasinProcessRequirements.isActivelyHeated(level, basinPos), "no burner yet");

        helper.setBlock(burnerRel, AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING));
        helper.assertTrue(BasinProcessRequirements.isActivelyHeated(level, basinPos), "seething is active heat");
        helper.assertFalse(BasinProcessRequirements.matches(level, basinPos, supercoolId), "heat blocks supercool");
        helper.assertFalse(BasinProcessRequirements.matches(level, basinPos, recoverId), "heat blocks recovery");
        helper.assertTrue(BasinProcessRequirements.matches(level, basinPos, accidentId), "accident still allowed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void coolingChamberWearsOnApplyAndConfigDefault(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.COOLING_CHAMBER_DURABILITY.get(), 250, "chamber durability default");
        helper.assertValueEqual(COIConfig.coolingChamberDurability(), 250, "helper");

        BlockPos basinRel = new BlockPos(1, 1, 1);
        BlockPos chamberRel = new BlockPos(2, 1, 1);
        helper.setBlock(basinRel, AllBlocks.BASIN.getDefaultState());
        helper.setBlock(chamberRel, COIBlocks.COOLING_CHAMBER.getDefaultState()
                .setValue(CoolingChamberBlock.FACING, Direction.EAST));
        CoolingChamberBlockEntity chamber = helper.getBlockEntity(chamberRel);
        helper.assertTrue(chamber != null, "chamber BE");
        chamber.activateForGameTest();
        int before = chamber.durability();
        BasinProcessRequirements.onApplied(
                helper.getLevel(), helper.absolutePos(basinRel), recipe(helper, "mixing/purest_originium_supercooling"));
        helper.assertValueEqual(chamber.durability(), before - 1, "one supercool wears one durability");
        helper.assertTrue(chamber.isChamberActive(), "still active");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void datapackSpecsAndMeltdownNeverExplodes(GameTestHelper helper) {
        BasinProcessSpec filter = BasinProcessIndex.get(id("mixing/filtered_molten_originium"));
        helper.assertTrue(filter.requireSieve(), "filter require_sieve");
        helper.assertFalse(filter.requireCoolingChamber(), "filter no chamber");
        helper.assertFalse(filter.rejectHeat(), "filter allows any heat");

        BasinProcessSpec supercool = BasinProcessIndex.get(id("mixing/purest_originium_supercooling"));
        helper.assertTrue(supercool.requireCoolingChamber(), "supercool chamber");
        helper.assertTrue(supercool.rejectHeat(), "supercool reject_heat");
        helper.assertFalse(supercool.requireSieve(), "supercool sieve is optional (dust capture still helps)");

        BasinProcessSpec none = BasinProcessIndex.get(id("mixing/catalyst_mixing"));
        helper.assertFalse(none.hasRequirements(), "培养液 recipe has no extra basin gate");

        helper.assertFalse(MeltdownPolicy.explodesBlocks(), "meltdown does not explode");
        helper.assertFalse(MeltdownPolicy.spawnsTnt(), "meltdown does not spawn TNT");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "purest_line")
    public static void coolingChamberCraftUsesAlloyAndBlueIce(GameTestHelper helper) {
        Recipe<?> chamber = recipe(helper, "crafting/originium_cooling_chamber");
        helper.assertTrue(chamber.getIngredients().stream().anyMatch(ing -> ing.test(COIItems.ORIGINIUM_ALLOY_INGOT.asStack())),
                "alloy ingot gate");
        helper.assertTrue(chamber.getIngredients().stream().anyMatch(ing -> ing.test(Items.BLUE_ICE.getDefaultInstance())),
                "blue ice gate");
        helper.succeed();
    }

    private static boolean fluidResultIs(Recipe<?> recipe, net.minecraft.world.level.material.Fluid fluid, int amount) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return false;
        }
        for (FluidStack stack : processing.getFluidResults()) {
            if (stack.getFluid() == fluid && stack.getAmount() == amount) {
                return true;
            }
        }
        return false;
    }

    private static boolean itemResultIs(Recipe<?> recipe, net.minecraft.world.item.Item item) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return false;
        }
        return processing.getRollableResultsAsItemStacks().stream().anyMatch(stack -> stack.is(item));
    }

    private static boolean recipeUsesPackedIce(Recipe<?> recipe) {
        return recipe.getIngredients().stream().anyMatch(ing -> ing.test(Items.PACKED_ICE.getDefaultInstance()));
    }

    private static boolean recipeUsesBlueIce(Recipe<?> recipe) {
        return recipe.getIngredients().stream().anyMatch(ing -> ing.test(Items.BLUE_ICE.getDefaultInstance()));
    }

    private static HeatCondition requiredHeat(Recipe<?> recipe) {
        if (recipe instanceof ProcessingRecipe<?> processing) {
            return processing.getRequiredHeat();
        }
        return HeatCondition.NONE;
    }

    private static Recipe<?> recipe(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(id(path));
        helper.assertTrue(holder.isPresent(), "recipe loaded: " + path);
        return holder.get().value();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }
}
