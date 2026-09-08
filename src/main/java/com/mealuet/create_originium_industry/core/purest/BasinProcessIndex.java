package com.mealuet.create_originium_industry.core.purest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Datapack-driven extra matching for Create basin mixing on the purest line.
 * <p>
 * JSON lives at {@code data/<namespace>/coi_basin_process/*.json}:
 * <pre>
 * { "recipe": "modid:path", "require_sieve": true }
 * { "recipe": "modid:path", "require_cooling_chamber": true, "reject_heat": true }
 * </pre>
 */
public final class BasinProcessIndex extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "coi_basin_process";
    public static final BasinProcessIndex INSTANCE = new BasinProcessIndex();

    private volatile Map<ResourceLocation, BasinProcessSpec> specs = Map.of();

    private BasinProcessIndex() {
        super(new Gson(), DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static BasinProcessSpec get(ResourceLocation recipeId) {
        if (recipeId == null) {
            return BasinProcessSpec.NONE;
        }
        return INSTANCE.specs.getOrDefault(recipeId, BasinProcessSpec.NONE);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, BasinProcessSpec> loaded = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : object.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "basin process");
                if (!json.has("recipe")) {
                    CreateOriginiumIndustry.LOGGER.warn("[Purest] Basin process file {} has no recipe", file.getKey());
                    continue;
                }
                ResourceLocation recipe = parseId(GsonHelper.getAsString(json, "recipe"));
                BasinProcessSpec spec = new BasinProcessSpec(
                        GsonHelper.getAsBoolean(json, "require_sieve", false),
                        GsonHelper.getAsBoolean(json, "require_cooling_chamber", false),
                        GsonHelper.getAsBoolean(json, "reject_heat", false)
                );
                BasinProcessSpec previous = loaded.put(recipe, spec);
                if (previous != null && !previous.equals(spec)) {
                    CreateOriginiumIndustry.LOGGER.warn(
                            "[Purest] Duplicate basin process mapping {} in {}", recipe, file.getKey());
                }
            } catch (RuntimeException ex) {
                CreateOriginiumIndustry.LOGGER.error(
                        "[Purest] Could not parse basin process file {}", file.getKey(), ex);
            }
        }
        this.specs = Map.copyOf(loaded);
        CreateOriginiumIndustry.LOGGER.debug("[Purest] Loaded {} basin process specs", loaded.size());
    }

    private static ResourceLocation parseId(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource location: " + raw);
        }
        return id;
    }
}
