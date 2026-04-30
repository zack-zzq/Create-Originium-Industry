package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages an in-memory cache of per-chunk dust levels for currently loaded overworld chunks.
 * Syncs cache with persistent NeoForge Attachment data on chunk load/unload.
 */
public class DustCacheManager {

    private static final Map<ChunkPos, Integer> dustLevels = new ConcurrentHashMap<>();

    // --- Public API ---

    public static int getDustLevel(ChunkPos pos) {
        return dustLevels.getOrDefault(pos, 0);
    }

    public static boolean isChunkLoaded(ChunkPos pos) {
        return dustLevels.containsKey(pos);
    }

    public static Map<ChunkPos, Integer> getSnapshot() {
        return Collections.unmodifiableMap(dustLevels);
    }

    public static boolean isEmpty() {
        return dustLevels.isEmpty();
    }

    /**
     * Updates the dust level in both the in-memory cache and the persistent chunk attachment.
     */
    public static void syncDustLevel(ServerLevel level, ChunkPos pos, int newLevel) {
        if (newLevel < 0) newLevel = 0;
        dustLevels.put(pos, newLevel);

        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        if (chunk != null) {
            Optional<OriDustData> dataOpt = COIAttachments.getChunkDustData(chunk);
            if (dataOpt.isPresent()) {
                OriDustData data = dataOpt.get();
                if (data.getDustLevel() != newLevel) {
                    data.setDustLevel(newLevel);
                    chunk.setUnsaved(true);
                }
            } else {
                CreateOriginiumIndustry.LOGGER.warn("Failed to get OriDustData for chunk {} during sync.", pos);
            }
        } else {
            // Chunk became unloaded; remove from cache
            dustLevels.remove(pos);
        }
    }

    /**
     * Removes a chunk from the cache (e.g. when the chunk has become unloaded mid-tick).
     */
    public static void evict(ChunkPos pos) {
        dustLevels.remove(pos);
    }

    // --- Event Handlers ---

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == Level.OVERWORLD
                && event.getChunk() instanceof LevelChunk chunk) {
            COIAttachments.getChunkDustData(chunk).ifPresent(data ->
                    dustLevels.put(chunk.getPos(), data.getDustLevel()));
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == Level.OVERWORLD) {
            ChunkAccess chunk = event.getChunk();
            if (dustLevels.remove(chunk.getPos()) != null) {
                CreateOriginiumIndustry.LOGGER.debug("Unloaded chunk {}, removed from dust cache.", chunk.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
        dustLevels.clear();
        CreateOriginiumIndustry.LOGGER.info("Server starting, initializing dust cache.");

        int radius = COIConfig.INIT_CHUNK_RADIUS.get();

        event.getServer().getAllLevels().forEach(serverLevel -> {
            if (serverLevel.dimension() != Level.OVERWORLD) return;

            ServerChunkCache chunkSource = serverLevel.getChunkSource();
            serverLevel.getPlayers(player -> true).forEach(player -> {
                ChunkPos playerPos = player.chunkPosition();
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        ChunkPos pos = new ChunkPos(playerPos.x + x, playerPos.z + z);
                        LevelChunk chunk = chunkSource.getChunkNow(pos.x, pos.z);
                        if (chunk != null) {
                            dustLevels.computeIfAbsent(pos, cp -> {
                                Optional<OriDustData> dataOpt = COIAttachments.getChunkDustData(chunk);
                                return dataOpt.map(OriDustData::getDustLevel).orElse(0);
                            });
                        }
                    }
                }
            });

            CreateOriginiumIndustry.LOGGER.debug("Dust cache initialized for {}. Size: {}",
                    serverLevel.dimension().location(), dustLevels.size());
        });
    }
}
