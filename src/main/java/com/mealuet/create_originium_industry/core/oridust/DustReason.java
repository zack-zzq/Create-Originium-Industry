package com.mealuet.create_originium_industry.core.oridust;

/**
 * Enumerates the reasons why dust was added to or removed from a chunk.
 * Used for debug logging and future analytics.
 */
public enum DustReason {
    /** Debug command or debug wand */
    DEBUG("debug"),
    /** Natural diffusion between chunks */
    DIFFUSION("diffusion"),
    /** Natural decay over time */
    DECAY("decay"),
    /** Create machine processing (crushing, milling, mixing, etc.) */
    MACHINE_PROCESSING("machine_processing"),
    /** Reactor operation or meltdown */
    REACTOR("reactor"),
    /** Player death dust burst */
    DEATH_BURST("death_burst"),
    /** Dust filter absorption */
    FILTER("filter"),
    /** Dust diffuser active removal */
    DIFFUSER("diffuser"),
    /** Unknown or unspecified */
    UNKNOWN("unknown");

    private final String id;

    DustReason(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
