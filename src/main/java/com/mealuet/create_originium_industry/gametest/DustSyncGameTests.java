package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustMeterBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.ClientDustCache;
import com.mealuet.create_originium_industry.core.oridust.ClientExposureCache;
import com.mealuet.create_originium_industry.core.oridust.DustObservation;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.DustSyncPlanner;
import com.mealuet.create_originium_industry.core.oridust.DustSyncTracker;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.core.oridust.VisibleDust;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.network.DustSyncPayload;
import com.mealuet.create_originium_industry.network.ExposureSyncPayload;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Coverage for issue #17: nearby dirty-set dust sync, local exposure payload,
 * and the shared {@link VisibleDust} read API. No mock-player network sends.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class DustSyncGameTests {

    private DustSyncGameTests() {}

    @GameTest(template = "empty", batch = "dust_sync")
    public static void plannerOmitsFarDirtyAndSkipsCleanWindow(GameTestHelper helper) {
        ChunkPos center = new ChunkPos(10, 20);
        ChunkPos near = new ChunkPos(11, 20);
        ChunkPos far = new ChunkPos(100, 200);
        LongOpenHashSet dirty = new LongOpenHashSet();
        dirty.add(near.toLong());
        dirty.add(far.toLong());
        Long2IntOpenHashMap last = neverSentMap();
        Long2IntOpenHashMap current = new Long2IntOpenHashMap();
        current.defaultReturnValue(0);
        current.put(near.toLong(), 1500);
        current.put(far.toLong(), 9000);
        current.put(center.toLong(), 0);

        DustSyncPlanner.DustDelta standing = DustSyncPlanner.plan(
                center, 2, dirty, last::get, current::get, false
        );
        helper.assertValueEqual(standing.size(), 1, "standing: only nearby dirty");
        helper.assertValueEqual(standing.valueOf(near.toLong(), -1), 1500, "near value");
        helper.assertValueEqual(standing.valueOf(far.toLong(), -1), -1, "far omitted");

        DustSyncPlanner.DustDelta window = DustSyncPlanner.plan(
                center, 2, new LongOpenHashSet(), last::get, current::get, true
        );
        helper.assertValueEqual(window.size(), 1, "window skips never-sent zeros");
        helper.assertValueEqual(window.valueOf(near.toLong(), -1), 1500, "window includes nearby dust");
        helper.assertValueEqual(window.valueOf(center.toLong(), -1), -1, "clean center omitted");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void twoObserversAgreeOnSameDelta(GameTestHelper helper) {
        ChunkPos center = new ChunkPos(3, 4);
        long key = center.toLong();
        LongOpenHashSet dirty = new LongOpenHashSet();
        dirty.add(key);
        Long2IntOpenHashMap lastA = neverSentMap();
        Long2IntOpenHashMap lastB = neverSentMap();
        Long2IntOpenHashMap current = new Long2IntOpenHashMap();
        current.defaultReturnValue(0);
        current.put(key, 777);

        DustSyncPlanner.DustDelta a = DustSyncPlanner.plan(center, 1, dirty, lastA::get, current::get, false);
        DustSyncPlanner.DustDelta b = DustSyncPlanner.plan(center, 1, dirty, lastB::get, current::get, false);
        helper.assertValueEqual(a.size(), b.size(), "same size");
        helper.assertValueEqual(a.valueOf(key, -1), b.valueOf(key, -1), "same dust");

        DustObservation obsA = new DustObservation();
        DustObservation obsB = new DustObservation();
        obsA.apply(a.keys(), a.values());
        obsB.apply(b.keys(), b.values());
        helper.assertValueEqual(obsA.get(center), 777, "observer A");
        helper.assertValueEqual(obsB.get(center), 777, "observer B");
        helper.assertTrue(obsA.isKnown(center) && obsB.isKnown(center), "both marked known");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void pruneKeepsFarNonZeroForLaterClear(GameTestHelper helper) {
        ChunkPos center = new ChunkPos(0, 0);
        ChunkPos far = new ChunkPos(40, 0);
        Long2IntOpenHashMap last = neverSentMap();
        last.put(far.toLong(), 500);
        last.put(new ChunkPos(50, 0).toLong(), 0);
        DustSyncPlanner.pruneOutsideRadius(last, center, 2);
        helper.assertValueEqual(last.get(far.toLong()), 500, "keep far non-zero last-sent");
        helper.assertTrue(last.get(new ChunkPos(50, 0).toLong()) == DustSyncPlanner.NEVER_SENT,
                "drop far remembered zero");

        LongOpenHashSet dirty = new LongOpenHashSet();
        Long2IntOpenHashMap current = new Long2IntOpenHashMap();
        current.defaultReturnValue(0);
        DustSyncPlanner.DustDelta returned = DustSyncPlanner.plan(
                far, 1, dirty, last::get, current::get, true
        );
        helper.assertValueEqual(returned.valueOf(far.toLong(), -1), 0, "return visit sends clear");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void payloadsRoundTripOnDedicatedServerSafeBuf(GameTestHelper helper) {
        DustSyncPayload dust = new DustSyncPayload(
                new long[] {ChunkPos.asLong(1, 2), ChunkPos.asLong(-4, 9)},
                new int[] {12, 3400}
        );
        RegistryFriendlyByteBuf dustBuf = new RegistryFriendlyByteBuf(
                Unpooled.buffer(), helper.getLevel().registryAccess());
        DustSyncPayload.STREAM_CODEC.encode(dustBuf, dust);
        DustSyncPayload dustDecoded = DustSyncPayload.STREAM_CODEC.decode(dustBuf);
        helper.assertValueEqual(dustDecoded.chunkKeys().length, 2, "dust keys");
        helper.assertValueEqual(dustDecoded.dustValues()[1], 3400, "dust value");
        helper.assertValueEqual(dustDecoded.chunkKeys()[0], ChunkPos.asLong(1, 2), "key 0");

        ExposureSyncPayload exposure = new ExposureSyncPayload(800, 2500);
        RegistryFriendlyByteBuf expBuf = new RegistryFriendlyByteBuf(
                Unpooled.buffer(), helper.getLevel().registryAccess());
        ExposureSyncPayload.STREAM_CODEC.encode(expBuf, exposure);
        ExposureSyncPayload expDecoded = ExposureSyncPayload.STREAM_CODEC.decode(expBuf);
        helper.assertValueEqual(expDecoded.exposure(), 800, "exposure");
        helper.assertValueEqual(expDecoded.infection(), 2500, "infection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void applyingPayloadsMakesClientCachesAgree(GameTestHelper helper) {
        ClientDustCache.clear();
        ClientExposureCache.clear();
        helper.assertFalse(ClientDustCache.hasAny(), "empty dust cache");
        helper.assertFalse(ClientExposureCache.hasReceived(), "no exposure yet");

        long key = ChunkPos.asLong(8, 8);
        DustSyncPayload.STREAM_CODEC.encode(
                new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess()),
                new DustSyncPayload(new long[] {key}, new int[] {1500})
        );
        ClientDustCache.apply(new long[] {key, ChunkPos.asLong(9, 8)}, new int[] {1500, 0});
        ClientExposureCache.apply(120, 40);

        DustObservation otherPlayer = new DustObservation();
        otherPlayer.apply(new long[] {key, ChunkPos.asLong(9, 8)}, new int[] {1500, 0});

        helper.assertValueEqual(ClientDustCache.get(new ChunkPos(8, 8)), 1500, "local cache");
        helper.assertValueEqual(otherPlayer.get(new ChunkPos(8, 8)), 1500, "peer cache");
        helper.assertTrue(ClientDustCache.isKnown(new ChunkPos(9, 8)), "zero is known");
        helper.assertValueEqual(ClientDustCache.getOrFallback(new ChunkPos(9, 8), 99), 0, "known zero not fallback");
        helper.assertValueEqual(ClientDustCache.getOrFallback(new ChunkPos(99, 99), 42), 42, "unknown uses fallback");
        helper.assertValueEqual(ClientExposureCache.exposure(), 120, "exposure cache");
        helper.assertValueEqual(ClientExposureCache.infection(), 40, "infection cache");
        helper.assertTrue(ClientExposureCache.hasReceived(), "received");

        ClientDustCache.clear();
        ClientExposureCache.clear();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void managerWritesMarkDirtyAndVisibleDustMatchesServer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        DustSyncTracker.clearForTests();
        ChunkPos pos = new ChunkPos(12017, -12017);
        OriginiumDustManager.clearDust(level, pos, DustReason.DEBUG);
        DustSyncTracker.clearForTests();

        OriginiumDustManager.setDust(level, pos, 2222, DustReason.DEBUG);
        helper.assertTrue(DustSyncTracker.isDustDirty(pos.toLong()), "set marks dirty");
        helper.assertValueEqual(VisibleDust.chunkDust(level, pos), 2222, "VisibleDust server path");
        helper.assertValueEqual(VisibleDust.chunkDust(level, pos), OriginiumDustManager.getDust(level, pos), "API agrees");

        OriginiumDustManager.clearDust(level, pos, DustReason.DEBUG);
        helper.assertTrue(DustSyncTracker.isDustDirty(pos.toLong()), "clear stays dirty");
        helper.assertValueEqual(VisibleDust.chunkDust(level, pos), 0, "cleared");
        DustSyncTracker.clearForTests();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void meterDisplayedDustUsesVisibleDustApi(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos meterRel = new BlockPos(1, 1, 1);
        helper.setBlock(meterRel, COIBlocks.DUST_METER.getDefaultState());
        DustMeterBlockEntity meter = helper.getBlockEntity(meterRel);
        helper.assertTrue(meter != null, "meter BE");

        BlockPos abs = helper.absolutePos(meterRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, abs);
        OriginiumDustManager.setDust(level, chunk, 4000, DustReason.DEBUG);
        meter.refreshFromServer();

        helper.assertValueEqual(meter.syncedDust(), 4000, "BE snapshot");
        helper.assertValueEqual(meter.displayedDust(), 4000, "displayed");
        helper.assertValueEqual(
                VisibleDust.chunkDustOrFallback(level, chunk, meter.syncedDust()),
                meter.displayedDust(),
                "meter shares VisibleDust"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void exposureCacheAndPlayerDataStayIndependent(GameTestHelper helper) {
        PlayerExposureData data = new PlayerExposureData();
        data.setExposure(900);
        data.setInfection(200);
        ClientExposureCache.clear();
        helper.assertValueEqual(ClientExposureCache.exposure(), 0, "client starts empty");
        ClientExposureCache.apply(data.getExposure(), data.getInfection());
        helper.assertValueEqual(ClientExposureCache.exposure(), 900, "mirrors local player");
        helper.assertValueEqual(ClientExposureCache.infection(), 200, "infection");
        data.setExposure(0);
        helper.assertValueEqual(ClientExposureCache.exposure(), 900, "server mutate does not touch cache");
        ClientExposureCache.clear();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "dust_sync")
    public static void syncRadiusStaysInsideActiveSet(GameTestHelper helper) {
        helper.assertTrue(COIConfig.syncDustToClients(), "sync on");
        helper.assertValueEqual(COIConfig.dustSyncInterval(), 20, "low frequency");
        int radius = COIConfig.dustSyncRadius(false);
        helper.assertTrue(radius <= COIConfig.effectiveInitChunkRadius(false), "no farther than active set");
        helper.assertTrue(radius >= 1, "at least one chunk");
        helper.assertTrue(
                DustSyncPlanner.shouldSend(100, DustSyncPlanner.NEVER_SENT, true, false),
                "dirty first send"
        );
        helper.assertFalse(
                DustSyncPlanner.shouldSend(0, DustSyncPlanner.NEVER_SENT, false, true),
                "window skips never-sent zero"
        );
        helper.succeed();
    }

    private static Long2IntOpenHashMap neverSentMap() {
        Long2IntOpenHashMap map = new Long2IntOpenHashMap();
        map.defaultReturnValue(DustSyncPlanner.NEVER_SENT);
        return map;
    }
}
