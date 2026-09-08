package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.AlloyHousing;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.ProtectionHooks;
import com.mealuet.create_originium_industry.core.oridust.SieveKind;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #21: alloy casing / sieve upgrade / sealed canister /
 * core housing. No GUI; attachments and tags do the work.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class AlloyPartsGameTests {

    private AlloyPartsGameTests() {}

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void survivalRecipesUseAlloyIngot(GameTestHelper helper) {
        helper.assertTrue(recipePresent(helper, "crafting/originium_alloy_casing"), "casing craft");
        helper.assertTrue(recipePresent(helper, "item_application/originium_alloy_casing"), "casing application");
        helper.assertTrue(recipePresent(helper, "crafting/originium_alloy_sieve"), "alloy sieve craft");
        helper.assertTrue(recipePresent(helper, "item_application/originium_alloy_sieve"), "alloy sieve application");
        helper.assertTrue(recipePresent(helper, "crafting/originium_sealed_canister"), "sealed canister");
        helper.assertTrue(recipePresent(helper, "crafting/originium_core_housing"), "core housing");
        helper.assertTrue(recipePresent(helper, "crafting/originium_cooling_chamber"), "cooling chamber still alloy");

        var casing = recipe(helper, "crafting/originium_alloy_casing");
        helper.assertTrue(
                casing.getIngredients().stream().anyMatch(ing -> ing.test(COIItems.ORIGINIUM_ALLOY_INGOT.asStack())),
                "casing consumes alloy"
        );
        var sieve = recipe(helper, "crafting/originium_alloy_sieve");
        helper.assertTrue(
                sieve.getIngredients().stream().anyMatch(ing -> ing.test(COIItems.ORIGINIUM_ALLOY_INGOT.asStack())),
                "sieve consumes alloy"
        );
        var sealed = recipe(helper, "crafting/originium_sealed_canister");
        helper.assertTrue(
                sealed.getIngredients().stream().anyMatch(ing -> ing.test(COIItems.ORIGINIUM_ALLOY_INGOT.asStack())),
                "sealed canister consumes alloy"
        );
        helper.assertTrue(
                sealed.getIngredients().stream().anyMatch(ing -> ing.test(COIBlocks.ALLOY_CASING.asStack())),
                "sealed canister consumes casing"
        );
        var housing = recipe(helper, "crafting/originium_core_housing");
        helper.assertTrue(
                housing.getIngredients().stream().anyMatch(ing -> ing.test(COIBlocks.ALLOY_CASING.asStack())),
                "core housing consumes casing"
        );
        helper.assertTrue(
                housing.getIngredients().stream().anyMatch(ing -> ing.test(COIBlocks.COOLING_CHAMBER.asStack())),
                "core housing consumes cooling chamber (M2/M3 link)"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void casingHousingReducesMachineEmission(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos emitRel = new BlockPos(2, 1, 1);
        BlockPos casingRel = new BlockPos(1, 1, 1);
        helper.setBlock(casingRel, COIBlocks.ALLOY_CASING.getDefaultState());
        helper.assertTrue(helper.getBlockState(casingRel).is(COITags.Blocks.POLLUTION_RESISTANT), "casing tagged");
        helper.assertTrue(helper.getBlockState(casingRel).is(COITags.Blocks.REACTOR_HOUSING), "casing is M3 housing");

        BlockPos emitPos = helper.absolutePos(emitRel);
        helper.assertValueEqual(AlloyHousing.reductionAt(level, emitPos), 0.10, "one face 10%");

        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);
        var milling = recipeHolder(helper, "milling/raw_originium_milling");
        int incoming = DustProductionHelper.getDustForRecipe(level, milling.value());
        helper.assertValueEqual(incoming, 80, "mill amount");
        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling.value());
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 72, "80 * 0.9 sealed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void coreHousingSealsMoreThanCasing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos emitRel = new BlockPos(2, 1, 1);
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.CORE_HOUSING.getDefaultState());
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 1)).is(COITags.Blocks.REACTOR_HOUSING), "core tagged");

        BlockPos emitPos = helper.absolutePos(emitRel);
        helper.assertValueEqual(AlloyHousing.reductionAt(level, emitPos), 0.20, "core face 20%");

        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);
        var milling = recipeHolder(helper, "milling/raw_originium_milling");
        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling.value());
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 64, "80 * 0.8 sealed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void alloySieveCapturesMoreThanIronSieve(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sieveRel = new BlockPos(1, 1, 1);
        BlockPos emitRel = new BlockPos(2, 1, 1);
        helper.setBlock(sieveRel, COIBlocks.ALLOY_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));

        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "alloy sieve BE");
        helper.assertTrue(sieve instanceof IDustPurifier, "purifier");
        helper.assertValueEqual(sieve.sieveKind(), SieveKind.ALLOY, "kind");
        sieve.activatePurifierForGameTest();
        helper.assertValueEqual(sieve.durability(), 1500, "alloy durability");
        helper.assertValueEqual(sieve.emissionCaptureFactor(), 0.70, "70% capture");
        helper.assertTrue(helper.getBlockState(sieveRel).is(COITags.Blocks.DUST_FILTERS), "tagged filter");

        BlockPos emitPos = helper.absolutePos(emitRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);
        var milling = recipeHolder(helper, "milling/raw_originium_milling");
        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling.value());
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 24, "80 * 0.3 remainder");
        helper.assertValueEqual(sieve.byproductStored(), 56, "80 * 0.7 buffered");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void alloySieveInsertsIntoKineticFilter(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos filterRel = new BlockPos(1, 1, 1);
        helper.setBlock(filterRel, COIBlocks.DUST_FILTER.get().defaultBlockState());
        DustFilterBlockEntity filter = helper.getBlockEntity(filterRel);
        helper.assertTrue(filter != null, "filter BE");

        filter.insertSieve(new ItemStack(COIBlocks.ALLOY_SIEVE.asItem()));
        helper.assertTrue(filter.hasSieve(), "inserted");
        helper.assertValueEqual(filter.sieveKind(), SieveKind.ALLOY, "alloy insert");
        helper.assertValueEqual(filter.sieveDurability(), 1500, "alloy filter durability");
        filter.activatePurifierForGameTest();
        helper.assertTrue(filter.isPurifierActive(), "spinning");
        helper.assertValueEqual(filter.emissionCaptureFactor(), 0.75, "75% filter capture");

        BlockPos emitPos = helper.absolutePos(filterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);
        var milling = recipeHolder(helper, "milling/raw_originium_milling");
        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling.value());
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 20, "80 * 0.25 remainder");

        ItemStack ejected = filter.removeSieve();
        helper.assertTrue(ejected.is(COIBlocks.ALLOY_SIEVE.asItem()), "ejects alloy sieve");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void sealedCanisterReinforcesProtection(GameTestHelper helper) {
        ItemStack sealed = new ItemStack(COIItems.ORIGINIUM_SEALED_CANISTER.get());
        helper.assertTrue(sealed.is(COITags.Items.ORIGINIUM_PROTECTION), "protection tag");
        helper.assertTrue(sealed.is(COITags.Items.REINFORCED_PROTECTION), "reinforced tag");
        helper.assertTrue(ProtectionHooks.isReinforcedProtectionItem(sealed), "hook");
        helper.assertFalse(
                ProtectionHooks.isReinforcedProtectionItem(new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get())),
                "iron canister is not reinforced"
        );
        helper.assertValueEqual(sealed.getMaxDamage(), 480, "longer durability");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(COIItems.ORIGINIUM_RESPIRATOR.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get()));
        helper.assertValueEqual(ProtectionHooks.incomingExposureFactor(player), 0.5, "iron full set unchanged");

        player.setItemSlot(EquipmentSlot.CHEST, sealed);
        helper.assertValueEqual(ProtectionHooks.countProtectionPieces(player), 2, "still two pieces");
        helper.assertTrue(ProtectionHooks.wearsReinforcedProtection(player), "wears sealed");
        helper.assertValueEqual(ProtectionHooks.incomingExposureFactor(player), 0.45, "0.5 * 0.9 bonus");
        helper.assertValueEqual(ProtectionHooks.exposureReductionPercent(player), 55, "55% less exposure");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "alloy_parts")
    public static void alloyPartConfigDefaults(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.CASING_HOUSING_REDUCTION_PER_FACE.get(), 0.10, "casing face");
        helper.assertValueEqual(COIConfig.CORE_HOUSING_REDUCTION_PER_FACE.get(), 0.20, "core face");
        helper.assertValueEqual(COIConfig.HOUSING_MAX_REDUCTION.get(), 0.50, "cap");
        helper.assertValueEqual(COIConfig.ALLOY_SIEVE_DURABILITY.get(), 1500, "durability");
        helper.assertValueEqual(COIConfig.ALLOY_PROCESS_SIEVE_EMISSION_CAPTURE.get(), 0.70, "process capture");
        helper.assertValueEqual(COIConfig.ALLOY_FILTER_EMISSION_CAPTURE.get(), 0.75, "filter capture");
        helper.assertValueEqual(COIConfig.SEALED_PROTECTION_BONUS.get(), 0.10, "bonus");
        helper.assertValueEqual(SieveKind.STANDARD.durability(), 500, "iron durability");
        helper.assertValueEqual(SieveKind.ALLOY.durability(), 1500, "alloy durability");
        BlockPos empty = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.assertValueEqual(AlloyHousing.reduceEmission(helper.getLevel(), empty, 100), 100, "no housing");
        helper.succeed();
    }

    private static boolean recipePresent(GameTestHelper helper, String path) {
        return helper.getLevel().getRecipeManager().byKey(id(path)).isPresent();
    }

    private static net.minecraft.world.item.crafting.Recipe<?> recipe(GameTestHelper helper, String path) {
        return recipeHolder(helper, path).value();
    }

    private static RecipeHolder<?> recipeHolder(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(id(path));
        helper.assertTrue(holder.isPresent(), "recipe loaded: " + path);
        return holder.get();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }
}
