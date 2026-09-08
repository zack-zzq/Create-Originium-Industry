package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.index.COIItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Drops {@code originium_dust} items produced by {@link ByproductBuffer}.
 * Captured pollution stays in the processing loop instead of being voided.
 */
public final class DustByproduct {

    private DustByproduct() {}

    public static void dropItems(Level level, BlockPos pos, int items) {
        if (items <= 0 || level == null || level.isClientSide || pos == null) {
            return;
        }
        int remaining = items;
        int max = COIItems.ORIGINIUM_DUST.get().getDefaultMaxStackSize();
        while (remaining > 0) {
            int n = Math.min(max, remaining);
            Containers.dropItemStack(level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    new ItemStack(COIItems.ORIGINIUM_DUST.get(), n));
            remaining -= n;
        }
    }
}
