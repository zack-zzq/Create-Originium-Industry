package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #8: mill / crush / mix (including heated and superheated)
 * emit through {@link DustProductionHelper}, neighbouring kinetic filters capture
 * emission into {@code originium_dust} without dup/void, and catalyst mixing is
 * an intentional non-emitter.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class DustProcessingGameTests {

    private DustProcessingGameTests() {}

    @GameTest(template = "empty", batch = "dust_processing")
    public static void unfilteredMillCrushAndHeatMixEmit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos chunk = WorldSpace.toDustChunk(level, pos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        Recipe<?> milling = recipe(helper, "milling/raw_originium_milling");
        Recipe<?> crushing = recipe(helper, "crushing/raw_originium_crushing");
        Recipe<?> heated = recipe(helper, "mixing/originium_shard_mixing");
        Recipe<?> superheated = recipe(helper, "mixing/originium_mixing");
        Recipe<?> alloy = recipe(helper, "mixing/molten_originium_iron_ingot_mixing");

        helper.assertValueEqual(requiredHeat(heated), HeatCondition.HEATED, "shard mix is heated");
        helper.assertValueEqual(requiredHeat(superheated), HeatCondition.SUPERHEATED, "melt is superheated");
        helper.assertValueEqual(requiredHeat(alloy), HeatCondition.NONE, "alloy mix is unheated");

        helper.assertValueEqual(
                DustProductionHelper.resolveRecipeId(level, milling),
                id("milling/raw_originium_milling"),
                "mill holder id"
        );
        helper.assertValueEqual(
                DustProductionHelper.resolveRecipeId(level, milling),
                id("milling/raw_originium_milling"),
                "second resolve uses cached mill id"
        );
        helper.assertValueEqual(
                DustProductionHelper.resolveRecipeId(level, heated),
                id("mixing/originium_shard_mixing"),
                "heated mix holder id"
        );
        helper.assertValueEqual(
                DustProductionHelper.resolveRecipeId(level, superheated),
                id("mixing/originium_mixing"),
                "superheated mix holder id"
        );

        // Create 6 stores the recipe type on ProcessingRecipe.id; that must not
        // be the emission key. Holder resolution still maps the frozen amounts.
        assertTypeIdIsNotTheEmissionKey(helper, milling, COIConfig.DUST_FROM_MILLING.get());
        assertTypeIdIsNotTheEmissionKey(helper, heated, COIConfig.DUST_FROM_SHARD_MIXING.get());
        assertTypeIdIsNotTheEmissionKey(helper, superheated, COIConfig.DUST_FROM_ORIGINIUM_MELTING.get());

        DustProductionHelper.emitDustFromRecipe(level, pos, milling);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), COIConfig.DUST_FROM_MILLING.get(), "mill emit");

        DustProductionHelper.emitDustFromRecipe(level, pos, crushing);
        int afterCrush = COIConfig.DUST_FROM_MILLING.get() + COIConfig.DUST_FROM_CRUSHING.get();
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), afterCrush, "crush emit stacked");

        DustProductionHelper.emitDustFromRecipe(level, pos, heated);
        int afterHeated = afterCrush + COIConfig.DUST_FROM_SHARD_MIXING.get();
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), afterHeated, "heated mix emit");

        DustProductionHelper.emitDustFromRecipe(level, pos, superheated);
        int afterSuper = afterHeated + COIConfig.DUST_FROM_ORIGINIUM_MELTING.get();
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), afterSuper, "superheated mix emit");

        DustProductionHelper.emitDustFromRecipe(level, pos, alloy);
        helper.assertValueEqual(
                OriginiumDustManager.getDust(level, chunk),
                afterSuper + COIConfig.DUST_FROM_ALLOY_MIXING.get(),
                "unheated alloy mix still emits"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_processing")
    public static void catalystMixingIsIntentionalNonEmitter(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos chunk = WorldSpace.toDustChunk(level, pos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        Recipe<?> catalyst = recipe(helper, "mixing/catalyst_mixing");
        helper.assertValueEqual(requiredHeat(catalyst), HeatCondition.HEATED, "catalyst is heated");
        helper.assertValueEqual(DustProductionHelper.mappedAmount(id("mixing/catalyst_mixing")), 0, "explicit datapack 0");
        helper.assertValueEqual(DustProductionHelper.getDustForRecipe(level, catalyst), 0, "no dust from catalyst");
        helper.assertValueEqual(DustProductionHelper.heatAwareFallback(catalyst), 0, "no originium feedstock");

        DustProductionHelper.emitDustFromRecipe(level, pos, catalyst);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 0, "chunk unchanged");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_processing")
    public static void heatFallbackMatchesOriginiumHeatTiers(GameTestHelper helper) {
        Recipe<?> heated = recipe(helper, "mixing/originium_shard_mixing");
        Recipe<?> superheated = recipe(helper, "mixing/originium_mixing");
        Recipe<?> alloy = recipe(helper, "mixing/molten_originium_iron_ingot_mixing");
        Recipe<?> milling = recipe(helper, "milling/raw_originium_milling");

        helper.assertValueEqual(
                DustProductionHelper.heatAwareFallback(superheated),
                COIConfig.DUST_FROM_ORIGINIUM_MELTING.get(),
                "superheated fallback"
        );
        helper.assertValueEqual(
                DustProductionHelper.heatAwareFallback(heated),
                COIConfig.DUST_FROM_SHARD_MIXING.get(),
                "heated fallback"
        );
        helper.assertValueEqual(
                DustProductionHelper.heatAwareFallback(alloy),
                COIConfig.DUST_FROM_ALLOY_MIXING.get(),
                "unheated molten fallback"
        );
        helper.assertValueEqual(
                DustProductionHelper.heatAwareFallback(milling),
                COIConfig.DUST_FROM_TAGGED_ITEM.get(),
                "unheated item fallback"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_processing")
    public static void neighbouringFilterLowersEmitAndYieldsByproduct(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Mixer-style layout: emit above, filter on the basin position below.
        BlockPos filterRel = new BlockPos(1, 1, 1);
        BlockPos emitRel = new BlockPos(1, 2, 1);
        helper.setBlock(filterRel, COIBlocks.DUST_FILTER.get().defaultBlockState());

        DustFilterBlockEntity filter = helper.getBlockEntity(filterRel);
        helper.assertTrue(filter != null, "filter BE");
        helper.assertTrue(filter instanceof IDustPurifier, "filter is purifier");
        filter.activatePurifierForGameTest();
        helper.assertTrue(filter.isPurifierActive(), "sieve + spin");

        BlockPos emitPos = helper.absolutePos(emitRel);
        BlockPos filterPos = helper.absolutePos(filterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        Recipe<?> superheated = recipe(helper, "mixing/originium_mixing");
        int incoming = DustProductionHelper.getDustForRecipe(level, superheated);
        helper.assertValueEqual(incoming, COIConfig.DUST_FROM_ORIGINIUM_MELTING.get(), "superheated amount");
        helper.assertValueEqual(COIConfig.FILTER_EMISSION_CAPTURE.get(), 0.5, "50% capture");
        helper.assertValueEqual(COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get(), 100, "100 dust / item");

        DustProductionHelper.emitDustFromRecipe(level, emitPos, superheated);

        int captured = (int) Math.round(incoming * 0.5);
        int remaining = incoming - captured;
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), remaining, "filtered chunk dust");
        helper.assertTrue(OriginiumDustManager.getDust(level, chunk) < incoming, "pollution dropped vs unfiltered");
        helper.assertValueEqual(countOriginiumDustItems(level, filterPos), 1, "one originium_dust byproduct");
        helper.assertValueEqual(filter.byproductStored(), 0, "no remainder after exact item");
        helper.assertValueEqual(
                countOriginiumDustItems(level, filterPos) * COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get()
                        + filter.byproductStored(),
                captured,
                "captured == items * ratio + buffer"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_processing")
    public static void filteredEmitDoesNotDupOrVoid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos filterRel = new BlockPos(2, 1, 1);
        BlockPos emitRel = new BlockPos(1, 1, 1);
        helper.setBlock(filterRel, COIBlocks.DUST_FILTER.get().defaultBlockState());

        DustFilterBlockEntity filter = helper.getBlockEntity(filterRel);
        helper.assertTrue(filter != null, "filter BE");
        filter.activatePurifierForGameTest();

        BlockPos emitPos = helper.absolutePos(emitRel);
        BlockPos filterPos = helper.absolutePos(filterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        Recipe<?> milling = recipe(helper, "milling/raw_originium_milling");
        int incoming = DustProductionHelper.getDustForRecipe(level, milling);
        int capturedEach = (int) Math.round(incoming * COIConfig.FILTER_EMISSION_CAPTURE.get());
        int remainingEach = incoming - capturedEach;

        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), remainingEach, "first remaining");
        helper.assertValueEqual(countOriginiumDustItems(level, filterPos), 0, "80*0.5=40 does not fill an item");
        helper.assertValueEqual(filter.byproductStored(), capturedEach, "remainder buffered");

        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), remainingEach * 2, "second remaining stacked");
        int capturedTotal = capturedEach * 2;
        int items = countOriginiumDustItems(level, filterPos);
        helper.assertValueEqual(items, 0, "80 dust still under 100 / item — no premature item");
        helper.assertValueEqual(filter.byproductStored(), capturedTotal, "buffer accumulated, not voided");

        Recipe<?> superheated = recipe(helper, "mixing/originium_mixing");
        int melt = DustProductionHelper.getDustForRecipe(level, superheated);
        int meltCaptured = (int) Math.round(melt * COIConfig.FILTER_EMISSION_CAPTURE.get());
        DustProductionHelper.emitDustFromRecipe(level, emitPos, superheated);

        int capturedAll = capturedTotal + meltCaptured;
        int ratio = COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get();
        int expectedItems = capturedAll / ratio;
        int expectedBuffer = capturedAll % ratio;
        helper.assertValueEqual(countOriginiumDustItems(level, filterPos), expectedItems, "items from combined capture");
        helper.assertValueEqual(filter.byproductStored(), expectedBuffer, "remainder after items");
        helper.assertValueEqual(
                countOriginiumDustItems(level, filterPos) * ratio + filter.byproductStored(),
                capturedAll,
                "no dup, no void"
        );
        helper.assertValueEqual(
                OriginiumDustManager.getDust(level, chunk),
                remainingEach * 2 + (melt - meltCaptured),
                "chunk only received uncaptured remainder"
        );
        helper.succeed();
    }

    private static void assertTypeIdIsNotTheEmissionKey(GameTestHelper helper, Recipe<?> recipe, int expected) {
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(helper.getLevel(), recipe),
                expected,
                "resolved recipe amount"
        );
        if (recipe instanceof ProcessingRecipe<?> processing
                && processing.id != null
                && "create".equals(processing.id.getNamespace())
                && !processing.id.getPath().contains("/")) {
            helper.assertValueEqual(
                    DustProductionHelper.getDustForRecipe(processing.id),
                    0,
                    "Create type id must not map to frozen amounts"
            );
        }
    }

    private static HeatCondition requiredHeat(Recipe<?> recipe) {
        if (recipe instanceof ProcessingRecipe<?> processing) {
            return processing.getRequiredHeat();
        }
        return HeatCondition.NONE;
    }

    private static Recipe<?> recipe(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(id(path));
        helper.assertTrue(holder.isPresent(), "recipe loaded: " + path);
        return holder.get().value();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }

    private static int countOriginiumDustItems(ServerLevel level, BlockPos around) {
        AABB box = new AABB(around).inflate(1.5);
        int total = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (entity.getItem().is(COIItems.ORIGINIUM_DUST.get())) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }
}
