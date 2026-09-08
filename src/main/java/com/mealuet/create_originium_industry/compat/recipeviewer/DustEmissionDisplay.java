package com.mealuet.create_originium_industry.compat.recipeviewer;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

/**
 * One {@code coi_dust_emission} mapping (recipe, item, or item tag).
 */
public record DustEmissionDisplay(
        Kind kind,
        ResourceLocation id,
        int amount,
        List<Ingredient> itemInputs,
        List<FluidStack> fluidInputs,
        List<ItemStack> itemOutputs,
        List<FluidStack> fluidOutputs
) {
    public enum Kind {
        RECIPE,
        ITEM,
        ITEM_TAG
    }

    public static DustEmissionDisplay recipe(ResourceLocation recipeId, int datapackAmount, RecipeManager recipes) {
        int amount = effectiveRecipeAmount(recipeId, datapackAmount);
        Recipe<?> recipe = find(recipes, recipeId);
        return new DustEmissionDisplay(
                Kind.RECIPE,
                recipeId,
                amount,
                RecipeInputs.itemIngredients(recipe),
                RecipeInputs.fluidIngredients(recipe),
                RecipeInputs.itemResults(recipe),
                RecipeInputs.fluidResults(recipe)
        );
    }

    public static DustEmissionDisplay item(ResourceLocation itemId, int amount) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        List<Ingredient> inputs = item == Items.AIR
                ? List.of()
                : List.of(Ingredient.of(item));
        return new DustEmissionDisplay(Kind.ITEM, itemId, amount, inputs, List.of(), List.of(), List.of());
    }

    public static DustEmissionDisplay itemTag(ResourceLocation tagId, int amount) {
        Ingredient ingredient = Ingredient.of(TagKey.create(Registries.ITEM, tagId));
        return new DustEmissionDisplay(Kind.ITEM_TAG, tagId, amount, List.of(ingredient), List.of(), List.of(), List.of());
    }

    /**
     * Config overrides win for frozen recipe ids; otherwise the datapack amount.
     */
    public static int effectiveRecipeAmount(ResourceLocation recipeId, int datapackAmount) {
        int mapped = DustProductionHelper.mappedAmount(recipeId);
        return mapped >= 0 ? mapped : Math.max(0, datapackAmount);
    }

    private static Recipe<?> find(RecipeManager recipes, ResourceLocation recipeId) {
        if (recipes == null || recipeId == null) {
            return null;
        }
        Optional<RecipeHolder<?>> holder = recipes.byKey(recipeId);
        return holder.map(RecipeHolder::value).orElse(null);
    }
}
