package com.mealuet.create_originium_industry.compat.recipeviewer;

import com.mealuet.create_originium_industry.core.oridust.DustEmissionIndex;
import com.mealuet.create_originium_industry.core.purest.BasinProcessIndex;
import com.mealuet.create_originium_industry.core.purest.BasinProcessSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Collects COI recipe-viewer pages from datapack indexes and reactor constants.
 * JEI/EMI plugins only format these records; they do not own the data.
 */
public final class RecipeViewerEntries {

    private RecipeViewerEntries() {}

    public static List<BasinProcessDisplay> basinProcesses(RecipeManager recipes) {
        List<BasinProcessDisplay> displays = new ArrayList<>();
        for (Map.Entry<ResourceLocation, BasinProcessSpec> entry : BasinProcessIndex.entries().entrySet()) {
            if (entry.getValue() == null || !entry.getValue().hasRequirements()) {
                continue;
            }
            displays.add(BasinProcessDisplay.of(entry.getKey(), entry.getValue(), recipes));
        }
        displays.sort(Comparator.comparing(display -> display.recipeId().toString()));
        return List.copyOf(displays);
    }

    public static List<DustEmissionDisplay> dustEmissions(RecipeManager recipes) {
        List<DustEmissionDisplay> displays = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : DustEmissionIndex.recipeAmounts().entrySet()) {
            displays.add(DustEmissionDisplay.recipe(entry.getKey(), entry.getValue(), recipes));
        }
        for (Map.Entry<ResourceLocation, Integer> entry : DustEmissionIndex.itemAmounts().entrySet()) {
            displays.add(DustEmissionDisplay.item(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<ResourceLocation, Integer> entry : DustEmissionIndex.itemTagAmounts().entrySet()) {
            displays.add(DustEmissionDisplay.itemTag(entry.getKey(), entry.getValue()));
        }
        displays.sort(Comparator
                .comparing((DustEmissionDisplay display) -> display.kind().name())
                .thenComparing(display -> display.id().toString()));
        return List.copyOf(displays);
    }

    public static List<ReactorInfoDisplay> reactorInfo() {
        return ReactorInfoDisplay.all();
    }
}
