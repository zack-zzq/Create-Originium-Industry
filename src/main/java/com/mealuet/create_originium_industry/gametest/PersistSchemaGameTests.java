package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.core.PersistSchema;
import com.mealuet.create_originium_industry.core.oridust.DustCacheManager;
import com.mealuet.create_originium_industry.core.oridust.OriDustData;
import com.mealuet.create_originium_industry.core.oridust.OriDustSavedData;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Coverage for issue #49: schema versions on SavedData and attachments.
 * Unversioned (legacy) payloads must load without losing dust / exposure.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class PersistSchemaGameTests {

    private PersistSchemaGameTests() {}

    @GameTest(template = "empty", batch = "persist_schema")
    public static void savedDataLoadsUnversionedLegacyPayload(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.Provider registries = level.registryAccess();
        ChunkPos pos = new ChunkPos(41001, -41002);

        CompoundTag legacy = new CompoundTag();
        legacy.putLongArray(OriDustSavedData.NBT_CHUNK_KEYS, new long[]{pos.toLong()});
        legacy.putIntArray(OriDustSavedData.NBT_DUST_VALUES, new int[]{2468});
        legacy.putLongArray(OriDustSavedData.NBT_MIGRATED, new long[]{pos.toLong()});
        helper.assertFalse(legacy.contains(OriDustSavedData.NBT_VERSION), "legacy SavedData has no Version");
        helper.assertValueEqual(
                PersistSchema.read(legacy, OriDustSavedData.NBT_VERSION),
                PersistSchema.UNVERSIONED,
                "missing Version is 0"
        );

        CompoundTag migrated = OriDustSavedData.migrate(legacy.copy(), PersistSchema.UNVERSIONED);
        helper.assertValueEqual(
                PersistSchema.read(migrated, OriDustSavedData.NBT_VERSION),
                OriDustSavedData.SCHEMA_VERSION,
                "v0→v1 stamps Version"
        );
        helper.assertValueEqual(migrated.getIntArray(OriDustSavedData.NBT_DUST_VALUES)[0], 2468, "dust kept in migrate");

        helper.assertFalse(legacy.contains(OriDustSavedData.NBT_VERSION), "load input stays unversioned");
        OriDustSavedData loaded = OriDustSavedData.load(legacy, registries);
        helper.assertValueEqual(loaded.get(pos), 2468, "unversioned dust fog");
        helper.assertTrue(loaded.isMigrated(pos), "unversioned migrated flag");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void savedDataCurrentVersionRoundTrip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.Provider registries = level.registryAccess();
        ChunkPos pos = new ChunkPos(41003, -41004);

        OriDustSavedData data = new OriDustSavedData();
        data.set(pos, 1357);
        data.mergeLegacyAttachment(pos, 0);

        CompoundTag tag = data.save(new CompoundTag(), registries);
        helper.assertValueEqual(
                tag.getInt(OriDustSavedData.NBT_VERSION),
                OriDustSavedData.SCHEMA_VERSION,
                "save writes Version 1"
        );
        helper.assertValueEqual(OriDustSavedData.SCHEMA_VERSION, 1, "REGISTRY SavedData version");

        OriDustSavedData reloaded = OriDustSavedData.load(tag, registries);
        helper.assertValueEqual(reloaded.get(pos), 1357, "v1 dust after round-trip");
        helper.assertTrue(reloaded.isMigrated(pos), "v1 migrated flag after round-trip");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void chunkAttachmentLoadsUnversionedLegacyPayload(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();

        CompoundTag legacy = new CompoundTag();
        legacy.putInt(OriDustData.NBT_KEY, 777);
        helper.assertFalse(legacy.contains(OriDustData.NBT_VERSION), "legacy attachment has no version");

        OriDustData loaded = new OriDustData();
        loaded.deserializeNBT(registries, legacy);
        helper.assertValueEqual(loaded.getDustLevel(), 777, "unversioned DustLevel");
        helper.assertValueEqual(
                PersistSchema.read(legacy, OriDustData.NBT_VERSION),
                OriDustData.SCHEMA_VERSION,
                "migrate stamps version on the load tag"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void chunkAttachmentCurrentVersionRoundTrip(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();

        OriDustData data = new OriDustData();
        data.setDustLevel(321);
        CompoundTag tag = data.serializeNBT(registries);
        helper.assertValueEqual(tag.getInt(OriDustData.NBT_VERSION), OriDustData.SCHEMA_VERSION, "serialize writes version 1");
        helper.assertValueEqual(tag.getInt(OriDustData.NBT_KEY), 321, "serialize DustLevel");
        helper.assertValueEqual(OriDustData.SCHEMA_VERSION, 1, "REGISTRY chunk attachment version");

        OriDustData reloaded = new OriDustData();
        reloaded.deserializeNBT(registries, tag);
        helper.assertValueEqual(reloaded.getDustLevel(), 321, "v1 DustLevel after round-trip");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void unversionedChunkAttachmentStillMigratesIntoSavedData(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        helper.assertTrue(overworld != null, "overworld present");
        OriDustSavedData saved = OriDustSavedData.get(overworld);
        ChunkPos unique = unusedChunk(saved);
        LevelChunk chunk = overworld.getChunk(unique.x, unique.z);

        CompoundTag legacy = new CompoundTag();
        legacy.putInt(OriDustData.NBT_KEY, 300);
        OriDustData attachment = chunk.getData(COIAttachments.CHUNK_DUST_TYPE);
        attachment.deserializeNBT(overworld.registryAccess(), legacy);
        helper.assertValueEqual(attachment.getDustLevel(), 300, "legacy NBT applied to attachment");

        saved.set(unique, 200);
        DustCacheManager.migrateLoadedChunk(overworld, chunk);
        helper.assertValueEqual(saved.get(unique), 500, "unversioned attachment merged");
        helper.assertValueEqual(attachment.getDustLevel(), 0, "attachment zeroed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void playerExposureLoadsUnversionedLegacyPayload(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();

        CompoundTag legacy = new CompoundTag();
        legacy.putInt(PlayerExposureData.NBT_EXPOSURE, 640);
        legacy.putInt(PlayerExposureData.NBT_INFECTION, 880);
        helper.assertFalse(legacy.contains(PlayerExposureData.NBT_VERSION), "legacy exposure has no version");

        PlayerExposureData loaded = new PlayerExposureData();
        loaded.deserializeNBT(registries, legacy);
        helper.assertValueEqual(loaded.getExposure(), 640, "unversioned Exposure");
        helper.assertValueEqual(loaded.getInfection(), 880, "unversioned Infection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "persist_schema")
    public static void playerExposureCurrentVersionRoundTrip(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();

        PlayerExposureData data = new PlayerExposureData();
        data.setExposure(111);
        data.setInfection(222);
        CompoundTag tag = data.serializeNBT(registries);
        helper.assertValueEqual(
                tag.getInt(PlayerExposureData.NBT_VERSION),
                PlayerExposureData.SCHEMA_VERSION,
                "serialize writes version 1"
        );
        helper.assertValueEqual(PlayerExposureData.SCHEMA_VERSION, 1, "REGISTRY player attachment version");

        PlayerExposureData reloaded = new PlayerExposureData();
        reloaded.deserializeNBT(registries, tag);
        helper.assertValueEqual(reloaded.getExposure(), 111, "v1 Exposure after round-trip");
        helper.assertValueEqual(reloaded.getInfection(), 222, "v1 Infection after round-trip");
        helper.succeed();
    }

    private static ChunkPos unusedChunk(OriDustSavedData saved) {
        for (int i = 0; i < 2048; i++) {
            ChunkPos pos = new ChunkPos(48 + i, 48);
            if (!saved.isMigrated(pos) && saved.get(pos) == 0) {
                return pos;
            }
        }
        throw new IllegalStateException("Could not find an unmigrated ChunkPos for GameTest");
    }
}
