package com.mealuet.create_originium_industry.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

final class JeiLayouts {

    static final int WIDTH = 176;
    static final int HEIGHT = 90;

    private JeiLayouts() {}

    static int addIngredients(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
                              List<Ingredient> ingredients, int x, int y) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null || ingredient.isEmpty()) {
                continue;
            }
            builder.addSlot(role, x, y).addIngredients(ingredient);
            x += 18;
        }
        return x;
    }

    static int addItemStacks(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
                             List<ItemStack> stacks, int x, int y) {
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            builder.addSlot(role, x, y).addItemStack(stack);
            x += 18;
        }
        return x;
    }

    static int addFluids(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
                         List<FluidStack> fluids, int x, int y) {
        for (FluidStack fluid : fluids) {
            if (fluid == null || fluid.isEmpty()) {
                continue;
            }
            int amount = Math.max(1, fluid.getAmount());
            builder.addSlot(role, x, y)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(fluid.copy()))
                    .setFluidRenderer(amount, false, 16, 16);
            x += 18;
        }
        return x;
    }
}
