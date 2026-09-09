package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustProductionHelper;
import com.mealuet.create_originium_industry.core.oridust.Oridust;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriDustData;
import com.mealuet.create_originium_industry.core.oridust.OriDustSavedData;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Automated coverage for originium dust persistence (PR #12 / issue #9).
 * <p>
 * These run on {@code runGameTestServer} with zero Sable/Aeronautics on the
 * classpath. Live ship {@code logicalPose} remap remains a manual smoke test.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class OriDustGameTests {

    private OriDustGameTests() {}

    /**
     * SavedData get/set/add survives serialize → deserialize (simulated restart).
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void savedDataPersistsAcrossSaveLoad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.Provider registries = level.registryAccess();
        ChunkPos pos = new ChunkPos(10001, -10001);

        OriDustSavedData data = OriDustSavedData.get(level);
        data.set(pos, 0);
        helper.assertValueEqual(data.get(pos), 0, "cleared dust");

        data.set(pos, 777);
        helper.assertValueEqual(data.get(pos), 777, "set dust");
        helper.assertValueEqual(data.add(pos, 23), 800, "add dust");

        CompoundTag tag = data.save(new CompoundTag(), registries);
        OriDustSavedData reloaded = OriDustSavedData.load(tag, registries);

        helper.assertValueEqual(reloaded.get(pos), 800, "dust after NBT roundtrip");
        helper.succeed();
    }

    /**
     * {@link OriginiumDustManager} write, then SavedData save/load — the
     * in-game get/set path used by {@code /coi_debug dust}.
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void managerSetDustSurvivesSavedDataReload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos pos = new ChunkPos(10002, -10002);

        OriginiumDustManager.setDust(level, pos, 888, DustReason.DEBUG);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, pos), 888, "manager get after set");

        OriDustSavedData store = OriDustSavedData.get(level);
        CompoundTag tag = store.save(new CompoundTag(), level.registryAccess());
        OriDustSavedData reloaded = OriDustSavedData.load(tag, level.registryAccess());

        helper.assertValueEqual(reloaded.get(pos), 888, "manager write after SavedData reload");
        helper.succeed();
    }

    /**
     * Mill/crush production helper deposits dust through
     * {@link com.mealuet.create_originium_industry.core.oridust.IOridustProducer}
     * (same path as the mixins) and that value round-trips through SavedData.
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void millAndCrushEmitAndPersist(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos millPos = helper.absolutePos(new BlockPos(1, 1, 1));
        ChunkPos chunk = WorldSpace.toDustChunk(level, millPos);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        ResourceLocation milling = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "milling/raw_originium_milling");
        DustProductionHelper.emitDustFromRecipe(level, millPos, milling);
        helper.assertValueEqual(
                OriginiumDustManager.getDust(level, chunk),
                COIConfig.DUST_FROM_MILLING.get(),
                "milling emit"
        );

        ResourceLocation crushing = ResourceLocation.fromNamespaceAndPath(
                CreateOriginiumIndustry.MODID, "crushing/raw_originium_crushing");
        DustProductionHelper.emitDustFromRecipe(level, millPos, crushing);
        int expected = COIConfig.DUST_FROM_MILLING.get() + COIConfig.DUST_FROM_CRUSHING.get();
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), expected, "crushing emit stacked");

        CompoundTag tag = OriDustSavedData.get(level).save(new CompoundTag(), level.registryAccess());
        helper.assertValueEqual(OriDustSavedData.load(tag, level.registryAccess()).get(chunk), expected, "recipe dust after reload");
        helper.succeed();
    }

    /**
     * The landmine: write/read a ChunkPos whose chunk is not loaded.
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void writeUnloadedLogicalChunkPersists(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos far = new ChunkPos(123456, -654321);

        helper.assertTrue(
                level.getChunkSource().getChunkNow(far.x, far.z) == null,
                "test chunk must be unloaded (the old attachment path would drop this write)"
        );

        OriginiumDustManager.setDust(level, far, 4321, DustReason.DEBUG);
        helper.assertValueEqual(OriginiumDustManager.getDust(level, far), 4321, "get after unloaded write");

        CompoundTag tag = OriDustSavedData.get(level).save(new CompoundTag(), level.registryAccess());
        OriDustSavedData reloaded = OriDustSavedData.load(tag, level.registryAccess());
        helper.assertValueEqual(reloaded.get(far), 4321, "unloaded write after SavedData reload");
        helper.succeed();
    }

    /**
     * Legacy attachment merge: add+clamp, attachment zeroed, second pass does
     * not double-count. Merge/NBT assertions use a fresh SavedData instance so
     * a reused GameTestServer world cannot poison {@code MigratedChunks}.
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void lazyMigrationMergesOnceAndZerosAttachment(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OriDustSavedData saved = new OriDustSavedData();

        ChunkPos logical = new ChunkPos(31001, 31002);
        saved.set(logical, 200);
        saved.mergeLegacyAttachment(logical, 300);
        helper.assertValueEqual(saved.get(logical), 500, "legacy merge add");
        helper.assertTrue(saved.isMigrated(logical), "chunk marked migrated");

        saved.mergeLegacyAttachment(logical, 300);
        helper.assertValueEqual(saved.get(logical), 500, "second merge must not double-count");

        ChunkPos clampPos = new ChunkPos(31003, 31004);
        saved.set(clampPos, 9000);
        saved.mergeLegacyAttachment(clampPos, 5000);
        helper.assertValueEqual(saved.get(clampPos), COIConfig.MAX_DUST_LEVEL.get(), "legacy merge clamps");

        CompoundTag tag = saved.save(new CompoundTag(), level.registryAccess());
        OriDustSavedData afterRestart = OriDustSavedData.load(tag, level.registryAccess());
        helper.assertTrue(afterRestart.isMigrated(logical), "migrated flag survives NBT roundtrip");
        afterRestart.mergeLegacyAttachment(logical, 300);
        helper.assertValueEqual(afterRestart.get(logical), 500, "reload does not double-count leftover attachment");

        ServerLevel overworld = level.getServer().overworld();
        helper.assertTrue(overworld != null, "overworld present");
        OriDustSavedData overworldSaved = OriDustSavedData.get(overworld);
        ChunkPos unique = unusedChunk(overworldSaved);
        LevelChunk chunk = overworld.getChunk(unique.x, unique.z);
        overworldSaved.set(unique, 200);

        OriDustData legacy = chunk.getData(COIAttachments.CHUNK_DUST_TYPE);
        legacy.setDustLevel(300);
        Oridust.migrateLoadedChunk(overworld, chunk);
        helper.assertValueEqual(overworldSaved.get(unique), 500, "loaded-chunk migrate add");
        helper.assertValueEqual(legacy.getDustLevel(), 0, "attachment zeroed after migrate");
        helper.assertTrue(overworldSaved.isMigrated(unique), "chunk marked migrated after load path");

        legacy.setDustLevel(300);
        Oridust.migrateLoadedChunk(overworld, chunk);
        helper.assertValueEqual(overworldSaved.get(unique), 500, "second migrate of leftover attachment does not double-count");
        helper.assertValueEqual(legacy.getDustLevel(), 0, "attachment still zero after second migrate");
        helper.succeed();
    }

    /**
     * Without Sable, WorldSpace is identity — vanilla ChunkPos.
     */
    @GameTest(template = "empty", batch = "oridust")
    public static void worldSpaceIdentityWithoutSable(GameTestHelper helper) {
        helper.assertFalse(WorldSpace.isSableLoaded(), "sable must not be on the GameTest classpath");

        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(32, 64, -48);
        helper.assertValueEqual(WorldSpace.toDustChunk(level, pos), new ChunkPos(pos), "BlockPos identity");

        BlockPos millPos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.assertValueEqual(
                WorldSpace.toDustChunk(level, millPos),
                new ChunkPos(millPos),
                "absolute test-structure pos identity"
        );
        helper.succeed();
    }

    /**
     * Picks a logical chunk that is not already in the Overworld store, so a
     * reused GameTest world cannot skip {@link Oridust#migrateLoadedChunk}.
     */
    private static ChunkPos unusedChunk(OriDustSavedData saved) {
        for (int i = 0; i < 2048; i++) {
            ChunkPos pos = new ChunkPos(32 + i, 32);
            if (!saved.isMigrated(pos) && saved.get(pos) == 0) {
                return pos;
            }
        }
        throw new IllegalStateException("Could not find an unmigrated ChunkPos for GameTest");
    }
}
