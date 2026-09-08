package com.mealuet.create_originium_industry.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.core.audio.IndustrialSoundPolicy;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.DustPurification;
import com.mealuet.create_originium_industry.core.oridust.PurificationResult;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COISounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Coverage for issue #23: industrial SFX, sounds.json, subtitles, and
 * client-config density / reduce-flicker wiring.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class AudioGameTests {

    private static final List<String> EVENTS = List.of(
            "filter_work",
            "high_dust",
            "reactor_steady",
            "reactor_alarm"
    );

    private AudioGameTests() {}

    @GameTest(template = "empty", batch = "audio")
    public static void soundEventsAreRegistered(GameTestHelper helper) {
        for (String path : EVENTS) {
            helper.assertTrue(
                    BuiltInRegistries.SOUND_EVENT.containsKey(id(path)),
                    "missing sound " + path
            );
        }
        helper.assertTrue(COISounds.FILTER_WORK.get() != null, "filter holder");
        helper.assertTrue(COISounds.HIGH_DUST.get() != null, "dust holder");
        helper.assertTrue(COISounds.REACTOR_STEADY.get() != null, "steady holder");
        helper.assertTrue(COISounds.REACTOR_ALARM.get() != null, "alarm holder");
        helper.assertValueEqual(
                COISounds.FILTER_WORK.getId(),
                id("filter_work"),
                "filter registry path"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "audio")
    public static void soundsJsonAndOggPlaceholdersLoad(GameTestHelper helper) {
        JsonObject root = readJson("/assets/create_originium_industry/sounds.json");
        helper.assertValueEqual(root.size(), EVENTS.size(), "sounds.json event count");
        for (String path : EVENTS) {
            helper.assertTrue(root.has(path), "sounds.json missing " + path);
            JsonObject event = root.getAsJsonObject(path);
            helper.assertTrue(event.has("subtitle"), path + " subtitle");
            helper.assertValueEqual(
                    event.get("subtitle").getAsString(),
                    "subtitles.create_originium_industry." + path,
                    path + " subtitle key"
            );
            helper.assertTrue(event.has("sounds"), path + " sounds array");
            JsonElement first = event.getAsJsonArray("sounds").get(0);
            String name = first.isJsonObject()
                    ? first.getAsJsonObject().get("name").getAsString()
                    : first.getAsString();
            helper.assertValueEqual(name, CreateOriginiumIndustry.MODID + ":" + path, path + " ogg name");
            String ogg = "/assets/create_originium_industry/sounds/" + path + ".ogg";
            try (InputStream in = AudioGameTests.class.getResourceAsStream(ogg)) {
                helper.assertTrue(in != null, "missing " + ogg);
            } catch (IOException e) {
                throw new IllegalStateException(ogg, e);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "audio")
    public static void subtitleKeysAreBilingualAndLockstep(GameTestHelper helper) {
        JsonObject en = readJson("/assets/create_originium_industry/lang/en_us.json");
        JsonObject zh = readJson("/assets/create_originium_industry/lang/zh_cn.json");
        helper.assertValueEqual(en.size(), zh.size(), "en_us/zh_cn key count");
        for (String key : en.keySet()) {
            helper.assertTrue(zh.has(key), "zh_cn missing " + key);
        }
        for (String key : zh.keySet()) {
            helper.assertTrue(en.has(key), "en_us missing " + key);
        }
        Set<String> required = Set.of(
                "subtitles.create_originium_industry.filter_work",
                "subtitles.create_originium_industry.high_dust",
                "subtitles.create_originium_industry.reactor_steady",
                "subtitles.create_originium_industry.reactor_alarm"
        );
        for (String key : required) {
            helper.assertTrue(en.has(key) && zh.has(key), key);
            helper.assertFalse(en.get(key).getAsString().isBlank(), key + " en");
            helper.assertFalse(zh.get(key).getAsString().isBlank(), key + " zh");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "audio")
    public static void industrialCuesFollowConfigAndState(GameTestHelper helper) {
        helper.assertFalse(IndustrialSoundPolicy.filterWorking(false, 64f), "no sieve");
        helper.assertFalse(IndustrialSoundPolicy.filterWorking(true, 0f), "no RPM");
        helper.assertTrue(IndustrialSoundPolicy.filterWorking(true, 32f), "spinning filter");

        helper.assertFalse(IndustrialSoundPolicy.highDustAmbience(DustLevel.SAFE), "safe");
        helper.assertFalse(IndustrialSoundPolicy.highDustAmbience(DustLevel.MEDIUM), "medium");
        helper.assertTrue(IndustrialSoundPolicy.highDustAmbience(DustLevel.HIGH), "high");
        helper.assertTrue(IndustrialSoundPolicy.highDustAmbience(DustLevel.CRITICAL), "critical");
        helper.assertValueEqual(IndustrialSoundPolicy.dustAmbienceGain(DustLevel.HIGH), 0.55f, "high gain");
        helper.assertValueEqual(IndustrialSoundPolicy.dustAmbienceGain(DustLevel.CRITICAL), 1.0f, "critical gain");

        helper.assertValueEqual(
                IndustrialSoundPolicy.reactorCue(false, 1500, 0),
                IndustrialSoundPolicy.ReactorCue.NONE,
                "idle core"
        );
        helper.assertValueEqual(
                IndustrialSoundPolicy.reactorCue(true, 1500, 0),
                IndustrialSoundPolicy.ReactorCue.STEADY,
                "stable core"
        );
        helper.assertValueEqual(
                IndustrialSoundPolicy.reactorCue(true, -2500, 0),
                IndustrialSoundPolicy.ReactorCue.ALARM,
                "S<0 alarm"
        );
        helper.assertValueEqual(
                IndustrialSoundPolicy.reactorCue(true, 1500, 50),
                IndustrialSoundPolicy.ReactorCue.ALARM,
                "instability warning"
        );

        helper.assertTrue(IndustrialSoundPolicy.shouldPlayCaptureOneShot(20, 0, 20), "interval elapsed");
        helper.assertFalse(IndustrialSoundPolicy.shouldPlayCaptureOneShot(19, 0, 20), "too soon");

        helper.assertTrue(COIClientOptions.industrialSoundsEnabled(), "enabled fallback");
        helper.assertValueEqual(COIClientOptions.ambientDustSoundVolume(), 0.4F, "particleDensity scales dust SFX");
        helper.assertTrue(COIClientOptions.pulseAudio(), "reduceFlicker off → pulse");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "audio")
    public static void sieveCapturePlaysFilterWorkWithoutThrowing(GameTestHelper helper) {
        BlockPos sieveRel = new BlockPos(1, 1, 1);
        helper.setBlock(sieveRel, COIBlocks.DUST_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));
        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "sieve BE");
        sieve.activatePurifierForGameTest();

        BlockPos emit = helper.absolutePos(new BlockPos(2, 1, 1));
        PurificationResult result = DustPurification.reduceNearby(helper.getLevel(), emit, 80);
        helper.assertTrue(result.captured() > 0, "sieve captured");
        COISounds.playFilterWork(helper.getLevel(), helper.absolutePos(sieveRel));
        helper.succeed();
    }

    private static JsonObject readJson(String resource) {
        try (InputStream in = AudioGameTests.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing " + resource);
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(resource, e);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }
}
