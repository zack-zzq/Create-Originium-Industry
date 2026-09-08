package com.mealuet.create_originium_industry.core.reactor;

import com.mealuet.create_originium_industry.config.COIConfig;

/**
 * Draft stability: {@code S = C * M - H}.
 * <p>
 * {@code H} is heat load (core heat × fuel heat-capacity multiplier).
 * {@code C} is heat capacity of coolant / water / hot water inventories.
 * {@code M} is cooling multiplier (config × attached chambers).
 * {@code S > 0} stable, {@code |S| ≤ epsilon} borderline, {@code S < 0} unstable.
 */
public final class StabilityMath {

    private StabilityMath() {}

    public enum Sign {
        POSITIVE,
        ZERO,
        NEGATIVE
    }

    public record Snapshot(double heat, double capacity, double cooling, double stability) {
        public Sign sign() {
            return StabilityMath.sign(stability);
        }

        public boolean generating() {
            return heat > 0.0;
        }
    }

    public static Sign sign(double stability) {
        double epsilon = COIConfig.reactorStabilityEpsilon();
        if (stability > epsilon) {
            return Sign.POSITIVE;
        }
        if (stability < -epsilon) {
            return Sign.NEGATIVE;
        }
        return Sign.ZERO;
    }

    /**
     * Heat load while the core is actually running on a fuel type.
     */
    public static double heatLoad(boolean running, boolean purestFuel) {
        if (!running) {
            return 0.0;
        }
        double core = COIConfig.reactorCoreHeat();
        double factor = purestFuel
                ? COIConfig.reactorPurestHeatCapacity()
                : COIConfig.reactorMoltenHeatCapacity();
        return core * factor;
    }

    public static double heatCapacity(int coolantMb, int waterMb, int hotWaterMb) {
        return coolantMb * COIConfig.reactorCoolantHeatPerMb()
                + waterMb * COIConfig.reactorWaterHeatPerMb()
                + hotWaterMb * COIConfig.reactorHotWaterHeatPerMb();
    }

    public static double coolingMultiplier(double chamberCooling) {
        return COIConfig.reactorCoolingMultiplier() * Math.max(0.0, chamberCooling);
    }

    public static Snapshot compute(
            boolean running,
            boolean purestFuel,
            int coolantMb,
            int waterMb,
            int hotWaterMb,
            double chamberCooling
    ) {
        double heat = heatLoad(running, purestFuel);
        double capacity = heatCapacity(coolantMb, waterMb, hotWaterMb);
        double cooling = coolingMultiplier(chamberCooling);
        return new Snapshot(heat, capacity, cooling, capacity * cooling - heat);
    }

    public static double nextInstability(double current, double stability) {
        Sign sign = sign(stability);
        double next = switch (sign) {
            case NEGATIVE -> current + COIConfig.reactorInstabilityGain();
            case POSITIVE -> current - COIConfig.reactorInstabilityDecay();
            case ZERO -> current;
        };
        return Math.max(0.0, next);
    }
}
