package com.mealuet.create_originium_industry.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.advancement.COIAdvancements;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustPurification;
import com.mealuet.create_originium_industry.core.oridust.PurificationResult;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COICriteria;
import com.mealuet.create_originium_industry.index.COIItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Coverage for issues #20 and #46: bilingual backbone advancement tree
 * plus infection / protection / alloy / meltdown milestones.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class AdvancementGameTests {

    private static final List<String> TREE = List.of(
            "root",
            "obtain_raw_originium",
            "dust_exposure",
            "dust_purified",
            "start_power_core",
            "obtain_protection",
            "obtain_alloy",
            "infection_stage",
            "power_core_meltdown"
    );

    private AdvancementGameTests() {}

    @GameTest(template = "empty", batch = "advancements")
    public static void backboneTreeIsLoaded(GameTestHelper helper) {
        AdvancementHolder root = advancement(helper, "root");
        helper.assertTrue(root.value().parent().isEmpty(), "root has no parent");
        helper.assertTrue(root.value().display().isPresent(), "root display");
        helper.assertTrue(root.value().display().orElseThrow().getBackground().isPresent(), "tab background");

        assertParent(helper, "obtain_raw_originium", "root");
        assertParent(helper, "dust_exposure", "obtain_raw_originium");
        assertParent(helper, "dust_purified", "dust_exposure");
        assertParent(helper, "start_power_core", "dust_purified");
        assertParent(helper, "obtain_alloy", "obtain_raw_originium");
        assertParent(helper, "obtain_protection", "dust_exposure");
        assertParent(helper, "infection_stage", "dust_exposure");
        assertParent(helper, "power_core_meltdown", "start_power_core");

        helper.assertValueEqual(
                advancement(helper, "start_power_core").value().display().orElseThrow().getType(),
                AdvancementType.GOAL,
                "power core is a goal"
        );
        helper.assertValueEqual(
                advancement(helper, "power_core_meltdown").value().display().orElseThrow().getType(),
                AdvancementType.CHALLENGE,
                "meltdown is a challenge"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void obtainRawOriginiumUsesInventoryChanged(GameTestHelper helper) {
        AdvancementHolder holder = advancement(helper, "obtain_raw_originium");
        helper.assertTrue(holder.value().criteria().containsKey("has_raw_originium"), "item criterion");
        helper.assertTrue(holder.value().criteria().containsKey("has_stone_ore"), "stone ore criterion");
        helper.assertTrue(holder.value().criteria().containsKey("has_deepslate_ore"), "deepslate ore criterion");
        for (Criterion<?> criterion : holder.value().criteria().values()) {
            helper.assertTrue(criterion.trigger() == CriteriaTriggers.INVENTORY_CHANGED, "vanilla pickup");
        }
        helper.assertTrue(
                advancement(helper, "obtain_raw_originium").value().display().orElseThrow().getIcon()
                        .is(COIItems.RAW_ORIGINIUM.get()),
                "raw originium icon"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void customTriggersAreRegisteredAndWired(GameTestHelper helper) {
        helper.assertTrue(
                BuiltInRegistries.TRIGGER_TYPES.containsKey(id("dust_exposure")),
                "dust_exposure trigger"
        );
        helper.assertTrue(
                BuiltInRegistries.TRIGGER_TYPES.containsKey(id("dust_purified")),
                "dust_purified trigger"
        );
        helper.assertTrue(
                BuiltInRegistries.TRIGGER_TYPES.containsKey(id("power_core_started")),
                "power_core_started trigger"
        );
        helper.assertTrue(
                BuiltInRegistries.TRIGGER_TYPES.containsKey(id("infection_stage")),
                "infection_stage trigger"
        );
        helper.assertTrue(
                BuiltInRegistries.TRIGGER_TYPES.containsKey(id("power_core_meltdown")),
                "power_core_meltdown trigger"
        );

        helper.assertTrue(
                criterionTrigger(helper, "dust_exposure", "exposed") == COICriteria.DUST_EXPOSURE.get(),
                "exposure criterion"
        );
        helper.assertTrue(
                criterionTrigger(helper, "dust_purified", "purified") == COICriteria.DUST_PURIFIED.get(),
                "purify criterion"
        );
        helper.assertTrue(
                criterionTrigger(helper, "start_power_core", "started") == COICriteria.POWER_CORE_STARTED.get(),
                "core criterion"
        );
        helper.assertTrue(
                criterionTrigger(helper, "infection_stage", "infected") == COICriteria.INFECTION_STAGE.get(),
                "infection criterion"
        );
        helper.assertTrue(
                criterionTrigger(helper, "power_core_meltdown", "melted") == COICriteria.POWER_CORE_MELTDOWN.get(),
                "meltdown criterion"
        );

        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        COIAdvancements.dustPurified(level, pos);
        COIAdvancements.powerCoreStarted(level, pos);
        COIAdvancements.powerCoreMeltdown(level, pos);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void obtainAlloyAndProtectionUseInventoryChanged(GameTestHelper helper) {
        AdvancementHolder alloy = advancement(helper, "obtain_alloy");
        helper.assertTrue(alloy.value().criteria().containsKey("has_alloy"), "alloy criterion");
        helper.assertTrue(
                alloy.value().criteria().get("has_alloy").trigger() == CriteriaTriggers.INVENTORY_CHANGED,
                "alloy pickup"
        );
        helper.assertTrue(
                alloy.value().display().orElseThrow().getIcon().is(COIItems.ORIGINIUM_ALLOY_INGOT.get()),
                "alloy icon"
        );

        AdvancementHolder protection = advancement(helper, "obtain_protection");
        helper.assertTrue(protection.value().criteria().containsKey("has_protection"), "protection criterion");
        helper.assertTrue(
                protection.value().criteria().get("has_protection").trigger() == CriteriaTriggers.INVENTORY_CHANGED,
                "protection pickup"
        );
        helper.assertTrue(
                protection.value().display().orElseThrow().getIcon().is(COIItems.ORIGINIUM_RESPIRATOR.get()),
                "respirator icon"
        );

        JsonObject alloyJson = readAdvancement("obtain_alloy");
        helper.assertValueEqual(
                alloyJson.getAsJsonObject("criteria").getAsJsonObject("has_alloy")
                        .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                        .getAsJsonObject().get("items").getAsString(),
                "create_originium_industry:originium_alloy_ingot",
                "alloy item id"
        );
        JsonObject protectionJson = readAdvancement("obtain_protection");
        helper.assertValueEqual(
                protectionJson.getAsJsonObject("criteria").getAsJsonObject("has_protection")
                        .getAsJsonObject("conditions").getAsJsonArray("items").get(0)
                        .getAsJsonObject().get("items").getAsString(),
                "#create_originium_industry:originium_protection",
                "protection tag"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void meltdownHookRunsWhenCoreDumps(GameTestHelper helper) {
        BlockPos coreRel = new BlockPos(2, 1, 1);
        helper.setBlock(coreRel, COIBlocks.POWER_CORE.getDefaultState());
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.CORE_HOUSING.getDefaultState());
        PowerCoreBlockEntity core = helper.getBlockEntity(coreRel);
        helper.assertTrue(core != null, "power core BE");
        core.configureForGameTest(1, 0, 0, 0, COIConfig.reactorMeltdownThreshold());
        core.triggerMeltdown(helper.getLevel());
        helper.assertTrue(core.shutdown(), "meltdown sets shutdown");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void infectionStageFiresOnlyOnFirstSymptomCrossing(GameTestHelper helper) {
        helper.assertFalse(COIAdvancements.crossedIntoInfectionStage(0, 199), "below weakness");
        helper.assertTrue(COIAdvancements.crossedIntoInfectionStage(199, 200), "weakness floor");
        helper.assertTrue(COIAdvancements.crossedIntoInfectionStage(0, 800), "skip into restricted");
        helper.assertFalse(COIAdvancements.crossedIntoInfectionStage(200, 800), "already symptomatic");
        helper.assertFalse(COIAdvancements.crossedIntoInfectionStage(800, 100), "recovery does not re-grant");
        helper.assertFalse(COIAdvancements.crossedIntoInfectionStage(0, 0), "still clean");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void successfulSieveCaptureAwardsPurifyTrigger(GameTestHelper helper) {
        BlockPos sieveRel = new BlockPos(1, 1, 1);
        helper.setBlock(sieveRel, COIBlocks.DUST_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));
        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "sieve BE");
        sieve.activatePurifierForGameTest();

        BlockPos emit = helper.absolutePos(new BlockPos(2, 1, 1));
        PurificationResult result = DustPurification.reduceNearby(helper.getLevel(), emit, 80);
        helper.assertTrue(result.captured() > 0, "sieve captured emission");
        helper.assertTrue(result.conservesDust(), "no dup/void");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "advancements")
    public static void displayKeysAreBilingualAndLockstep(GameTestHelper helper) {
        JsonObject en = readLang("en_us");
        JsonObject zh = readLang("zh_cn");
        helper.assertValueEqual(en.size(), zh.size(), "en_us/zh_cn key count");
        for (String key : en.keySet()) {
            helper.assertTrue(zh.has(key), "zh_cn missing " + key);
        }
        for (String key : zh.keySet()) {
            helper.assertTrue(en.has(key), "en_us missing " + key);
        }

        for (String path : TREE) {
            AdvancementHolder holder = advancement(helper, path);
            var display = holder.value().display().orElseThrow();
            String titleKey = translatableKey(display.getTitle());
            String descKey = translatableKey(display.getDescription());
            helper.assertValueEqual(titleKey, "advancements.create_originium_industry." + path + ".title", path + " title");
            helper.assertValueEqual(descKey, "advancements.create_originium_industry." + path + ".description", path + " desc");
            helper.assertTrue(en.has(titleKey) && zh.has(titleKey), path + " title lockstep");
            helper.assertTrue(en.has(descKey) && zh.has(descKey), path + " description lockstep");
            helper.assertFalse(en.get(titleKey).getAsString().isBlank(), path + " en title");
            helper.assertFalse(zh.get(titleKey).getAsString().isBlank(), path + " zh title");
        }

        Set<String> required = Set.of(
                "advancements.create_originium_industry.root.title",
                "advancements.create_originium_industry.obtain_raw_originium.title",
                "advancements.create_originium_industry.dust_exposure.title",
                "advancements.create_originium_industry.dust_purified.title",
                "advancements.create_originium_industry.start_power_core.title",
                "advancements.create_originium_industry.obtain_protection.title",
                "advancements.create_originium_industry.obtain_alloy.title",
                "advancements.create_originium_industry.infection_stage.title",
                "advancements.create_originium_industry.power_core_meltdown.title"
        );
        for (String key : required) {
            helper.assertTrue(en.has(key) && zh.has(key), key);
        }
        helper.succeed();
    }

    private static void assertParent(GameTestHelper helper, String child, String parent) {
        helper.assertTrue(
                advancement(helper, child).value().parent().equals(Optional.of(id(parent))),
                child + " parent"
        );
    }

    private static AdvancementHolder advancement(GameTestHelper helper, String path) {
        AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(id(path));
        helper.assertTrue(holder != null, "missing advancement " + path);
        return holder;
    }

    private static Object criterionTrigger(GameTestHelper helper, String path, String criterion) {
        Criterion<?> value = advancement(helper, path).value().criteria().get(criterion);
        helper.assertTrue(value != null, path + " missing " + criterion);
        return value.trigger();
    }

    private static String translatableKey(net.minecraft.network.chat.Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        throw new IllegalStateException("expected translatable component, got " + component.getContents());
    }

    private static JsonObject readLang(String file) {
        return readJson("/assets/create_originium_industry/lang/" + file + ".json");
    }

    private static JsonObject readAdvancement(String path) {
        return readJson("/data/create_originium_industry/advancement/" + path + ".json");
    }

    private static JsonObject readJson(String resource) {
        try (InputStream in = AdvancementGameTests.class.getResourceAsStream(resource)) {
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
