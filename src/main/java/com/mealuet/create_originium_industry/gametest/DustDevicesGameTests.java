package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustMeterBlockEntity;
import com.mealuet.create_originium_industry.block.DustNozzleBlock;
import com.mealuet.create_originium_industry.block.DustNozzleBlockEntity;
import com.mealuet.create_originium_industry.block.ProcessSieveBlock;
import com.mealuet.create_originium_industry.block.ProcessSieveBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.DustPurification;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.DustRedirect;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #4: Basin/process sieve, Encased Fan nozzle redirection,
 * and dust meter readout.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class DustDevicesGameTests {

    private DustDevicesGameTests() {}

    @GameTest(template = "empty", batch = "dust_devices")
    public static void processSieveCapturesEmitAndYieldsByproduct(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sieveRel = new BlockPos(1, 1, 1);
        BlockPos emitRel = new BlockPos(2, 1, 1);
        helper.setBlock(sieveRel, COIBlocks.DUST_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));

        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "sieve BE");
        helper.assertTrue(sieve instanceof IDustPurifier, "sieve is purifier");
        sieve.activatePurifierForGameTest();
        helper.assertTrue(sieve.isPurifierActive(), "placed sieve is active without RPM");
        helper.assertTrue(
                helper.getBlockState(sieveRel).is(COITags.Blocks.DUST_FILTERS),
                "sieve tagged dust_filters"
        );

        BlockPos emitPos = helper.absolutePos(emitRel);
        BlockPos sievePos = helper.absolutePos(sieveRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        var milling = recipe(helper, "milling/raw_originium_milling");
        int incoming = DustProductionHelper.getDustForRecipe(level, milling);
        helper.assertValueEqual(COIConfig.PROCESS_SIEVE_EMISSION_CAPTURE.get(), 0.4, "40% process capture");
        DustProductionHelper.emitDustFromRecipe(level, emitPos, milling);

        int captured = (int) Math.round(incoming * 0.4);
        int remaining = incoming - captured;
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), remaining, "filtered mill emit");
        helper.assertValueEqual(sieve.byproductStored(), captured, "80*0.4=32 buffered");
        helper.assertValueEqual(countOriginiumDustItems(level, sievePos), 0, "no item yet");

        var melt = recipe(helper, "mixing/originium_mixing");
        int meltIn = DustProductionHelper.getDustForRecipe(level, melt);
        DustProductionHelper.emitDustFromRecipe(level, emitPos, melt);
        int meltCaptured = (int) Math.round(meltIn * 0.4);
        int capturedAll = captured + meltCaptured;
        int ratio = COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get();
        helper.assertValueEqual(countOriginiumDustItems(level, sievePos), capturedAll / ratio, "byproduct items");
        helper.assertValueEqual(sieve.byproductStored(), capturedAll % ratio, "remainder");
        helper.assertValueEqual(
                countOriginiumDustItems(level, sievePos) * ratio + sieve.byproductStored(),
                capturedAll,
                "no dup, no void"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_devices")
    public static void basinSideSieveIsFoundFromMixerEmitPos(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos basinRel = new BlockPos(1, 1, 1);
        BlockPos sieveRel = new BlockPos(2, 1, 1);
        BlockPos emitRel = new BlockPos(1, 2, 1);

        helper.setBlock(basinRel, AllBlocks.BASIN.getDefaultState());
        helper.setBlock(sieveRel, COIBlocks.DUST_SIEVE.getDefaultState().setValue(ProcessSieveBlock.FACING, Direction.EAST));

        ProcessSieveBlockEntity sieve = helper.getBlockEntity(sieveRel);
        helper.assertTrue(sieve != null, "sieve BE on basin side");
        sieve.activatePurifierForGameTest();

        BlockPos emitPos = helper.absolutePos(emitRel);
        helper.assertTrue(
                DustPurification.findNearby(level, emitPos).contains(sieve),
                "mixer emit sees basin-adjacent sieve"
        );

        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);
        var heated = recipe(helper, "mixing/originium_shard_mixing");
        int incoming = DustProductionHelper.getDustForRecipe(level, heated);
        DustProductionHelper.emitDustFromRecipe(level, emitPos, heated);
        int captured = (int) Math.round(incoming * COIConfig.PROCESS_SIEVE_EMISSION_CAPTURE.get());
        helper.assertValueEqual(
                OriginiumDustManager.getDust(level, chunk),
                incoming - captured,
                "basin-side sieve reduced mixer emit"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_devices")
    public static void nozzleRedirectsDustToDownwindChunk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos from = WorldSpace.toDustChunk(level, pos);
        ChunkPos to = DustRedirect.neighbourChunk(from, Direction.EAST);

        OriginiumDustManager.clearDust(level, from, DustReason.DEBUG);
        OriginiumDustManager.clearDust(level, to, DustReason.DEBUG);
        OriginiumDustManager.setDust(level, from, 500, DustReason.DEBUG);

        DustRedirect.Transfer transfer = DustRedirect.moveToNeighbour(level, pos, Direction.EAST, 80);
        helper.assertValueEqual(transfer.moved(), 80, "moved");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, from), 420, "source reduced");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, to), 80, "downwind gained");
        helper.assertValueEqual(
                OriginiumDustManager.getDust(level, from) + OriginiumDustManager.getDust(level, to),
                500,
                "redirect conserves dust"
        );

        helper.setBlock(new BlockPos(1, 1, 1),
                COIBlocks.DUST_NOZZLE.getDefaultState().setValue(DustNozzleBlock.FACING, Direction.EAST));
        DustNozzleBlockEntity nozzle = helper.getBlockEntity(new BlockPos(1, 1, 1));
        helper.assertTrue(nozzle != null, "nozzle BE");
        nozzle.activateAirflowForGameTest(Direction.EAST, 64f);
        helper.assertTrue(nozzle.hasAirflow(), "test airflow");

        OriginiumDustManager.setDust(level, from, 200, DustReason.DEBUG);
        OriginiumDustManager.setDust(level, to, 0, DustReason.DEBUG);
        int moved = nozzle.redirectNowForGameTest(50);
        helper.assertValueEqual(moved, 50, "nozzle moved");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, from), 150, "nozzle source");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, to), 50, "nozzle dest");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_devices")
    public static void dustMeterMatchesServerDustAndComparator(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meterRel = new BlockPos(1, 1, 1);
        helper.setBlock(meterRel, COIBlocks.DUST_METER.getDefaultState());
        DustMeterBlockEntity meter = helper.getBlockEntity(meterRel);
        helper.assertTrue(meter != null, "meter BE");

        BlockPos abs = helper.absolutePos(meterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, abs);
        OriginiumDustManager.setDust(level, chunk, 1500, DustReason.DEBUG);

        meter.refreshFromServer();
        helper.assertValueEqual(meter.syncedDust(), 1500, "meter matches SavedData");
        helper.assertValueEqual(meter.risk(), DustLevel.MEDIUM, "risk tier");
        helper.assertValueEqual(meter.protectionPercent(), 0, "no protection gear");
        int expectedSignal = (int) Math.ceil(1500 * 15.0 / COIConfig.METER_COMPARATOR_FULL_DUST.get());
        helper.assertValueEqual(meter.comparatorSignal(), expectedSignal, "comparator scale");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_devices")
    public static void survivalRecipesAndCreativeItemsExist(GameTestHelper helper) {
        helper.assertTrue(recipePresent(helper, "crafting/originium_dust_sieve"), "sieve recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_dust_nozzle"), "nozzle recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_dust_meter"), "meter recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_dust_filter"), "filter recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_respirator"), "respirator recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_filter_canister"), "canister recipe");
        helper.assertTrue(COIBlocks.DUST_SIEVE.asItem() != net.minecraft.world.item.Items.AIR, "sieve item");
        helper.assertTrue(COIBlocks.DUST_NOZZLE.asItem() != net.minecraft.world.item.Items.AIR, "nozzle item");
        helper.assertTrue(COIBlocks.DUST_METER.asItem() != net.minecraft.world.item.Items.AIR, "meter item");
        helper.assertTrue(COIItems.ORIGINIUM_DUST.get() != net.minecraft.world.item.Items.AIR, "dust item");
        helper.succeed();
    }

    private static boolean recipePresent(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path));
        return holder.isPresent();
    }

    private static net.minecraft.world.item.crafting.Recipe<?> recipe(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path));
        helper.assertTrue(holder.isPresent(), "recipe loaded: " + path);
        return holder.get().value();
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
