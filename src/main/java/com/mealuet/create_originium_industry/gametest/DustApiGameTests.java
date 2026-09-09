package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.ByproductBuffer;
import com.mealuet.create_originium_industry.core.oridust.DustEmissionIndex;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.DustPurification;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.DustSubmission;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.mealuet.create_originium_industry.core.oridust.IOridustProducer;
import com.mealuet.create_originium_industry.core.oridust.InfectionStage;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposure;
import com.mealuet.create_originium_industry.core.oridust.PurificationResult;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Coverage for issue #7: data-driven {@link IOridustProducer} submit path and
 * shared {@link IDustPurifier} capture/byproduct accounting.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class DustApiGameTests {

    private DustApiGameTests() {}

    @GameTest(template = "empty", batch = "dust_api")
    public static void producerSubmitsExpectedDust(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos chunk = WorldSpace.toDustChunk(level, pos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        IOridustProducer producer = new RecordingProducer();
        int deposited = producer.submitDust(level, pos, 250, DustReason.DEBUG);

        helper.assertValueEqual(deposited, 250, "producer deposited");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 250, "chunk dust");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void purifierReducesEmissionWithoutItemDupOrVoid(GameTestHelper helper) {
        RecordingPurifier purifier = new RecordingPurifier(0.4, 20);
        PurificationResult result = DustPurification.reduceEmission(100, List.of(purifier));

        helper.assertTrue(result.conservesDust(), "remaining + captured == incoming");
        helper.assertValueEqual(result.remaining(), 60, "remaining after 40% capture");
        helper.assertValueEqual(result.captured(), 40, "captured");
        helper.assertValueEqual(result.byproductItems(), 2, "40 dust / 20 per item");
        helper.assertValueEqual(purifier.buffer.stored(), 0, "no remainder");
        helper.assertValueEqual(
                purifier.capturedTotal,
                result.byproductItems() * 20 + purifier.buffer.stored(),
                "captured == items * ratio + buffer"
        );

        PurificationResult second = DustPurification.reduceEmission(100, List.of(purifier));
        helper.assertValueEqual(second.byproductItems(), 2, "second capture is not a duplicate of the first");
        helper.assertValueEqual(purifier.itemsTotal, 4, "two captures yield four items total");
        helper.assertValueEqual(purifier.capturedTotal, 80, "two captures");
        helper.assertValueEqual(purifier.acceptCalls, 2, "one accept per capture");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void purifierRemainderIsNotVoided(GameTestHelper helper) {
        RecordingPurifier purifier = new RecordingPurifier(1.0, 30);
        PurificationResult first = DustPurification.reduceEmission(50, List.of(purifier));
        helper.assertValueEqual(first.byproductItems(), 1, "50 / 30 = 1 item");
        helper.assertValueEqual(purifier.buffer.stored(), 20, "remainder kept");

        PurificationResult second = DustPurification.reduceEmission(10, List.of(purifier));
        helper.assertValueEqual(second.byproductItems(), 1, "20 + 10 fills a second item");
        helper.assertValueEqual(purifier.buffer.stored(), 0, "remainder consumed into item");
        helper.assertValueEqual(purifier.capturedTotal, 60, "50 + 10");
        helper.assertValueEqual(purifier.itemsTotal * 30 + purifier.buffer.stored(), 60, "no void, no dup");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void inactivePurifierDoesNotCapture(GameTestHelper helper) {
        RecordingPurifier purifier = new RecordingPurifier(1.0, 10);
        purifier.active = false;
        PurificationResult result = DustPurification.reduceEmission(80, List.of(purifier));
        helper.assertValueEqual(result.remaining(), 80, "inactive passes through");
        helper.assertValueEqual(result.captured(), 0, "no capture");
        helper.assertValueEqual(purifier.acceptCalls, 0, "accept not called");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void disabledByproductDoesNotSpawnItems(GameTestHelper helper) {
        RecordingPurifier purifier = new RecordingPurifier(1.0, 0);
        PurificationResult result = DustPurification.reduceEmission(50, List.of(purifier));
        helper.assertTrue(result.conservesDust(), "dust still conserved");
        helper.assertValueEqual(result.byproductItems(), 0, "no items when ratio is 0");
        helper.assertValueEqual(purifier.buffer.stored(), 0, "buffer unused when byproduct off");
        helper.assertValueEqual(disabledByproductBufferYieldsZero(), 0, "buffer helper");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void dataDrivenAmountsAndTags(GameTestHelper helper) {
        ResourceLocation milling = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "milling/raw_originium_milling");
        ResourceLocation crushing = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "crushing/raw_originium_crushing");
        ResourceLocation catalyst = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "mixing/catalyst_mixing");
        ResourceLocation datapackOnly = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "example/datapack_only");

        helper.assertValueEqual(DustEmissionIndex.getRecipeAmount(milling), 80, "datapack milling");
        helper.assertValueEqual(DustEmissionIndex.getRecipeAmount(crushing), 100, "datapack crushing");
        helper.assertValueEqual(
                DustProductionHelper.getDustForRecipe(milling),
                COIConfig.DUST_FROM_MILLING.get(),
                "config override for frozen milling id"
        );
        helper.assertValueEqual(DustEmissionIndex.getRecipeAmount(catalyst), 0, "catalyst datapack amount 0");
        helper.assertValueEqual(DustProductionHelper.getDustForRecipe(catalyst), 0, "catalyst is not a dust recipe");
        helper.assertValueEqual(DustProductionHelper.getDustForRecipe(datapackOnly), 33, "datapack-only recipe id");

        helper.assertValueEqual(COIConfig.DUST_FROM_TAGGED_ITEM.get(), 40, "tagged-item fallback");
        helper.assertValueEqual(COIConfig.FILTER_EMISSION_CAPTURE.get(), 0.5, "emission capture default");
        helper.assertValueEqual(COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get(), 100, "byproduct default");

        ItemStack raw = new ItemStack(COIItems.RAW_ORIGINIUM.get());
        helper.assertTrue(raw.is(COITags.Items.DUST_PRODUCING), "raw is dust_producing");
        helper.assertValueEqual(DustProductionHelper.getDustForItem(raw), 40, "tag datapack amount");
        helper.assertValueEqual(DustProductionHelper.getDustForItem(new ItemStack(Items.IRON_INGOT)), 0, "untagged item");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void kineticFilterImplementsPurifierAndIdleDoesNotSteal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos filterRel = new BlockPos(1, 1, 1);
        helper.setBlock(filterRel, COIBlocks.DUST_FILTER.get().defaultBlockState());

        DustFilterBlockEntity filter = helper.getBlockEntity(filterRel);
        helper.assertTrue(filter != null, "filter BE present");
        helper.assertTrue(filter instanceof IDustPurifier, "filter is IDustPurifier");
        filter.insertSieve(new ItemStack(COIBlocks.DUST_SIEVE.asItem()));
        helper.assertFalse(filter.isPurifierActive(), "no rotation → inactive");
        helper.assertTrue(
                helper.getBlockState(filterRel).is(COITags.Blocks.DUST_FILTERS),
                "filter block is tagged dust_filters"
        );

        BlockPos emitPos = helper.absolutePos(filterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, emitPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        int deposited = DustSubmission.submit(level, emitPos, 100, DustReason.DEBUG);
        helper.assertValueEqual(deposited, 100, "idle filter at the emit pos does not capture");
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 100, "full submit");
        helper.assertValueEqual(filter.absorbAmbient(level, emitPos, 50), 0, "idle absorb");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_api")
    public static void playerExposureAccessorsRoundTrip(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(PlayerExposure.getExposure(player), 0, "fresh exposure");
        helper.assertValueEqual(PlayerExposure.getInfection(player), 0, "fresh infection");
        helper.assertValueEqual(PlayerExposure.getStage(player), InfectionStage.NONE, "fresh stage");

        PlayerExposure.setExposure(player, 640);
        PlayerExposure.setInfection(player, 2500);
        helper.assertValueEqual(PlayerExposure.getExposure(player), 640, "set exposure");
        helper.assertValueEqual(PlayerExposure.getInfection(player), 2500, "set infection");
        helper.assertValueEqual(PlayerExposure.getStage(player), InfectionStage.GROWTH, "stage from infection");
        helper.assertValueEqual(PlayerExposure.of(player).getExposure(), 640, "of() shares attachment");
        helper.succeed();
    }

    private static int disabledByproductBufferYieldsZero() {
        ByproductBuffer buffer = new ByproductBuffer();
        int items = buffer.add(99, 0);
        return items + buffer.stored();
    }

    /**
     * Test double that records the last deposit while delegating to
     * {@link DustSubmission}.
     */
    private static final class RecordingProducer implements IOridustProducer {
        @Override
        public int submitDust(ServerLevel level, BlockPos pos, int expectedAmount, DustReason reason) {
            return DustSubmission.INSTANCE.submitDust(level, pos, expectedAmount, reason);
        }
    }

    /**
     * In-memory purifier used to prove capture math and byproduct conservation
     * without placing Create kinetics.
     */
    private static final class RecordingPurifier implements IDustPurifier {
        private final double factor;
        private final int dustPerItem;
        private final ByproductBuffer buffer = new ByproductBuffer();
        private boolean active = true;
        private int capturedTotal;
        private int itemsTotal;
        private int acceptCalls;

        private RecordingPurifier(double factor, int dustPerItem) {
            this.factor = factor;
            this.dustPerItem = dustPerItem;
        }

        @Override
        public boolean isPurifierActive() {
            return active;
        }

        @Override
        public double emissionCaptureFactor() {
            return factor;
        }

        @Override
        public int acceptCapturedDust(int captured) {
            acceptCalls++;
            capturedTotal += Math.max(0, captured);
            int items = buffer.add(captured, dustPerItem);
            itemsTotal += items;
            return items;
        }
    }
}
