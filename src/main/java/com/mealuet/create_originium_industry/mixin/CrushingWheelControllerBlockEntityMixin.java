package com.mealuet.create_originium_industry.mixin;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Mixin into {@link CrushingWheelControllerBlockEntity} to emit originium dust
 * when crushing originium recipes complete.
 */
@Mixin(value = CrushingWheelControllerBlockEntity.class, remap = false)
public abstract class CrushingWheelControllerBlockEntityMixin {

    @Inject(method = "applyRecipe", at = @At("HEAD"))
    private void coi$beforeApplyRecipe(CallbackInfo ci) {
        CrushingWheelControllerBlockEntity self = (CrushingWheelControllerBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) return;

        // findRecipe() is a public method on CrushingWheelControllerBlockEntity
        Optional<RecipeHolder<ProcessingRecipe<RecipeWrapper>>> recipeOpt = self.findRecipe();
        if (recipeOpt.isEmpty()) return;

        ResourceLocation recipeId = recipeOpt.get().id();
        DustProductionHelper.emitDustFromRecipe(serverLevel, self.getBlockPos(), recipeId);
    }
}
