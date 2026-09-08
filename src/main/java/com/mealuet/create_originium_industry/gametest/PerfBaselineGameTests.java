package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.CoolingChamberBlock;
import com.mealuet.create_originium_industry.block.CoolingChamberBlockEntity;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustDiffusionEngine;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.DustSyncTracker;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.perf.PerfLoad;
import com.mealuet.create_originium_industry.core.perf.PerfProbe;
import com.mealuet.create_originium_industry.index.COIBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

/**
 * Issue #24: repeatable timing for the stated 100-machine dust + reactor load.
 * <p>
 * Asserts a generous CI guard (50 ms) so shared runners do not flake on the
 * 2 ms design target. Median nanos are logged for {@code docs/PERF.md}.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class PerfBaselineGameTests {

    private static final int WARMUP = 3;
    private static final int TRIALS = 5;

    private PerfBaselineGameTests() {}

    @GameTest(template = "empty", batch = "perf")
    public static void statedLoadMatchesDesignBudget(GameTestHelper helper) {
        helper.assertValueEqual(PerfProbe.STATED_MACHINE_COUNT, 100, "100 machines");
        helper.assertValueEqual(PerfProbe.TARGET_NANOS, 2_000_000L, "2 ms target");
        helper.assertValueEqual(COIConfig.DIFFUSION_INTERVAL.get(), 20, "dust every 20 ticks");
        helper.assertValueEqual(COIConfig.FILTER_ABSORPTION_INTERVAL.get(), 20, "filter every 20 ticks");
        helper.assertValueEqual(COIConfig.dustSyncInterval(), 20, "sync every 20 ticks");
        helper.assertValueEqual(COIConfig.INIT_CHUNK_RADIUS.get(), 8, "active-set radius 8");
        helper.assertValueEqual(PerfLoad.GRID * PerfLoad.GRID, PerfProbe.STATED_MACHINE_COUNT, "10×10 grid");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "perf")
    public static void dustCycleMovesCheckerboardAndStaysUnderGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos center = factoryCenter(helper);
        PerfProbe.resetForTests();
        try {
            PerfLoad.seedMachineDust(level, center);
            ChunkPos high = PerfLoad.machineChunks(center).get(0);
            ChunkPos low = PerfLoad.machineChunks(center).get(1);
            int highBefore = OriginiumDustManager.getDust(level, high);
            int lowBefore = OriginiumDustManager.getDust(level, low);

            warmupDust(level, center);
            long[] samples = new long[TRIALS];
            int active = 0;
            for (int i = 0; i < TRIALS; i++) {
                PerfLoad.seedMachineDust(level, center);
                long start = System.nanoTime();
                active = DustDiffusionEngine.runActiveSetCycle(level);
                samples[i] = System.nanoTime() - start;
            }
            long median = median(samples);
            log("dust_cycle", "active=" + active + " median=" + PerfProbe.formatNanos(median)
                    + " min=" + PerfProbe.formatNanos(min(samples)));

            helper.assertTrue(active >= PerfProbe.STATED_MACHINE_COUNT, "active set includes the 100 machine chunks");
            helper.assertTrue(highBefore > lowBefore, "checkerboard seeded");
            helper.assertTrue(
                    OriginiumDustManager.getDust(level, high) < highBefore
                            || OriginiumDustManager.getDust(level, low) > lowBefore,
                    "diffusion transferred on the stated load"
            );
            helper.assertTrue(median < PerfProbe.CI_GUARD_NANOS, "dust cycle under 50 ms CI guard");
            helper.succeed();
        } finally {
            PerfLoad.clearMachineDust(level, center);
        }
    }

    @GameTest(template = "empty", batch = "perf")
    public static void hundredReactorTicksStayUnderGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PowerCoreBlockEntity core = placeRunningCore(helper);
        PerfProbe.resetForTests();
        warmupReactors(core, level);
        long[] samples = new long[TRIALS];
        for (int i = 0; i < TRIALS; i++) {
            core.configureForGameTest(8, 4000, 0, 0, 0.0);
            long start = System.nanoTime();
            for (int n = 0; n < PerfProbe.STATED_MACHINE_COUNT; n++) {
                core.tickReactor(level);
            }
            samples[i] = System.nanoTime() - start;
        }
        long median = median(samples);
        log("reactor_x100", "median=" + PerfProbe.formatNanos(median)
                + " min=" + PerfProbe.formatNanos(min(samples))
                + " probe=" + PerfProbe.formatNanos(PerfProbe.lastReactorTickNanos())
                + " count=" + PerfProbe.lastReactorTickCount());
        helper.assertTrue(core.isRunning(), "core still running after 100 ticks");
        helper.assertTrue(median < PerfProbe.CI_GUARD_NANOS, "100 reactor ticks under 50 ms CI guard");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "perf")
    public static void hundredFilterWritesStayUnderGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos center = factoryCenter(helper);
        DustFilterBlockEntity filter = placeSpinningFilter(helper, new BlockPos(1, 1, 1));
        BlockPos filterAbs = helper.absolutePos(new BlockPos(1, 1, 1));
        PerfProbe.resetForTests();
        try {
            warmupFilters(level, center, filter, filterAbs);
            long[] samples = new long[TRIALS];
            for (int i = 0; i < TRIALS; i++) {
                PerfLoad.seedMachineDust(level, center);
                OriginiumDustManager.setDust(level, WorldSpace.toDustChunk(level, filterAbs), 2000, DustReason.DEBUG);
                long start = System.nanoTime();
                absorbHundredChunks(level, center);
                filter.absorbAmbient(level, filterAbs, COIConfig.FILTER_ABSORPTION_RATE.get());
                samples[i] = System.nanoTime() - start;
            }
            long median = median(samples);
            log("filter_x100", "median=" + PerfProbe.formatNanos(median)
                    + " min=" + PerfProbe.formatNanos(min(samples)));
            helper.assertTrue(median < PerfProbe.CI_GUARD_NANOS, "100 filter writes under 50 ms CI guard");
            helper.succeed();
        } finally {
            PerfLoad.clearMachineDust(level, center);
        }
    }

    @GameTest(template = "empty", batch = "perf")
    public static void alignedStatedLoadExtraStaysUnderGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos center = factoryCenter(helper);
        PowerCoreBlockEntity core = placeRunningCore(helper);
        DustFilterBlockEntity filter = placeSpinningFilter(helper, new BlockPos(1, 1, 2));
        BlockPos filterAbs = helper.absolutePos(new BlockPos(1, 1, 2));
        PerfProbe.resetForTests();
        try {
            for (int w = 0; w < WARMUP; w++) {
                alignedOnce(level, center, core, filter, filterAbs);
            }
            long[] samples = new long[TRIALS];
            int active = 0;
            for (int i = 0; i < TRIALS; i++) {
                core.configureForGameTest(8, 4000, 0, 0, 0.0);
                PerfLoad.seedMachineDust(level, center);
                OriginiumDustManager.setDust(level, WorldSpace.toDustChunk(level, filterAbs), 2000, DustReason.DEBUG);
                long start = System.nanoTime();
                active = alignedOnce(level, center, core, filter, filterAbs);
                samples[i] = System.nanoTime() - start;
            }
            long median = median(samples);
            long min = min(samples);
            boolean underTarget = median < PerfProbe.TARGET_NANOS;
            log("aligned_extra", "active=" + active
                    + " median=" + PerfProbe.formatNanos(median)
                    + " min=" + PerfProbe.formatNanos(min)
                    + " target=" + PerfProbe.formatNanos(PerfProbe.TARGET_NANOS)
                    + " under_2ms=" + underTarget
                    + " dust=" + PerfProbe.formatNanos(PerfProbe.lastDustCycleNanos())
                    + " reactor=" + PerfProbe.formatNanos(PerfProbe.lastReactorTickNanos())
                    + " filter=" + PerfProbe.formatNanos(PerfProbe.lastFilterTickNanos())
                    + " sync=" + PerfProbe.formatNanos(PerfProbe.lastSyncNanos()));
            helper.assertTrue(active >= PerfProbe.STATED_MACHINE_COUNT, "aligned load has 100 machine chunks");
            helper.assertTrue(median < PerfProbe.CI_GUARD_NANOS, "aligned extra under 50 ms CI guard");
            helper.succeed();
        } finally {
            PerfLoad.clearMachineDust(level, center);
        }
    }

    private static int alignedOnce(
            ServerLevel level,
            ChunkPos center,
            PowerCoreBlockEntity core,
            DustFilterBlockEntity filter,
            BlockPos filterAbs
    ) {
        int active = DustDiffusionEngine.runActiveSetCycle(level);
        for (int n = 0; n < PerfProbe.STATED_MACHINE_COUNT; n++) {
            core.tickReactor(level);
        }
        absorbHundredChunks(level, center);
        filter.absorbAmbient(level, filterAbs, COIConfig.FILTER_ABSORPTION_RATE.get());
        DustSyncTracker.flushNow(level);
        return active;
    }

    private static void absorbHundredChunks(ServerLevel level, ChunkPos center) {
        int rate = COIConfig.FILTER_ABSORPTION_RATE.get();
        for (ChunkPos pos : PerfLoad.machineChunks(center)) {
            OriginiumDustManager.addDust(level, pos, -rate, DustReason.FILTER);
        }
    }

    private static void warmupDust(ServerLevel level, ChunkPos center) {
        for (int i = 0; i < WARMUP; i++) {
            PerfLoad.seedMachineDust(level, center);
            DustDiffusionEngine.runActiveSetCycle(level);
        }
    }

    private static void warmupReactors(PowerCoreBlockEntity core, ServerLevel level) {
        core.configureForGameTest(8, 4000, 0, 0, 0.0);
        for (int i = 0; i < WARMUP * PerfProbe.STATED_MACHINE_COUNT; i++) {
            core.tickReactor(level);
        }
    }

    private static void warmupFilters(
            ServerLevel level,
            ChunkPos center,
            DustFilterBlockEntity filter,
            BlockPos filterAbs
    ) {
        for (int i = 0; i < WARMUP; i++) {
            PerfLoad.seedMachineDust(level, center);
            absorbHundredChunks(level, center);
            filter.absorbAmbient(level, filterAbs, COIConfig.FILTER_ABSORPTION_RATE.get());
        }
    }

    private static ChunkPos factoryCenter(GameTestHelper helper) {
        return WorldSpace.toDustChunk(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 1)));
    }

    private static PowerCoreBlockEntity placeRunningCore(GameTestHelper helper) {
        BlockPos coreRel = new BlockPos(2, 1, 1);
        helper.setBlock(coreRel, COIBlocks.POWER_CORE.getDefaultState());
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.CORE_HOUSING.getDefaultState());
        helper.setBlock(new BlockPos(2, 1, 2), COIBlocks.COOLING_CHAMBER.getDefaultState()
                .setValue(CoolingChamberBlock.FACING, Direction.SOUTH));
        CoolingChamberBlockEntity chamber = helper.getBlockEntity(new BlockPos(2, 1, 2));
        helper.assertTrue(chamber != null, "chamber BE");
        chamber.activateForGameTest();
        PowerCoreBlockEntity core = helper.getBlockEntity(coreRel);
        helper.assertTrue(core != null, "power core BE");
        core.configureForGameTest(8, 4000, 0, 0, 0.0);
        return core;
    }

    private static DustFilterBlockEntity placeSpinningFilter(GameTestHelper helper, BlockPos rel) {
        helper.setBlock(rel, COIBlocks.DUST_FILTER.getDefaultState());
        DustFilterBlockEntity filter = helper.getBlockEntity(rel);
        helper.assertTrue(filter != null, "filter BE");
        filter.activatePurifierForGameTest();
        return filter;
    }

    private static long median(long[] samples) {
        long[] copy = Arrays.copyOf(samples, samples.length);
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static long min(long[] samples) {
        long m = Long.MAX_VALUE;
        for (long sample : samples) {
            m = Math.min(m, sample);
        }
        return m;
    }

    private static void log(String lane, String detail) {
        CreateOriginiumIndustry.LOGGER.info("[COI perf] {} {}", lane, detail);
    }
}
