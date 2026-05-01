package com.mealuet.create_originium_industry.mixin;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link BasinOperatingBlockEntity} to emit originium dust
 * when basin recipes (mixing, compacting) involving originium complete.
 */
@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingBlockEntityMixin {

    @Shadow
    protected Recipe<?> currentRecipe;

    @Inject(method = "applyBasinRecipe", at = @At("TAIL"))
    protected void coi$afterApplyBasinRecipe(CallbackInfo ci) {
        BasinOperatingBlockEntity self = (BasinOperatingBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) return;
        if (currentRecipe == null) return;

        // In Create, basin recipes are ProcessingRecipe instances with a public `id` field
        ResourceLocation recipeId = null;
        if (currentRecipe instanceof ProcessingRecipe<?> processingRecipe) {
            recipeId = processingRecipe.id;
        }

        if (recipeId != null) {
            DustProductionHelper.emitDustFromRecipe(serverLevel, self.getBlockPos(), recipeId);
        }
    }
}
