package com.mealuet.create_originium_industry.mixin;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.purest.BasinProcessRequirements;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Mixin into {@link BasinOperatingBlockEntity}:
 * <ul>
 *   <li>Gate sieve / cooling-chamber / no-heat recipes on the purest line.</li>
 *   <li>Emit originium dust and wear the cooling chamber only when a basin
 *       recipe actually applies. Passes the live {@link Recipe} so
 *       {@link DustProductionHelper} can resolve {@code RecipeHolder.id()} —
 *       Create 6 stores the recipe <em>type</em> on {@code ProcessingRecipe.id}.</li>
 * </ul>
 */
@Mixin(value = BasinOperatingBlockEntity.class, remap = false)
public abstract class BasinOperatingBlockEntityMixin {

    @Shadow
    protected abstract Optional<BasinBlockEntity> getBasin();

    @Inject(method = "matchBasinRecipe", at = @At("RETURN"), cancellable = true)
    private void coi$matchBasinProcess(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || recipe == null) {
            return;
        }
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty()) {
            return;
        }
        if (!BasinProcessRequirements.matches(basin.get(), recipe)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "applyBasinRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)Z"
            )
    )
    private boolean coi$applyBasinRecipeWithProcess(BasinBlockEntity basin, Recipe<?> recipe) {
        boolean applied = BasinRecipe.apply(basin, recipe);
        if (!applied) {
            return false;
        }
        BasinOperatingBlockEntity self = (BasinOperatingBlockEntity) (Object) this;
        if (self.getLevel() instanceof ServerLevel serverLevel) {
            DustProductionHelper.emitDustFromRecipe(serverLevel, self.getBlockPos(), recipe);
            BasinProcessRequirements.onApplied(serverLevel, basin.getBlockPos(), recipe);
        }
        return true;
    }
}
