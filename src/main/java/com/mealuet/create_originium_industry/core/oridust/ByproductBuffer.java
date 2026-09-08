package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.nbt.CompoundTag;

/**
 * Converts captured chunk/emission dust into whole {@code originium_dust}
 * items without duplicating or voiding remainder.
 * <p>
 * Invariant when {@code dustPerItem > 0}:
 * {@code totalCaptured == itemsCreated * dustPerItem + stored()}.
 * When {@code dustPerItem <= 0}, byproduct is disabled: captured dust is a
 * pollution sink (not an item), and the buffer stays empty.
 */
public final class ByproductBuffer {

    public static final String NBT_KEY = "CapturedDust";

    private int stored;

    public int stored() {
        return stored;
    }

    /**
     * @param captured    dust units just captured (must be ≥ 0)
     * @param dustPerItem units required per item; {@code <= 0} disables byproduct
     * @return whole items produced from this call (0 if none)
     */
    public int add(int captured, int dustPerItem) {
        if (captured <= 0) {
            return 0;
        }
        if (dustPerItem <= 0) {
            return 0;
        }
        long sum = (long) stored + captured;
        if (sum > Integer.MAX_VALUE) {
            sum = Integer.MAX_VALUE;
        }
        stored = (int) sum;
        int items = stored / dustPerItem;
        stored -= items * dustPerItem;
        return items;
    }

    public void save(CompoundTag tag) {
        tag.putInt(NBT_KEY, stored);
    }

    public void load(CompoundTag tag) {
        stored = Math.max(0, tag.getInt(NBT_KEY));
    }
}
