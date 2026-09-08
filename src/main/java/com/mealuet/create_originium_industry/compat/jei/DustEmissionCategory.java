package com.mealuet.create_originium_industry.compat.jei;

import com.mealuet.create_originium_industry.compat.recipeviewer.DustEmissionDisplay;
import com.mealuet.create_originium_industry.index.COIItems;
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

public final class DustEmissionCategory implements IRecipeCategory<DustEmissionDisplay> {

    private final IDrawable icon;

    public DustEmissionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(COIItems.ORIGINIUM_DUST.asStack());
    }

    @Override
    public RecipeType<DustEmissionDisplay> getRecipeType() {
        return COIJeiPlugin.DUST_EMISSION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.create_originium_industry.dust_emission");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DustEmissionDisplay recipe, IFocusGroup focuses) {
        JeiLayouts.addIngredients(builder, RecipeIngredientRole.INPUT, recipe.itemInputs(), 6, 12);
        JeiLayouts.addFluids(builder, RecipeIngredientRole.INPUT, recipe.fluidInputs(), 6, 32);
        JeiLayouts.addItemStacks(builder, RecipeIngredientRole.OUTPUT, recipe.itemOutputs(), 100, 12);
        JeiLayouts.addFluids(builder, RecipeIngredientRole.OUTPUT, recipe.fluidOutputs(), 100, 32);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 154, 22)
                .addItemStack(COIItems.ORIGINIUM_DUST.asStack());
    }

    @Override
    public void draw(DustEmissionDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, recipe.id().getPath(), 6, 0, 0xFF808080, false);
        Component amount = recipe.amount() <= 0
                ? Component.translatable("jei.create_originium_industry.dust_emission.none")
                : Component.translatable("jei.create_originium_industry.dust_emission.amount", recipe.amount());
        graphics.drawString(font, amount, 6, 70, 0xFF404040, false);
    }
}
