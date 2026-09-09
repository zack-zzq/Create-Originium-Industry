package com.mealuet.create_originium_industry.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustMeterBlockEntity;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.core.a11y.AccessibilityCues;
import com.mealuet.create_originium_industry.core.audio.IndustrialSoundPolicy;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.reactor.StabilityMath;
import com.mealuet.create_originium_industry.index.COIBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Coverage for issue #60: CLIENT accessibility knobs, non-color critical-state
 * cues, and goggle / HUD / particle defaults that preserve current feel.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class AccessibilityGameTests {

    private AccessibilityGameTests() {}

    @GameTest(template = "empty", batch = "a11y")
    public static void defaultsPreserveCurrentFeel(GameTestHelper helper) {
        helper.assertFalse(COIClientOptions.reduceFlicker(), "reduceFlicker off");
        helper.assertFalse(COIClientOptions.highContrastIndicators(), "highContrast off");
        helper.assertFalse(COIClientOptions.nonColorAlerts(), "nonColor off");
        helper.assertFalse(COIClientOptions.simplifyParticles(), "simplify off");
        helper.assertValueEqual(COIClientOptions.particleDensity(), 0.4, "particle density");
        helper.assertValueEqual(COIClientOptions.particleCount(10), 4, "0.4 * 10");
        helper.assertValueEqual(COIClientOptions.particlePeriod(), 4, "spawn every 4 ticks");
        helper.assertTrue(COIClientOptions.pulseAudio(), "alarm still pulses");
        helper.assertValueEqual(
                AccessibilityCues.indicatorBackgroundAlpha(false, false, 0x55),
                0x55,
                "HUD still pulses"
        );

        Component risk = AccessibilityCues.dustRiskLabel(DustLevel.HIGH, false, false);
        helper.assertTrue(hasKey(risk, DustLevel.HIGH.getLangKey()), "default uses colored lang key");
        helper.assertFalse(risk.getString().contains(AccessibilityCues.ICON_HIGH), "default has no icon");

        Component warn = AccessibilityCues.reactorWarnLabel(false, false);
        helper.assertTrue(
                hasKey(warn, "block.create_originium_industry.originium_power_core.goggle.warn"),
                "default warn key"
        );
        helper.assertFalse(warn.getString().contains(AccessibilityCues.ICON_REACTOR), "default warn has no icon");

        Component exposure = AccessibilityCues.exposureHudText(DustLevel.CRITICAL, 2, false, false);
        helper.assertTrue(hasKey(exposure, "hud.create_originium_industry.sickness"), "default sickness HUD");
        helper.assertFalse(exposure.getString().contains(AccessibilityCues.ICON_CRITICAL), "default HUD has no icon");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void highContrastAndReduceFlickerChangeIndicators(GameTestHelper helper) {
        helper.assertValueEqual(
                DustLevel.CRITICAL.getArgb(false),
                0xFFAA0000,
                "default critical ARGB"
        );
        helper.assertValueEqual(
                DustLevel.CRITICAL.getArgb(true),
                0xFFFF00FF,
                "high-contrast critical is magenta, not dark red"
        );
        helper.assertValueEqual(
                AccessibilityCues.chatFormatting(DustLevel.CRITICAL, true),
                ChatFormatting.LIGHT_PURPLE,
                "goggle contrast formatting"
        );
        helper.assertValueEqual(
                AccessibilityCues.indicatorBackgroundAlpha(true, false, 0x44),
                0xEE,
                "opaque contrast plate"
        );
        helper.assertValueEqual(
                AccessibilityCues.indicatorBackgroundAlpha(false, true, 0x44),
                0x88,
                "steady reduce-flicker plate"
        );

        Component contrast = AccessibilityCues.dustRiskLabel(DustLevel.HIGH, true, false);
        helper.assertTrue(hasKey(contrast, DustLevel.HIGH.getPlainLangKey()), "contrast uses plain name");
        helper.assertValueEqual(
                contrast.getStyle().getColor() != null ? contrast.getStyle().getColor().getValue() : -1,
                ChatFormatting.RED.getColor(),
                "contrast applies bright red"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void nonColorIconsDistinguishCriticalStates(GameTestHelper helper) {
        Component high = AccessibilityCues.dustRiskLabel(DustLevel.HIGH, false, true);
        Component critical = AccessibilityCues.dustRiskLabel(DustLevel.CRITICAL, false, true);
        helper.assertTrue(high.getString().contains(AccessibilityCues.ICON_HIGH), "high icon");
        helper.assertTrue(critical.getString().contains(AccessibilityCues.ICON_CRITICAL), "critical icon");
        helper.assertTrue(
                AccessibilityCues.distinguishableWithoutColor(high, critical),
                "high vs critical without § codes"
        );
        helper.assertTrue(hasKey(high, DustLevel.HIGH.getPlainLangKey()), "icon path uses plain name");

        Component exposure = AccessibilityCues.exposureHudText(DustLevel.CRITICAL, 3, false, true);
        helper.assertTrue(exposure.getString().contains(AccessibilityCues.ICON_CRITICAL), "exposure severity icon");

        Component reactor = AccessibilityCues.reactorWarnLabel(false, true);
        helper.assertTrue(reactor.getString().startsWith(AccessibilityCues.ICON_REACTOR + " "), "reactor icon");

        Component unstable = AccessibilityCues.reactorSignLabel(
                StabilityMath.Sign.NEGATIVE,
                "block.create_originium_industry.originium_power_core.sign.negative",
                false,
                true
        );
        Component stable = AccessibilityCues.reactorSignLabel(
                StabilityMath.Sign.POSITIVE,
                "block.create_originium_industry.originium_power_core.sign.positive",
                false,
                true
        );
        helper.assertTrue(unstable.getString().contains(AccessibilityCues.ICON_REACTOR), "unstable icon");
        helper.assertFalse(stable.getString().contains(AccessibilityCues.ICON_REACTOR + " "), "stable has no alarm icon");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void criticalStatesReuseExistingSoundsAndSubtitles(GameTestHelper helper) {
        helper.assertValueEqual(
                AccessibilityCues.soundPath(AccessibilityCues.CriticalState.HIGH_DUST),
                AccessibilityCues.SOUND_HIGH_DUST,
                "high dust sound"
        );
        helper.assertValueEqual(
                AccessibilityCues.soundPath(AccessibilityCues.CriticalState.EXPOSURE),
                AccessibilityCues.SOUND_HIGH_DUST,
                "exposure reuses high_dust"
        );
        helper.assertValueEqual(
                AccessibilityCues.soundPath(AccessibilityCues.CriticalState.REACTOR_UNSTABLE),
                AccessibilityCues.SOUND_REACTOR_ALARM,
                "reactor sound"
        );
        helper.assertValueEqual(
                AccessibilityCues.subtitleKey(AccessibilityCues.CriticalState.HIGH_DUST),
                AccessibilityCues.SUBTITLE_HIGH_DUST,
                "high dust subtitle"
        );
        helper.assertValueEqual(
                AccessibilityCues.subtitleKey(AccessibilityCues.CriticalState.EXPOSURE),
                AccessibilityCues.SUBTITLE_HIGH_DUST,
                "exposure subtitle"
        );
        helper.assertValueEqual(
                AccessibilityCues.subtitleKey(AccessibilityCues.CriticalState.REACTOR_UNSTABLE),
                AccessibilityCues.SUBTITLE_REACTOR_ALARM,
                "reactor subtitle"
        );

        helper.assertTrue(AccessibilityCues.isHighDust(DustLevel.HIGH), "high is critical dust");
        helper.assertTrue(AccessibilityCues.isHighDust(DustLevel.CRITICAL), "critical is critical dust");
        helper.assertFalse(AccessibilityCues.isHighDust(DustLevel.MEDIUM), "medium is not");

        helper.assertValueEqual(
                IndustrialSoundPolicy.reactorCue(true, -10, 0),
                IndustrialSoundPolicy.ReactorCue.ALARM,
                "S<0 still alarms"
        );
        helper.assertValueEqual(
                AccessibilityCues.exposureFallbackAmbience(false, true, 1.0F),
                0.0F,
                "no fallback when option off"
        );
        helper.assertValueEqual(
                AccessibilityCues.exposureFallbackAmbience(true, true, 1.0F),
                0.18F,
                "exposure fallback reuses high_dust"
        );
        helper.assertValueEqual(
                AccessibilityCues.exposureFallbackAmbience(true, false, 1.0F),
                0.0F,
                "no sickness → no fallback"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void particleKnobsScaleDustFog(GameTestHelper helper) {
        helper.assertValueEqual(COIClientOptions.particleCount(0), 0, "no base → none");
        helper.assertValueEqual(COIClientOptions.particleCount(6), 2, "CRITICAL base 6 * 0.4");
        helper.assertValueEqual(COIClientOptions.particlePeriod(), 4, "default period");
        helper.assertFalse(COIClientOptions.simplifyParticles(), "simplify off by default");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void bilingualA11yKeysLockstep(GameTestHelper helper) {
        JsonObject en = readJson("/assets/create_originium_industry/lang/en_us.json");
        JsonObject zh = readJson("/assets/create_originium_industry/lang/zh_cn.json");
        helper.assertValueEqual(en.size(), zh.size(), "en_us/zh_cn key count");
        for (String key : en.keySet()) {
            helper.assertTrue(zh.has(key), "zh_cn missing " + key);
        }
        Set<String> required = Set.of(
                "hud.create_originium_industry.high_dust",
                "hud.create_originium_industry.reactor_unstable",
                "dust_level.create_originium_industry.safe.plain",
                "dust_level.create_originium_industry.low.plain",
                "dust_level.create_originium_industry.medium.plain",
                "dust_level.create_originium_industry.high.plain",
                "dust_level.create_originium_industry.critical.plain",
                AccessibilityCues.SUBTITLE_HIGH_DUST,
                AccessibilityCues.SUBTITLE_REACTOR_ALARM
        );
        for (String key : required) {
            helper.assertTrue(en.has(key) && zh.has(key), key);
            helper.assertFalse(en.get(key).getAsString().isBlank(), key + " en");
            helper.assertFalse(zh.get(key).getAsString().isBlank(), key + " zh");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "a11y")
    public static void meterAndReactorGogglesDefaultUnchanged(GameTestHelper helper) {
        BlockPos meterRel = new BlockPos(1, 1, 1);
        helper.setBlock(meterRel, COIBlocks.DUST_METER.getDefaultState());
        DustMeterBlockEntity meter = helper.getBlockEntity(meterRel);
        helper.assertTrue(meter != null, "meter BE");
        ChunkPos chunk = WorldSpace.toDustChunk(helper.getLevel(), helper.absolutePos(meterRel));
        OriginiumDustManager.setDust(helper.getLevel(), chunk, 4000, DustReason.DEBUG);
        meter.refreshFromServer();
        helper.assertValueEqual(meter.risk(), DustLevel.HIGH, "high dust");

        List<Component> meterTip = new ArrayList<>();
        meter.addToGoggleTooltip(meterTip, false);
        helper.assertTrue(containsKey(meterTip, DustLevel.HIGH.getLangKey()), "meter still uses colored risk key");
        helper.assertFalse(containsText(meterTip, AccessibilityCues.ICON_HIGH + " "), "meter default has no icon");

        helper.setBlock(new BlockPos(2, 1, 1), COIBlocks.POWER_CORE.getDefaultState());
        PowerCoreBlockEntity core = helper.getBlockEntity(new BlockPos(2, 1, 1));
        helper.assertTrue(core != null, "core BE");
        List<Component> coreTip = new ArrayList<>();
        core.addToGoggleTooltip(coreTip, false);
        helper.assertTrue(
                containsKey(coreTip, "block.create_originium_industry.originium_power_core.sign.positive")
                        || containsKey(coreTip, "block.create_originium_industry.originium_power_core.sign.zero")
                        || containsKey(coreTip, "block.create_originium_industry.originium_power_core.sign.negative"),
                "core still shows S sign"
        );
        helper.assertFalse(containsText(coreTip, AccessibilityCues.ICON_REACTOR + " "), "core default has no icon");
        helper.succeed();
    }

    private static boolean containsKey(List<Component> lines, String key) {
        for (Component line : lines) {
            if (hasKey(line, key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsText(List<Component> lines, String fragment) {
        for (Component line : lines) {
            if (line.getString().contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasKey(Component component, String key) {
        if (component.getContents() instanceof TranslatableContents contents && key.equals(contents.getKey())) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (hasKey(sibling, key)) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject readJson(String resource) {
        try (InputStream in = AccessibilityGameTests.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing " + resource);
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(resource, e);
        }
    }
}
