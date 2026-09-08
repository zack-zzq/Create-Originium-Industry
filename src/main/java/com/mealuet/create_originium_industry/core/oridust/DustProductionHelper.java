package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COITags;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.IdentityHashMap;
import java.util.List;

/**
 * Recipe/item-aware facade over {@link IOridustProducer}.
 * <p>
 * Amounts resolve as:
 * <ol>
 *   <li>Frozen recipe ids — {@link COIConfig} {@code dust_production.*} overrides
 *       (existing server.toml keys keep working).</li>
 *   <li>Datapack {@code coi_dust_emission} recipe entry (including explicit {@code 0}).</li>
 *   <li>Heat-aware fallback when the recipe has originium item/fluid inputs but no
 *       mapping: {@link HeatCondition#SUPERHEATED} uses melting, {@link HeatCondition#HEATED}
 *       uses shard mixing, unheated uses alloy mixing (fluids) or the tagged-item amount.</li>
 * </ol>
 * <p>
 * Create 6.0.4 stores the <em>recipe type</em> on {@link ProcessingRecipe#id}
 * ({@code create:mixing}, {@code create:milling}, …), not the datapack id.
 * Machine mixins therefore pass the {@link Recipe} instance; this helper resolves
 * {@link RecipeHolder#id()} via {@link net.minecraft.world.item.crafting.RecipeManager}.
 */
public final class DustProductionHelper {

    private static final String MOD_ID = CreateOriginiumIndustry.MODID;

    /**
     * Identity cache of {@link RecipeHolder#value()} → datapack id. Recipe
     * instances are stable until datapack reload; {@link #clearRecipeIdCache()}
     * is called from {@link DustEmissionIndex}.
     */
    private static final IdentityHashMap<Recipe<?>, ResourceLocation> HOLDER_IDS = new IdentityHashMap<>();

    private DustProductionHelper() {}

    /**
     * Drop cached holder ids after a datapack / recipe reload.
     */
    public static void clearRecipeIdCache() {
        HOLDER_IDS.clear();
    }

    /**
     * Resolves the dust production amount for a given recipe ID.
     * Returns 0 if the recipe is not an originium dust-producing recipe.
     * Does not apply the heat-aware fallback (that needs the {@link Recipe} object).
     */
    public static int getDustForRecipe(ResourceLocation recipeId) {
        int mapped = mappedAmount(recipeId);
        return Math.max(0, mapped);
    }

    /**
     * Amount for a completed Create recipe, resolving {@link RecipeHolder#id()}
     * and applying the heat-aware originium-input fallback when unmapped.
     */
    public static int getDustForRecipe(ServerLevel level, Recipe<?> recipe) {
        if (recipe == null) {
            return 0;
        }
        return amountForResolved(resolveRecipeId(level, recipe), recipe);
    }

    private static int amountForResolved(ResourceLocation id, Recipe<?> recipe) {
        int mapped = mappedAmount(id);
        if (mapped >= 0) {
            return mapped;
        }
        return heatAwareFallback(recipe);
    }

