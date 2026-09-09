package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Classifies chunk dust levels into human-readable risk tiers.
 * Fallback constants match the original bands; live thresholds come from
 * {@code dust_diffusion.dustLevel*} so packs can retune display without
 * changing exposure math.
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
        int low = threshold(COIConfig.DUST_LEVEL_LOW, LOW.minDust);
        int medium = threshold(COIConfig.DUST_LEVEL_MEDIUM, MEDIUM.minDust);
        int high = threshold(COIConfig.DUST_LEVEL_HIGH, HIGH.minDust);
        int critical = threshold(COIConfig.DUST_LEVEL_CRITICAL, CRITICAL.minDust);
        if (dust < low) return SAFE;
        if (dust < medium) return LOW;
        if (dust < high) return MEDIUM;
        if (dust < critical) return HIGH;
        return CRITICAL;
    }

    private static int threshold(ModConfigSpec.IntValue value, int fallback) {
        return COIConfig.COMMON_SPEC.isLoaded() ? value.get() : fallback;
    }

    public String getId() {
        return id;
    }

    public String getColorCode() {
        return colorCode;
    }

    /**
     * Chat/HUD color. High-contrast client option uses brighter primaries.
     */
    public int getArgb(boolean highContrast) {
        if (highContrast) {
            return switch (this) {
                case SAFE -> 0xFF00FF00;
                case LOW -> 0xFFFFFF00;
                case MEDIUM -> 0xFFFF9900;
                case HIGH -> 0xFFFF3333;
                case CRITICAL -> 0xFFFF00FF;
            };
        }
        return switch (this) {
            case SAFE -> 0xFF55FF55;
            case LOW -> 0xFFFFFF55;
            case MEDIUM -> 0xFFFFAA00;
            case HIGH -> 0xFFFF5555;
            case CRITICAL -> 0xFFAA0000;
        };
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
     * Unstyled tag for high-contrast / non-color goggle and HUD restyling.
     * The original {@link #getLangKey()} keeps baked-in § codes for defaults.
     */
    public String getPlainLangKey() {
        return getLangKey() + ".plain";
    }

    /**
     * Returns a colored display string like "§aSafe" for chat messages.
     */
    public String getColoredTag(String translatedName) {
        return colorCode + translatedName + "§r";
    }
}
