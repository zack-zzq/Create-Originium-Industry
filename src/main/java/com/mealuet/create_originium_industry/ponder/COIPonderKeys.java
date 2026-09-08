package com.mealuet.create_originium_industry.ponder;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Ponder lang keys. Scene copy always goes through {@link Component#translatable(String)}
 * so {@code en_us} / {@code zh_cn} stay the source of truth.
 * <p>
 * Ponder looks up {@code <modid>.ponder.<sceneId>.header} and {@code .text_N}
 * (and {@code .ponder.tag.<id>}) via I18n at runtime. Keep this list in lockstep
 * with both lang files and the order of {@code .text()} calls in each scene.
 */
public final class COIPonderKeys {

    public static final String DUST_GENERATION = "dust_generation";
    public static final String DUST_FILTER = "dust_filter";
    public static final String PUREST_SUPERCOOLING = "purest_supercooling";
    public static final String POWER_CORE = "power_core";

    public static final String TAG_ORIGINIUM_INDUSTRY = "originium_industry";

    public static final String DUST_GENERATION_HEADER = sceneKey(DUST_GENERATION, "header");
    public static final String DUST_GENERATION_TEXT_1 = sceneKey(DUST_GENERATION, "text_1");
    public static final String DUST_GENERATION_TEXT_2 = sceneKey(DUST_GENERATION, "text_2");
    public static final String DUST_GENERATION_TEXT_3 = sceneKey(DUST_GENERATION, "text_3");

    public static final String DUST_FILTER_HEADER = sceneKey(DUST_FILTER, "header");
    public static final String DUST_FILTER_TEXT_1 = sceneKey(DUST_FILTER, "text_1");
    public static final String DUST_FILTER_TEXT_2 = sceneKey(DUST_FILTER, "text_2");
    public static final String DUST_FILTER_TEXT_3 = sceneKey(DUST_FILTER, "text_3");

    public static final String PUREST_HEADER = sceneKey(PUREST_SUPERCOOLING, "header");
    public static final String PUREST_TEXT_1 = sceneKey(PUREST_SUPERCOOLING, "text_1");
    public static final String PUREST_TEXT_2 = sceneKey(PUREST_SUPERCOOLING, "text_2");
    public static final String PUREST_TEXT_3 = sceneKey(PUREST_SUPERCOOLING, "text_3");
    public static final String PUREST_TEXT_4 = sceneKey(PUREST_SUPERCOOLING, "text_4");

    public static final String POWER_CORE_HEADER = sceneKey(POWER_CORE, "header");
    public static final String POWER_CORE_TEXT_1 = sceneKey(POWER_CORE, "text_1");
    public static final String POWER_CORE_TEXT_2 = sceneKey(POWER_CORE, "text_2");
    public static final String POWER_CORE_TEXT_3 = sceneKey(POWER_CORE, "text_3");
    public static final String POWER_CORE_TEXT_4 = sceneKey(POWER_CORE, "text_4");

    public static final String TAG_TITLE = tagKey(TAG_ORIGINIUM_INDUSTRY);
    public static final String TAG_DESCRIPTION = tagKey(TAG_ORIGINIUM_INDUSTRY) + ".description";

    public static final List<String> SCENE_IDS = List.of(
            DUST_GENERATION,
            DUST_FILTER,
            PUREST_SUPERCOOLING,
            POWER_CORE
    );

    public static final List<String> SCHEMATICS = List.of(
            "dust_generation.nbt",
            "dust_filter.nbt",
            "purest_supercooling.nbt",
            "power_core.nbt"
    );

    public static final List<String> ALL = List.of(
            DUST_GENERATION_HEADER,
            DUST_GENERATION_TEXT_1,
            DUST_GENERATION_TEXT_2,
            DUST_GENERATION_TEXT_3,
            DUST_FILTER_HEADER,
            DUST_FILTER_TEXT_1,
            DUST_FILTER_TEXT_2,
            DUST_FILTER_TEXT_3,
            PUREST_HEADER,
            PUREST_TEXT_1,
            PUREST_TEXT_2,
            PUREST_TEXT_3,
            PUREST_TEXT_4,
            POWER_CORE_HEADER,
            POWER_CORE_TEXT_1,
            POWER_CORE_TEXT_2,
            POWER_CORE_TEXT_3,
            POWER_CORE_TEXT_4,
            TAG_TITLE,
            TAG_DESCRIPTION
    );

    private COIPonderKeys() {}

    public static Component component(String key) {
        return Component.translatable(key);
    }

    /**
     * Resolve a ponder string through {@link Component#translatable(String)}.
     * Ponder's overlay API takes a {@code String}; the lang files remain canonical.
     */
    public static String copy(String key) {
        return component(key).getString();
    }

    public static String sceneKey(String sceneId, String part) {
        return CreateOriginiumIndustry.MODID + ".ponder." + sceneId + "." + part;
    }

    public static String tagKey(String tagId) {
        return CreateOriginiumIndustry.MODID + ".ponder.tag." + tagId;
    }
}
