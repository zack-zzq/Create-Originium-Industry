package com.mealuet.create_originium_industry.compat.jei;

import com.mealuet.create_originium_industry.compat.recipeviewer.ReactorInfoDisplay;
import com.mealuet.create_originium_industry.config.COIConfig;
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
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class ReactorInfoCategory implements IRecipeCategory<ReactorInfoDisplay> {

    private final IDrawable icon;

    public ReactorInfoCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(COIBlocks.POWER_CORE.asStack());
    }

    @Override
    public RecipeType<ReactorInfoDisplay> getRecipeType() {
        return COIJeiPlugin.REACTOR;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.create_originium_industry.reactor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ReactorInfoDisplay recipe, IFocusGroup focuses) {
        JeiLayouts.addItemStacks(builder, RecipeIngredientRole.INPUT, recipe.items(), 6, 14);
        JeiLayouts.addFluids(builder, RecipeIngredientRole.INPUT, recipe.fluids(), 6, 34);
    }

    @Override
    public void draw(ReactorInfoDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable(recipe.titleKey()), 6, 0, 0xFF404040, false);
        Component body = switch (recipe.kind()) {
            case FUEL -> Component.translatable(recipe.bodyKey(), COIConfig.reactorFuelTicksPerItem());
            case COOLING, STRUCTURE -> Component.translatable(recipe.bodyKey());
        };
        List<FormattedCharSequence> lines = font.split(body, JeiLayouts.WIDTH - 12);
        int y = 54;
        for (FormattedCharSequence line : lines) {
            if (y > JeiLayouts.HEIGHT - 10) {
                break;
            }
            graphics.drawString(font, line, 6, y, 0xFF404040, false);
            y += 9;
        }
    }
}
