package com.mealuet.create_originium_industry.core.oridust;

/**
 * Classifies chunk dust levels into human-readable risk tiers.
 * Thresholds are fixed constants to keep classification consistent
 * across config changes to exposure/effect values.
 */
public enum DustLevel {
    SAFE(0, 499, "safe", "§a"),
    LOW(500, 1499, "low", "§e"),
    MEDIUM(1500, 3999, "medium", "§6"),
    HIGH(4000, 7999, "high", "§c"),
    CRITICAL(8000, Integer.MAX_VALUE, "critical", "§4");

    private final int minDust;
    private final int maxDust;
    private final String id;
    private final String colorCode;

    DustLevel(int minDust, int maxDust, String id, String colorCode) {
        this.minDust = minDust;
        this.maxDust = maxDust;
        this.id = id;
        this.colorCode = colorCode;
    }

    /**
     * Classifies a raw dust value into a DustLevel tier.
     */
    public static DustLevel fromDust(int dust) {
        if (dust < 0) return SAFE;
        for (DustLevel level : values()) {
            if (dust >= level.minDust && dust <= level.maxDust) {
                return level;
            }
        }
        return CRITICAL;
    }

    public String getId() {
        return id;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getMinDust() {
        return minDust;
    }

    /**
     * Returns the lang key for this dust level's display name.
     * Example: "dust_level.create_originium_industry.safe"
     */
    public String getLangKey() {
        return "dust_level.create_originium_industry." + id;
    }

    /**
     * Returns a colored display string like "§aSafe" for chat messages.
     */
    public String getColoredTag(String translatedName) {
        return colorCode + translatedName + "§r";
    }
}
