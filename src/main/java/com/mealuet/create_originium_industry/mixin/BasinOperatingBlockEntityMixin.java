package com.mealuet.create_originium_industry.mixin;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link BasinOperatingBlockEntity} to emit originium dust
 * when basin recipes (mixing, including heated / superheated, and compacting)
 * involving originium complete.
 * <p>
 * Passes the live {@link net.minecraft.world.item.crafting.Recipe} so
 * {@link DustProductionHelper} can resolve {@code RecipeHolder.id()} — Create 6
 * stores the recipe <em>type</em> on {@code ProcessingRecipe.id}.
 */
@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingBlockEntityMixin {

    @Shadow
    protected net.minecraft.world.item.crafting.Recipe<?> currentRecipe;

    @Inject(method = "applyBasinRecipe", at = @At("TAIL"))
    protected void coi$afterApplyBasinRecipe(CallbackInfo ci) {
        BasinOperatingBlockEntity self = (BasinOperatingBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) return;
        if (currentRecipe == null) return;

        DustProductionHelper.emitDustFromRecipe(serverLevel, self.getBlockPos(), currentRecipe);
    }
}
