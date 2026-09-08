package com.mealuet.create_originium_industry.compat.recipeviewer;

import com.mealuet.create_originium_industry.core.purest.BasinProcessSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

/**
 * One {@code coi_basin_process} mapping plus the linked Create recipe, if loaded.
 */
public record BasinProcessDisplay(
        ResourceLocation recipeId,
        BasinProcessSpec spec,
        List<Ingredient> itemInputs,
        List<FluidStack> fluidInputs,
        List<ItemStack> itemOutputs,
        List<FluidStack> fluidOutputs
) {
    public static BasinProcessDisplay of(ResourceLocation recipeId, BasinProcessSpec spec, RecipeManager recipes) {
        Recipe<?> recipe = find(recipes, recipeId);
        return new BasinProcessDisplay(
                recipeId,
                spec == null ? BasinProcessSpec.NONE : spec,
                RecipeInputs.itemIngredients(recipe),
                RecipeInputs.fluidIngredients(recipe),
                RecipeInputs.itemResults(recipe),
                RecipeInputs.fluidResults(recipe)
        );
    }

    private static Recipe<?> find(RecipeManager recipes, ResourceLocation recipeId) {
        if (recipes == null || recipeId == null) {
            return null;
        }
        Optional<RecipeHolder<?>> holder = recipes.byKey(recipeId);
        return holder.map(RecipeHolder::value).orElse(null);
    }
}
