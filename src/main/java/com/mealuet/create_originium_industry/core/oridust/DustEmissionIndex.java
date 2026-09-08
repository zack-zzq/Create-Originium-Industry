package com.mealuet.create_originium_industry.core.oridust;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Datapack-driven dust emission amounts.
 * <p>
 * JSON lives at {@code data/<namespace>/coi_dust_emission/*.json}:
 * <pre>
 * { "recipe": "modid:path", "amount": 80 }
 * { "item": "modid:path", "amount": 40 }
 * { "item_tag": "modid:path", "amount": 40 }
 * </pre>
 * Lookups return {@code -1} when unmapped so callers can distinguish
 * "explicit zero" from "no entry".
 */
public final class DustEmissionIndex extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "coi_dust_emission";
    public static final DustEmissionIndex INSTANCE = new DustEmissionIndex();

    private static final Gson GSON = new Gson();

    private volatile Map<ResourceLocation, Integer> recipeAmounts = Map.of();
    private volatile Map<ResourceLocation, Integer> itemAmounts = Map.of();
    private volatile Map<ResourceLocation, Integer> itemTagAmounts = Map.of();

    private DustEmissionIndex() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    /**
     * @return datapack amount for this recipe id, or {@code -1} if unmapped
     */
    public static int getRecipeAmount(ResourceLocation recipeId) {
        if (recipeId == null) {
            return -1;
        }
        Integer amount = INSTANCE.recipeAmounts.get(recipeId);
        return amount == null ? -1 : amount;
    }

    /**
     * @return datapack amount for this item id, or {@code -1} if unmapped
     */
    public static int getItemAmount(ResourceLocation itemId) {
        if (itemId == null) {
            return -1;
        }
        Integer amount = INSTANCE.itemAmounts.get(itemId);
        return amount == null ? -1 : amount;
    }

    /**
     * Item-id entry, then the highest matching {@code item_tag} amount.
     *
     * @return datapack amount, or {@code -1} if unmapped
     */
    public static int getItemAmount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        int exact = getItemAmount(itemId);
        if (exact >= 0) {
            return exact;
        }
        int tagMax = -1;
        for (Map.Entry<ResourceLocation, Integer> entry : INSTANCE.itemTagAmounts.entrySet()) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, entry.getKey());
            if (stack.is(tag)) {
                tagMax = Math.max(tagMax, entry.getValue());
            }
        }
        return tagMax;
    }

    /**
     * Whether {@code stack} is tagged {@link COITags.Items#DUST_PRODUCING}.
     */
    public static boolean isDustProducing(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(COITags.Items.DUST_PRODUCING);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Integer> recipes = new HashMap<>();
        Map<ResourceLocation, Integer> items = new HashMap<>();
        Map<ResourceLocation, Integer> tags = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : object.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "dust emission");
                int amount = GsonHelper.getAsInt(json, "amount");
                if (amount < 0) {
                    CreateOriginiumIndustry.LOGGER.warn(
                            "[OriDust] Ignoring negative amount in dust emission file {}", file.getKey());
                    continue;
                }
                boolean any = false;
                if (json.has("recipe")) {
                    ResourceLocation recipe = parseId(GsonHelper.getAsString(json, "recipe"));
                    putAmount(recipes, recipe, amount, "recipe", file.getKey());
                    any = true;
                }
                if (json.has("item")) {
                    ResourceLocation item = parseId(GsonHelper.getAsString(json, "item"));
                    putAmount(items, item, amount, "item", file.getKey());
                    any = true;
                }
                if (json.has("item_tag")) {
                    ResourceLocation tag = parseId(GsonHelper.getAsString(json, "item_tag"));
                    putAmount(tags, tag, amount, "item_tag", file.getKey());
                    any = true;
                }
                if (!any) {
                    CreateOriginiumIndustry.LOGGER.warn(
                            "[OriDust] Dust emission file {} has no recipe, item, or item_tag", file.getKey());
                }
            } catch (RuntimeException ex) {
                CreateOriginiumIndustry.LOGGER.error(
                        "[OriDust] Could not parse dust emission file {}", file.getKey(), ex);
            }
        }

        this.recipeAmounts = Map.copyOf(recipes);
        this.itemAmounts = Map.copyOf(items);
        this.itemTagAmounts = Map.copyOf(tags);
        CreateOriginiumIndustry.LOGGER.debug(
                "[OriDust] Loaded dust emission entries: {} recipes, {} items, {} tags",
                recipes.size(), items.size(), tags.size()
        );
    }

    private static void putAmount(Map<ResourceLocation, Integer> map, ResourceLocation key, int amount,
                                  String kind, ResourceLocation file) {
        Integer previous = map.put(key, amount);
        if (previous != null && previous != amount) {
            CreateOriginiumIndustry.LOGGER.warn(
                    "[OriDust] Duplicate {} mapping {} in {} ({} -> {})",
                    kind, key, file, previous, amount
            );
        }
    }

    private static ResourceLocation parseId(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource location: " + raw);
        }
        return id;
    }
}
