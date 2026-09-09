package com.mealuet.create_originium_industry.core.a11y;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.core.audio.IndustrialSoundPolicy;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.reactor.StabilityMath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Side-agnostic accessibility presentation for dust, exposure, and reactor cues.
 * <p>
 * Defaults (options off) keep the existing color-coded goggle / HUD strings.
 * {@code highContrastIndicators} restyles those lines with brighter formatting.
 * {@code nonColorAlerts} prefixes ASCII icons so critical states stay readable
 * without relying on § color codes. Audio still reuses {@code sounds.json}
 * ({@code high_dust}, {@code reactor_alarm}) and their subtitles.
 */
public final class AccessibilityCues {

    public enum CriticalState {
        HIGH_DUST,
        EXPOSURE,
        REACTOR_UNSTABLE
    }

    public static final String ICON_SAFE = "[.]";
    public static final String ICON_LOW = "[~]";
    public static final String ICON_MEDIUM = "[=]";
    public static final String ICON_HIGH = "[!]";
    public static final String ICON_CRITICAL = "[!!]";
    public static final String ICON_EXPOSURE = "[+]";
    public static final String ICON_REACTOR = "[!]";

    public static final String SUBTITLE_HIGH_DUST = "subtitles.create_originium_industry.high_dust";
    public static final String SUBTITLE_REACTOR_ALARM = "subtitles.create_originium_industry.reactor_alarm";
    public static final String SOUND_HIGH_DUST = "high_dust";
    public static final String SOUND_REACTOR_ALARM = "reactor_alarm";

    private AccessibilityCues() {}

    public static String dustIcon(DustLevel level) {
        return switch (level) {
            case SAFE -> ICON_SAFE;
            case LOW -> ICON_LOW;
            case MEDIUM -> ICON_MEDIUM;
            case HIGH -> ICON_HIGH;
            case CRITICAL -> ICON_CRITICAL;
        };
    }

    public static ChatFormatting chatFormatting(DustLevel level, boolean highContrast) {
        if (highContrast) {
            return switch (level) {
                case SAFE -> ChatFormatting.GREEN;
                case LOW -> ChatFormatting.YELLOW;
                case MEDIUM -> ChatFormatting.GOLD;
                case HIGH -> ChatFormatting.RED;
                case CRITICAL -> ChatFormatting.LIGHT_PURPLE;
            };
        }
        return switch (level) {
            case SAFE -> ChatFormatting.GREEN;
            case LOW -> ChatFormatting.YELLOW;
            case MEDIUM -> ChatFormatting.GOLD;
            case HIGH -> ChatFormatting.RED;
            case CRITICAL -> ChatFormatting.DARK_RED;
        };
    }

    public static String subtitleKey(CriticalState state) {
        return switch (state) {
            case HIGH_DUST, EXPOSURE -> SUBTITLE_HIGH_DUST;
            case REACTOR_UNSTABLE -> SUBTITLE_REACTOR_ALARM;
        };
    }

    public static String soundPath(CriticalState state) {
        return switch (state) {
            case HIGH_DUST, EXPOSURE -> SOUND_HIGH_DUST;
            case REACTOR_UNSTABLE -> SOUND_REACTOR_ALARM;
        };
    }

    public static boolean isHighDust(DustLevel level) {
        return IndustrialSoundPolicy.highDustAmbience(level);
    }

    public static Component dustRiskLabel(DustLevel level) {
        return dustRiskLabel(level, COIClientOptions.highContrastIndicators(), COIClientOptions.nonColorAlerts());
    }

    /**
     * Meter / HUD risk tag. Options off → existing colored lang key.
     */
    public static Component dustRiskLabel(DustLevel level, boolean highContrast, boolean nonColor) {
        Component name = (highContrast || nonColor)
                ? Component.translatable(level.getPlainLangKey())
                : Component.translatable(level.getLangKey());
        if (highContrast) {
            name = name.copy().withStyle(chatFormatting(level, true));
        }
        if (nonColor) {
            return Component.literal(dustIcon(level) + " ").append(name);
        }
        return name;
    }

