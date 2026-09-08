package com.mealuet.create_originium_industry.compat.jei;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.recipeviewer.BasinProcessDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.DustEmissionDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.ReactorInfoDisplay;
import com.mealuet.create_originium_industry.compat.recipeviewer.RecipeViewerEntries;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.AllBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * JEI entrypoint. Loaded only when JEI is present ({@link JeiPlugin}); the main
 * mod never references this class so the game starts without JEI.
 */
@JeiPlugin
public final class COIJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, "jei_plugin");

    public static final RecipeType<BasinProcessDisplay> BASIN_PROCESS = RecipeType.create(
            CreateOriginiumIndustry.MODID, "basin_process", BasinProcessDisplay.class);
    public static final RecipeType<DustEmissionDisplay> DUST_EMISSION = RecipeType.create(
            CreateOriginiumIndustry.MODID, "dust_emission", DustEmissionDisplay.class);
    public static final RecipeType<ReactorInfoDisplay> REACTOR = RecipeType.create(
            CreateOriginiumIndustry.MODID, "reactor", ReactorInfoDisplay.class);

    public COIJeiPlugin() {}

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new BasinProcessCategory(guiHelper),
                new DustEmissionCategory(guiHelper),
                new ReactorInfoCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = clientRecipes();
        registration.addRecipes(BASIN_PROCESS, RecipeViewerEntries.basinProcesses(recipes));
        registration.addRecipes(DUST_EMISSION, RecipeViewerEntries.dustEmissions(recipes));
        registration.addRecipes(REACTOR, RecipeViewerEntries.reactorInfo());

        registration.addIngredientInfo(
                COIBlocks.POWER_CORE.asStack(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.create_originium_industry.info.power_core")
        );
        registration.addIngredientInfo(
                COIItems.PUREST_ORIGINIUM.asStack(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.create_originium_industry.info.purest_originium")
        );
        registration.addIngredientInfo(
                new ItemStack(COIFluids.ORIGINIUM_COOLANT.get().getBucket()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.create_originium_industry.info.originium_coolant")
        );
        registration.addIngredientInfo(
                COIBlocks.DUST_SIEVE.asStack(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.create_originium_industry.info.dust_sieve")
        );
        registration.addIngredientInfo(
                COIItems.ORIGINIUM_DUST.asStack(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.create_originium_industry.info.originium_dust")
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AllBlocks.BASIN.asStack(), BASIN_PROCESS);
        registration.addRecipeCatalyst(AllBlocks.MECHANICAL_MIXER.asStack(), BASIN_PROCESS);
        registration.addRecipeCatalyst(COIBlocks.DUST_SIEVE.asStack(), BASIN_PROCESS);
        registration.addRecipeCatalyst(COIBlocks.ALLOY_SIEVE.asStack(), BASIN_PROCESS);
        registration.addRecipeCatalyst(COIBlocks.COOLING_CHAMBER.asStack(), BASIN_PROCESS);

        registration.addRecipeCatalyst(COIItems.ORIGINIUM_DUST.asStack(), DUST_EMISSION);
        registration.addRecipeCatalyst(AllBlocks.MILLSTONE.asStack(), DUST_EMISSION);
        registration.addRecipeCatalyst(AllBlocks.CRUSHING_WHEEL.asStack(), DUST_EMISSION);
        registration.addRecipeCatalyst(AllBlocks.MECHANICAL_MIXER.asStack(), DUST_EMISSION);
        registration.addRecipeCatalyst(COIBlocks.DUST_FILTER.asStack(), DUST_EMISSION);

        registration.addRecipeCatalyst(COIBlocks.POWER_CORE.asStack(), REACTOR);
        registration.addRecipeCatalyst(COIItems.PUREST_ORIGINIUM.asStack(), REACTOR);
        registration.addRecipeCatalyst(COIBlocks.COOLING_CHAMBER.asStack(), REACTOR);
        registration.addRecipeCatalyst(COIBlocks.SUPER_COOLING_CHAMBER.asStack(), REACTOR);
        registration.addRecipeCatalyst(COIBlocks.CORE_HOUSING.asStack(), REACTOR);
        registration.addRecipeCatalyst(new ItemStack(COIFluids.ORIGINIUM_COOLANT.get().getBucket()), REACTOR);
    }

    static RecipeManager clientRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager();
        }
        ClientPacketListener connection = minecraft.getConnection();
        return connection == null ? null : connection.getRecipeManager();
    }
}
