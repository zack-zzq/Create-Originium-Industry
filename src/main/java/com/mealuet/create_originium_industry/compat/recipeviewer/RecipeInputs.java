package com.mealuet.create_originium_industry.compat.recipeviewer;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared Create-recipe unpacking for JEI/EMI display records.
 * Lives outside {@code compat.jei} so GameTests can use it without JEI.
 */
public final class RecipeInputs {

    private RecipeInputs() {}

    public static List<Ingredient> itemIngredients(Recipe<?> recipe) {
        if (recipe == null) {
            return List.of();
        }
        return List.copyOf(recipe.getIngredients());
    }

    public static List<FluidStack> fluidIngredients(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return List.of();
        }
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidIngredient ingredient : processing.getFluidIngredients()) {
            try {
                List<FluidStack> matching = ingredient.getMatchingFluidStacks();
                if (matching != null && !matching.isEmpty()) {
                    fluids.add(matching.getFirst().copy());
                }
            } catch (RuntimeException ignored) {
                // Custom fluid ingredients may not enumerate stacks.
            }
        }
        return List.copyOf(fluids);
    }

    public static List<ItemStack> itemResults(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return List.of();
        }
        return List.copyOf(processing.getRollableResultsAsItemStacks());
    }

    public static List<FluidStack> fluidResults(Recipe<?> recipe) {
        if (!(recipe instanceof ProcessingRecipe<?> processing)) {
            return List.of();
        }
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidStack stack : processing.getFluidResults()) {
            if (stack != null && !stack.isEmpty()) {
                fluids.add(stack.copy());
            }
        }
        return List.copyOf(fluids);
    }
}
