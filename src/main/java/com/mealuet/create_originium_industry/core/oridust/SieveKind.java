package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Kinetic-filter insert / process-sieve grade. Standard is the frozen
 * {@code originium_dust_sieve}; alloy is the M2 upgrade. Old filter NBT
 * without {@code SieveKind} reads as {@link #STANDARD}.
 */
public enum SieveKind {
    STANDARD,
    ALLOY;

    public static final String NBT_KEY = "SieveKind";

    public String id() {
        return name().toLowerCase();
    }

    public static SieveKind fromNbt(String value) {
        if (value == null || value.isEmpty() || "iron".equals(value) || "sieve".equals(value)) {
            return STANDARD;
        }
        if ("alloy".equals(value)) {
            return ALLOY;
        }
        try {
            return SieveKind.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return STANDARD;
        }
    }

    public static SieveKind fromItem(Item item) {
        if (item == null) {
            return null;
        }
        if (item == COIBlocks.ALLOY_SIEVE.asItem()) {
            return ALLOY;
        }
        if (item == COIBlocks.DUST_SIEVE.asItem()) {
            return STANDARD;
        }
        return null;
    }

    public static SieveKind fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return fromItem(stack.getItem());
    }

    public static SieveKind fromBlock(Block block) {
        if (block == COIBlocks.ALLOY_SIEVE.get()) {
            return ALLOY;
        }
        return STANDARD;
    }

    public static boolean isSieveItem(ItemStack stack) {
        return fromStack(stack) != null;
    }

    public Item item() {
        return this == ALLOY ? COIBlocks.ALLOY_SIEVE.asItem() : COIBlocks.DUST_SIEVE.asItem();
    }

    public int durability() {
        return this == ALLOY ? COIConfig.alloySieveDurability() : COIConfig.processSieveDurability();
    }

    public double processCapture() {
        return this == ALLOY
                ? COIConfig.ALLOY_PROCESS_SIEVE_EMISSION_CAPTURE.get()
                : COIConfig.PROCESS_SIEVE_EMISSION_CAPTURE.get();
    }

    public double filterCapture() {
        return this == ALLOY
                ? COIConfig.ALLOY_FILTER_EMISSION_CAPTURE.get()
                : COIConfig.FILTER_EMISSION_CAPTURE.get();
    }
}
