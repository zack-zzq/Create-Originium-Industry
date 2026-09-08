package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Protection-gear hooks for exposure and infection gain.
 * <p>
 * No protection items are registered yet. Reduction applies only when the
 * entity wears items tagged {@code create_originium_industry:originium_protection}.
 * Unprotected players always receive full gain so existing worlds are unchanged.
 */
public final class ProtectionHooks {

    private ProtectionHooks() {}

    public static double incomingExposureFactor(LivingEntity entity) {
        return incomingFactor(countProtectionPieces(entity), COIConfig.PROTECTION_EXPOSURE_REDUCTION.get());
    }

    public static double incomingInfectionFactor(LivingEntity entity) {
        return incomingFactor(countProtectionPieces(entity), COIConfig.PROTECTION_INFECTION_REDUCTION.get());
    }

    /**
     * Formula used by gameplay and GameTests. {@code reduction} is the full-set
     * fraction removed from incoming gain (0.5 → half gain with a full set).
     */
    public static double incomingFactor(int protectionPieces, double reduction) {
        if (!COIConfig.ENABLE_PROTECTION.get() || protectionPieces <= 0 || reduction <= 0.0) {
            return 1.0;
        }
        int fullSet = Math.max(1, COIConfig.PROTECTION_FULL_SET_PIECES.get());
        if (COIConfig.PROTECTION_REQUIRES_FULL_SET.get() && protectionPieces < fullSet) {
            return 1.0;
        }
        double coverage = Math.min(1.0, protectionPieces / (double) fullSet);
        return Math.max(0.0, 1.0 - reduction * coverage);
    }

    public static int countProtectionPieces(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (isProtectionItem(stack)) {
                count++;
            }
        }
        // Hands count too so future masks/charms can tag without being armor.
        if (isProtectionItem(entity.getMainHandItem())) {
            count++;
        }
        if (isProtectionItem(entity.getOffhandItem())) {
            count++;
        }
        return count;
    }

    public static boolean isProtectionItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(COITags.Items.ORIGINIUM_PROTECTION);
    }
}
