package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustEmissionIndex;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.purest.BasinProcessIndex;
import com.mealuet.create_originium_industry.core.purest.BasinProcessSpec;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #43: captured {@code originium_dust} is recyclable
 * feedstock (heated mix back to shards), not only a sieve craft ingredient.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class DustRecyclingGameTests {

    private static final String RECIPE = "mixing/originium_dust_recycling";

    private DustRecyclingGameTests() {}

    @GameTest(template = "empty", batch = "dust_recycling")
    public static void recycleRecipeIsHeatedMixing(GameTestHelper helper) {
        Recipe<?> recycle = recipe(helper, RECIPE);
        helper.assertTrue(recycle instanceof ProcessingRecipe<?>, "Create processing recipe");
        helper.assertValueEqual(requiredHeat(recycle), HeatCondition.HEATED, "heated remelt, not superheat");
        helper.assertTrue(
                DustProductionHelper.containsOriginiumInput((ProcessingRecipe<?>) recycle),
                "consumes originium feedstock"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_recycling")
    public static void fourDustRecoverOneShard(GameTestHelper helper) {
        Recipe<?> recycle = recipe(helper, RECIPE);
        helper.assertValueEqual(dustIngredientSlots(recycle), 4, "four captured dust");
        helper.assertValueEqual(
                guaranteedItemCount(recycle, COIItems.ORIGINIUM_SHARD.get()),
                1,
                "one shard — recovery, not a 1:1 compact"
        );
        helper.assertValueEqual(
                guaranteedItemCount(recycle, COIItems.ORIGINIUM.get()),
                0,
                "does not skip to originium"
        );
        helper.assertValueEqual(
                guaranteedItemCount(recycle, COIItems.ORIGINIUM_DUST.get()),
                0,
                "no guaranteed dust item refund"
        );
        helper.assertFalse(hasFluidResult(recycle), "item recovery, not a molten shortcut");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_recycling")
    public static void recycleEmissionIsDatapackOnly(GameTestHelper helper) {
        ResourceLocation id = id(RECIPE);
        ServerLevel level = helper.getLevel();
        Recipe<?> recycle = recipe(helper, RECIPE);

        helper.assertValueEqual(DustEmissionIndex.getRecipeAmount(id), 80, "datapack amount");
        helper.assertValueEqual(DustProductionHelper.configOverride(id), -1, "not a frozen config override");
        helper.assertValueEqual(DustProductionHelper.mappedAmount(id), 80, "mapped uses datapack");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(level, recycle),
                80,
                "live recipe resolves the datapack amount"
        );
        helper.assertTrue(
                DustProductionHelper.getDustForRecipe(level, recycle)
                        < COIConfig.DUST_FROM_SHARD_MIXING.get(),
                "mapped 80 stays below the unmapped heated fallback (shard-mix 120)"
        );
        helper.assertTrue(
                80 < COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get(),
                "one recycle cannot mint a dust item even at 100% capture (80 < 100)"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_recycling")
    public static void recycleIsNotBasinGated(GameTestHelper helper) {
        BasinProcessSpec spec = BasinProcessIndex.get(id(RECIPE));
        helper.assertFalse(spec.hasRequirements(), "any heated mixer; no sieve/chamber gate");
        helper.succeed();
    }

    private static boolean hasFluidResult(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return false;
        }
        return !processing.getFluidResults().isEmpty();
    }

    private static int dustIngredientSlots(Recipe<?> recipe) {
        int slots = 0;
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.test(COIItems.ORIGINIUM_DUST.asStack())) {
                slots++;
            }
        }
        return slots;
    }

    private static int guaranteedItemCount(Recipe<?> recipe, Item item) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return 0;
        }
        int total = 0;
        for (var stack : processing.getRollableResultsAsItemStacks()) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
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
