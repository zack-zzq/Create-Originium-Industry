package com.mealuet.create_originium_industry.core.reactor;

/**
 * Coolant ↔ water ↔ hot water conversion driven by {@code S}.
 * <p>
 * {@code S >= 0}: one step toward coolant (hot water → water, else water → coolant).
 * {@code S < 0}: one step toward hot water (coolant → water, else water → hot water).
 * Never voids fluid; conversion stops when the destination tank is full.
 */
public final class ReactorFluids {

    private ReactorFluids() {}

    public record Amounts(int coolant, int water, int hotWater) {
        public Amounts {
            coolant = Math.max(0, coolant);
            water = Math.max(0, water);
            hotWater = Math.max(0, hotWater);
        }

        public int total() {
            return coolant + water + hotWater;
        }
    }

    public static Amounts convert(Amounts in, double stability, int budgetMb, int tankCapacity) {
        if (in == null || budgetMb <= 0 || tankCapacity <= 0) {
            return in == null ? new Amounts(0, 0, 0) : in;
        }
        int budget = Math.max(0, budgetMb);
        int cap = Math.max(1, tankCapacity);
        if (stability >= 0.0) {
            return convertTowardCoolant(in, budget, cap);
        }
        return convertTowardHotWater(in, budget, cap);
    }

    private static Amounts convertTowardCoolant(Amounts in, int budget, int cap) {
        if (in.hotWater() > 0) {
            int move = Math.min(budget, Math.min(in.hotWater(), Math.max(0, cap - in.water())));
            return new Amounts(in.coolant(), in.water() + move, in.hotWater() - move);
        }
        int move = Math.min(budget, Math.min(in.water(), Math.max(0, cap - in.coolant())));
        return new Amounts(in.coolant() + move, in.water() - move, in.hotWater());
    }

    private static Amounts convertTowardHotWater(Amounts in, int budget, int cap) {
        if (in.coolant() > 0) {
            int move = Math.min(budget, Math.min(in.coolant(), Math.max(0, cap - in.water())));
            return new Amounts(in.coolant() - move, in.water() + move, in.hotWater());
        }
        int move = Math.min(budget, Math.min(in.water(), Math.max(0, cap - in.hotWater())));
        return new Amounts(in.coolant(), in.water() - move, in.hotWater() + move);
    }

    /**
     * Meltdown dump: remaining coolant and water become hot water (heat, not TNT).
     */
    public static Amounts dumpHeat(Amounts in, int tankCapacity) {
        if (in == null) {
            return new Amounts(0, 0, 0);
        }
        int cap = Math.max(1, tankCapacity);
        int hot = Math.min(cap, in.total());
        return new Amounts(0, 0, hot);
    }
}