    /**
     * Dust emitted when processing {@code stack} without a mapped recipe id.
     */
    public static int getDustForItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int data = DustEmissionIndex.getItemAmount(stack);
        if (data >= 0) {
            return data;
        }
        if (DustEmissionIndex.isDustProducing(stack)) {
            return COIConfig.DUST_FROM_TAGGED_ITEM.get();
        }
        return 0;
    }

    /**
     * Config override for frozen recipe ids documented in {@code docs/REGISTRY.md}.
     *
     * @return amount, or {@code -1} if this id is not a frozen production recipe
     */
    public static int configOverride(ResourceLocation recipeId) {
        if (recipeId == null || !MOD_ID.equals(recipeId.getNamespace())) {
            return -1;
        }
        return switch (recipeId.getPath()) {
            case "milling/raw_originium_milling" -> COIConfig.DUST_FROM_MILLING.get();
            case "crushing/raw_originium_crushing" -> COIConfig.DUST_FROM_CRUSHING.get();
            case "mixing/originium_shard_mixing" -> COIConfig.DUST_FROM_SHARD_MIXING.get();
            case "mixing/originium_mixing" -> COIConfig.DUST_FROM_ORIGINIUM_MELTING.get();
            case "mixing/molten_originium_iron_ingot_mixing" -> COIConfig.DUST_FROM_ALLOY_MIXING.get();
            default -> -1;
        };
    }

    /**
     * Datapack/config amount, or {@code -1} if this id has no mapping.
     * Explicit datapack {@code 0} (e.g. catalyst mixing) is a mapping.
     */
    public static int mappedAmount(ResourceLocation recipeId) {
        int config = configOverride(recipeId);
        if (config >= 0) {
            return config;
        }
        return DustEmissionIndex.getRecipeAmount(recipeId);
    }

    /**
     * Real datapack recipe id for a live {@link Recipe} instance.
     * <p>
     * Create 6 JSON codecs build {@link ProcessingRecipe} with
     * {@code AllRecipeTypes.id} ({@code create:milling} / {@code create:mixing} /
     * {@code create:crushing}), so {@link ProcessingRecipe#id} must not be used
     * as the emission key. Identity-match against {@link RecipeHolder#value()}.
     */
    public static ResourceLocation resolveRecipeId(ServerLevel level, Recipe<?> recipe) {
        if (recipe == null) {
            return null;
        }
        ResourceLocation cached = HOLDER_IDS.get(recipe);
        if (cached != null) {
            return cached;
        }
        ResourceLocation id = lookupHolderId(level, recipe);
        if (id != null) {
            HOLDER_IDS.put(recipe, id);
            return id;
        }
        // Create type ids (create:mixing / create:milling) are not cached:
        // a later call with a real RecipeManager must still be able to resolve.
        if (recipe instanceof ProcessingRecipe<?> processing) {
            return processing.id;
        }
        return null;
    }

    /**
     * Scan only recipes of the same {@link RecipeType}, not the full registry.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceLocation lookupHolderId(ServerLevel level, Recipe<?> recipe) {
        if (level == null) {
            return null;
        }
        RecipeType type = recipe.getType();
        List<RecipeHolder<?>> holders = level.getRecipeManager().getAllRecipesFor(type);
        for (RecipeHolder<?> holder : holders) {
            if (holder.value() == recipe) {
                return holder.id();
            }
        }
        return null;
    }

    /**
     * Emits dust at a block position if the recipe produces originium dust.
     * Called by Mixins after a Create machine completes a recipe.
     * Goes through {@link DustSubmission} ({@link IOridustProducer}).
     */
    public static void emitDustFromRecipe(ServerLevel level, BlockPos pos, ResourceLocation recipeId) {
        if (level == null || !COIConfig.ENABLE_DUST_PRODUCTION.get()) {
            return;
        }
        int dustAmount = getDustForRecipe(recipeId);
        if (dustAmount <= 0) {
            return;
        }
        submit(level, pos, dustAmount, recipeId);
    }

    /**
     * Emits dust for a completed Create {@link Recipe}, resolving the holder id
     * once (and applying heat-aware fallback) the same way machine mixins do.
     */
    public static void emitDustFromRecipe(ServerLevel level, BlockPos pos, Recipe<?> recipe) {
        if (level == null || recipe == null || !COIConfig.ENABLE_DUST_PRODUCTION.get()) {
            return;
        }
        ResourceLocation id = resolveRecipeId(level, recipe);
        int dustAmount = amountForResolved(id, recipe);
        if (dustAmount <= 0) {
            return;
        }
        submit(level, pos, dustAmount, id);
    }

    /**
     * Emits dust for an item being processed when no recipe mapping applies.
     */
    public static void emitDustFromItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (level == null || !COIConfig.ENABLE_DUST_PRODUCTION.get()) {
            return;
        }
        int dustAmount = getDustForItem(stack);
        if (dustAmount <= 0) {
            return;
        }
        DustSubmission.submit(level, pos, dustAmount, DustReason.MACHINE_PROCESSING);
    }

    /**
     * Unmapped processing recipes that consume originium items or fluids still
     * emit, scaled by {@link HeatCondition}. Cultivation solution (catalyst)
     * has no originium feedstock and returns 0.
     */
    public static int heatAwareFallback(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return 0;
        }
        if (!containsOriginiumInput(processing)) {
            return 0;
        }
        HeatCondition heat = processing.getRequiredHeat();
        if (heat == HeatCondition.SUPERHEATED) {
            return COIConfig.DUST_FROM_ORIGINIUM_MELTING.get();
        }
        if (heat == HeatCondition.HEATED) {
            return COIConfig.DUST_FROM_SHARD_MIXING.get();
        }
        if (hasOriginiumFluidInput(processing)) {
            return COIConfig.DUST_FROM_ALLOY_MIXING.get();
        }
        return COIConfig.DUST_FROM_TAGGED_ITEM.get();
    }

    public static boolean containsOriginiumInput(ProcessingRecipe<?> recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            try {
                for (ItemStack stack : ingredient.getItems()) {
                    if (isOriginiumItem(stack)) {
                        return true;
                    }
                }
            } catch (RuntimeException ignored) {
                // Custom ingredients may not enumerate stacks.
            }
        }
        return hasOriginiumFluidInput(recipe);
    }

    private static boolean hasOriginiumFluidInput(ProcessingRecipe<?> recipe) {
        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            try {
                for (FluidStack stack : fluidIngredient.getMatchingFluidStacks()) {
                    if (stack != null && !stack.isEmpty() && stack.getFluid().is(COITags.Fluids.ORIGINIUM_FLUIDS)) {
                        return true;
                    }
                }
            } catch (RuntimeException ignored) {
                // Custom fluid ingredients may not enumerate stacks.
            }
        }
        return false;
    }

    private static boolean isOriginiumItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (
                DustEmissionIndex.isDustProducing(stack) || getDustForItem(stack) > 0);
    }

    private static void submit(ServerLevel level, BlockPos pos, int dustAmount, ResourceLocation recipeId) {
        int deposited = DustSubmission.submit(level, pos, dustAmount, DustReason.MACHINE_PROCESSING);
        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Machine at [{}, {}, {}] recipe {} expected {} deposited {}",
                    pos.getX(), pos.getY(), pos.getZ(), recipeId, dustAmount, deposited
            );
        }
    }
}
