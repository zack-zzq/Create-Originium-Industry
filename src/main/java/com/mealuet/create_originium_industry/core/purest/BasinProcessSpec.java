package com.mealuet.create_originium_industry.core.purest;

/**
 * Extra basin-mixer requirements for a datapack recipe id.
 * Create mixing JSON cannot express sieve / cooling-chamber / no-heat gates,
 * so those live beside the recipe under {@code coi_basin_process}.
 */
public record BasinProcessSpec(boolean requireSieve, boolean requireCoolingChamber, boolean rejectHeat) {

    public static final BasinProcessSpec NONE = new BasinProcessSpec(false, false, false);

    public boolean hasRequirements() {
        return requireSieve || requireCoolingChamber || rejectHeat;
    }
}
