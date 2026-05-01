package com.mealuet.create_originium_industry.mixin;

import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into {@link MillstoneBlockEntity} to emit originium dust
 * when milling originium recipes complete.
 */
@Mixin(value = MillstoneBlockEntity.class, remap = false)
public abstract class MillstoneBlockEntityMixin {

    @Shadow
    private MillingRecipe lastRecipe;

    @Inject(method = "process", at = @At("TAIL"))
    private void coi$afterProcess(CallbackInfo ci) {
        MillstoneBlockEntity self = (MillstoneBlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) return;
        if (lastRecipe == null) return;

        // ProcessingRecipe has a public `id` field (ResourceLocation)
        if (lastRecipe.id != null) {
            DustProductionHelper.emitDustFromRecipe(serverLevel, self.getBlockPos(), lastRecipe.id);
        }
    }
}
