package com.mealuet.create_originium_industry.core.oridust;

/**
 * Outcome of running one or more {@link IDustPurifier}s against an incoming
 * emission. Dust conservation: {@code remaining + captured == incoming}.
 */
public record PurificationResult(int incoming, int remaining, int captured, int byproductItems) {

    public static PurificationResult unchanged(int incoming) {
        int safe = Math.max(0, incoming);
        return new PurificationResult(safe, safe, 0, 0);
    }

    /**
     * {@code remaining + captured == incoming} (amounts are clamped to ≥ 0).
     */
    public boolean conservesDust() {
        return remaining >= 0 && captured >= 0 && remaining + captured == incoming;
    }
}
