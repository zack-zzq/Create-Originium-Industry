package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Protection-gear hooks for exposure and infection gain.
 * <p>
 * Reduction applies when the entity wears items tagged
 * {@code create_originium_industry:originium_protection}
 * (respirator on the head, filter canister on the chest, or tagged hands).
 * Unprotected players always receive full gain.
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

    /**
     * Percent of exposure gain removed (0–100). Same number the dust meter shows.
     */
    public static int exposureReductionPercent(LivingEntity entity) {
        double factor = incomingExposureFactor(entity);
        return Mth.clamp((int) Math.round((1.0 - factor) * 100.0), 0, 100);
    }

    /**
     * Wear tagged gear while standing in dusty air. Broken pieces return
     * {@code originium_dust} so spent filter media re-enters the factory loop.
     */
    public static void wearProtectionInDust(LivingEntity entity) {
        if (entity == null || !COIConfig.ENABLE_PROTECTION.get()) {
            return;
        }
        int loss = COIConfig.PROTECTION_DURABILITY_LOSS.get();
        if (loss <= 0) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!isProtectionItem(stack) || !stack.isDamageableItem()) {
                continue;
            }
            boolean willBreak = stack.getDamageValue() + loss >= stack.getMaxDamage();
            stack.hurtAndBreak(loss, entity, slot);
            if (willBreak && entity instanceof Player player) {
                player.spawnAtLocation(COIItems.ORIGINIUM_DUST.get());
            }
        }
    }
}
