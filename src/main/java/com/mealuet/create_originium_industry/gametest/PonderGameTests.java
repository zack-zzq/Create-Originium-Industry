package com.mealuet.create_originium_industry.gametest;

import com.google.gson.JsonParser;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.client.ponder.COIPonderPlugin;
import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Coverage for issue #19: Ponder scenes for dust, filter, supercooling, and
 * the power core. Copy is {@code Component.translatable}; zh_cn / en_us lockstep.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class PonderGameTests {

    private PonderGameTests() {}

    @GameTest(template = "empty", batch = "ponder")
    public static void fourScenesAndPlugin(GameTestHelper helper) {
        helper.assertValueEqual(COIPonderKeys.SCENE_IDS.size(), 4, "four ponder scenes");
        helper.assertTrue(COIPonderKeys.SCENE_IDS.contains(COIPonderKeys.DUST_GENERATION), "dust generation");
        helper.assertTrue(COIPonderKeys.SCENE_IDS.contains(COIPonderKeys.DUST_FILTER), "filter recovery");
        helper.assertTrue(COIPonderKeys.SCENE_IDS.contains(COIPonderKeys.PUREST_SUPERCOOLING), "supercooling");
        helper.assertTrue(COIPonderKeys.SCENE_IDS.contains(COIPonderKeys.POWER_CORE), "power core");
        helper.assertValueEqual(new COIPonderPlugin().getModId(), CreateOriginiumIndustry.MODID, "plugin modid");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ponder")
    public static void copyGoesThroughTranslatable(GameTestHelper helper) {
        Language language = Language.getInstance();
        for (String key : COIPonderKeys.ALL) {
            var component = COIPonderKeys.component(key);
            helper.assertTrue(
                    component.getContents() instanceof TranslatableContents,
                    key + " must use Component.translatable");
            helper.assertTrue(language.has(key), "en_us loaded " + key);
            String resolved = COIPonderKeys.copy(key);
            helper.assertFalse(resolved.equals(key), "en_us must translate " + key);
            helper.assertFalse(resolved.isBlank(), key + " is not blank");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ponder")
    public static void langFilesLockstep(GameTestHelper helper) throws Exception {
        Set<String> en = loadLangKeys("en_us");
        Set<String> zh = loadLangKeys("zh_cn");
        Set<String> onlyEn = new HashSet<>(en);
        onlyEn.removeAll(zh);
        Set<String> onlyZh = new HashSet<>(zh);
        onlyZh.removeAll(en);
        helper.assertTrue(onlyEn.isEmpty(), "zh_cn missing " + onlyEn);
        helper.assertTrue(onlyZh.isEmpty(), "en_us missing " + onlyZh);
        helper.assertTrue(en.containsAll(COIPonderKeys.ALL), "ponder keys present in en_us");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ponder")
    public static void schematicResourcesExist(GameTestHelper helper) throws Exception {
        helper.assertValueEqual(COIPonderKeys.SCHEMATICS.size(), 4, "four schematics");
        for (String name : COIPonderKeys.SCHEMATICS) {
            String path = "/assets/create_originium_industry/ponder/" + name;
            try (InputStream in = PonderGameTests.class.getResourceAsStream(path)) {
                helper.assertTrue(in != null, "missing " + path);
                int b1 = in.read();
                int b2 = in.read();
                helper.assertValueEqual(b1, 0x1f, name + " gzip 1");
                helper.assertValueEqual(b2, 0x8b, name + " gzip 2");
            }
        }
        helper.succeed();
    }

    private static Set<String> loadLangKeys(String locale) throws Exception {
        String path = "/assets/create_originium_industry/lang/" + locale + ".json";
        try (InputStream in = PonderGameTests.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("missing " + path);
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json).getAsJsonObject().keySet();
        }
    }
}
