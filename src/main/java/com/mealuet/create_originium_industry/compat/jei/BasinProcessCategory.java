package com.mealuet.create_originium_industry.compat.jei;

import com.mealuet.create_originium_industry.compat.recipeviewer.BasinProcessDisplay;
import com.mealuet.create_originium_industry.core.purest.BasinProcessSpec;
import com.mealuet.create_originium_industry.index.COIBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class BasinProcessCategory implements IRecipeCategory<BasinProcessDisplay> {

    private final IDrawable icon;

    public BasinProcessCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(COIBlocks.DUST_SIEVE.asStack());
    }

    @Override
    public RecipeType<BasinProcessDisplay> getRecipeType() {
        return COIJeiPlugin.BASIN_PROCESS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.create_originium_industry.basin_process");
    }

    @Override
    public int getWidth() {
        return JeiLayouts.WIDTH;
    }

    @Override
    public int getHeight() {
        return JeiLayouts.HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasinProcessDisplay recipe, IFocusGroup focuses) {
        JeiLayouts.addIngredients(builder, RecipeIngredientRole.INPUT, recipe.itemInputs(), 6, 8);
        JeiLayouts.addFluids(builder, RecipeIngredientRole.INPUT, recipe.fluidInputs(), 6, 28);
        JeiLayouts.addItemStacks(builder, RecipeIngredientRole.OUTPUT, recipe.itemOutputs(), 118, 8);
        JeiLayouts.addFluids(builder, RecipeIngredientRole.OUTPUT, recipe.fluidOutputs(), 118, 28);

        BasinProcessSpec spec = recipe.spec();
        int catalystX = 6;
        if (spec.requireSieve()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, catalystX, 48)
                    .addItemStack(COIBlocks.DUST_SIEVE.asStack());
            builder.addSlot(RecipeIngredientRole.CATALYST, catalystX + 18, 48)
                    .addItemStack(COIBlocks.ALLOY_SIEVE.asStack());
            catalystX += 40;
        }
        if (spec.requireCoolingChamber()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, catalystX, 48)
                    .addItemStack(COIBlocks.COOLING_CHAMBER.asStack());
        }
    }

    @Override
    public void draw(BasinProcessDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, recipe.recipeId().getPath(), 6, 0, 0xFF808080, false);

        int textY = 68;
        BasinProcessSpec spec = recipe.spec();
        if (spec.requireSieve()) {
            graphics.drawString(font,
                    Component.translatable("jei.create_originium_industry.basin_process.require_sieve"),
                    6, textY, 0xFF404040, false);
            textY += 9;
        }
        if (spec.requireCoolingChamber()) {
            graphics.drawString(font,
                    Component.translatable("jei.create_originium_industry.basin_process.require_cooling_chamber"),
                    6, textY, 0xFF404040, false);
            textY += 9;
        }
        if (spec.rejectHeat()) {
            graphics.drawString(font,
                    Component.translatable("jei.create_originium_industry.basin_process.reject_heat"),
                    6, textY, 0xFF404040, false);
        }
    }
}