    public static Component exposureHudText(DustLevel visual, int amplifier, boolean verbose, boolean nonColor) {
        Component text = verbose
                ? Component.translatable("hud.create_originium_industry.sickness.verbose", amplifier + 1)
                : Component.translatable("hud.create_originium_industry.sickness");
        String icon = visual == DustLevel.SAFE ? ICON_EXPOSURE : dustIcon(visual);
        return maybeIcon(nonColor, icon, text);
    }

    public static Component highDustHudText(DustLevel level, boolean nonColor) {
        Component text = Component.translatable("hud.create_originium_industry.high_dust");
        return maybeIcon(nonColor, dustIcon(level), text);
    }

    public static Component reactorHudText(boolean nonColor) {
        Component text = Component.translatable("hud.create_originium_industry.reactor_unstable");
        return maybeIcon(nonColor, ICON_REACTOR, text);
    }

    public static Component reactorWarnLabel() {
        return reactorWarnLabel(COIClientOptions.highContrastIndicators(), COIClientOptions.nonColorAlerts());
    }

    public static Component reactorWarnLabel(boolean highContrast, boolean nonColor) {
        Component text = Component.translatable("block.create_originium_industry.originium_power_core.goggle.warn");
        if (highContrast) {
            text = text.copy().withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return maybeIcon(nonColor, ICON_REACTOR, text);
    }

    public static Component reactorSignLabel(StabilityMath.Sign sign, String langKey) {
        return reactorSignLabel(sign, langKey, COIClientOptions.highContrastIndicators(), COIClientOptions.nonColorAlerts());
    }

    public static Component reactorSignLabel(
            StabilityMath.Sign sign,
            String langKey,
            boolean highContrast,
            boolean nonColor
    ) {
        Component text = Component.translatable(langKey);
        if (highContrast) {
            ChatFormatting color = switch (sign) {
                case POSITIVE -> ChatFormatting.GREEN;
                case ZERO -> ChatFormatting.YELLOW;
                case NEGATIVE -> ChatFormatting.RED;
            };
            text = text.copy().withStyle(color);
        }
        if (nonColor && sign == StabilityMath.Sign.NEGATIVE) {
            return maybeIcon(true, ICON_REACTOR, text);
        }
        return text;
    }

    public static Component maybeIcon(boolean nonColor, String icon, Component text) {
        if (!nonColor) {
            return text;
        }
        return Component.literal(icon + " ").append(text);
    }

    /**
     * HUD plate alpha. High-contrast is opaque; reduce-flicker holds a steady
     * mid alpha instead of the default pulse.
     */
    public static int indicatorBackgroundAlpha(boolean contrast, boolean reduceFlicker, int pulseAlpha) {
        if (contrast) {
            return 0xEE;
        }
        if (reduceFlicker) {
            return 0x88;
        }
        return pulseAlpha;
    }

    /**
     * MINIMAL goggle detail still surfaces critical reactor cues when the
     * non-color channel is on. Defaults stay hidden.
     */
    public static boolean showMinimalCriticalCues() {
        return COIClientOptions.nonColorAlerts();
    }

    /**
     * Quiet {@code high_dust} floor so exposure in clean air still has a
     * subtitle-bearing channel when non-color alerts are on.
     */
    public static float exposureFallbackAmbience(boolean nonColorAlerts, boolean hasSickness, float machineVolume) {
        if (!nonColorAlerts || !hasSickness || machineVolume <= 0.0F) {
            return 0.0F;
        }
        return machineVolume * 0.18F;
    }

    /**
     * True when two dust labels stay distinct after stripping Minecraft
     * section-color codes — used to prove the non-color channel.
     */
    public static boolean distinguishableWithoutColor(Component a, Component b) {
        return !stripSectionCodes(a.getString()).equals(stripSectionCodes(b.getString()));
    }

    public static String stripSectionCodes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
