package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lifecycle, lazy attachment migration, and the diffusion <em>active set</em>.
 * <p>
 * Dust values themselves live in {@link OriDustSavedData} (Overworld SavedData).
 * This class no longer dual-writes chunk attachments. The old
 * {@code syncDustLevel} path dropped updates when {@code getChunkNow} was null;
 * SavedData writes do not depend on chunk load.
 * <p>
 * After SavedData can hold unloaded keys, diffusion/decay must not walk the
 * entire map every tick. The active set is:
 * <ul>
 *   <li>every logical chunk within {@link COIConfig#INIT_CHUNK_RADIUS} of a
 *       player (Sable-remapped), and</li>
 *   <li>recently written keys plus their 4-neighbors, so isolated machines
 *       can decay and bleed one chunk without a player nearby.</li>
 * </ul>
 * Far (inactive) pollution is left unchanged until a player approaches or the
 * chunk is written again — a simple, safe rule that avoids scanning the world.
 */
public class DustCacheManager {

    /** chunkKey → gameTime of last external write (not diffusion/decay). */
    private static final ConcurrentHashMap<Long, Long> recentlyWritten = new ConcurrentHashMap<>();

    private DustCacheManager() {}

    // --- Public API (thin wrappers over SavedData) ---

    public static int getDustLevel(ServerLevel level, ChunkPos pos) {
        return OriDustSavedData.get(level).get(pos);
    }

    /**
     * Prefer {@link #getDustLevel(ServerLevel, ChunkPos)}. Kept so
     * existing call sites that only have a ChunkPos still resolve against the
     * Overworld store once the server is running.
     */
    public static int getDustLevel(ChunkPos pos) {
        MinecraftServer server = serverOrNull();
        if (server == null) return 0;
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return 0;
        return OriDustSavedData.get(overworld).get(pos);
    }

    public static boolean isInActiveSet(ServerLevel level, ChunkPos pos) {
        return lastActiveSetContains(pos) || isRecentlyWritten(level, pos);
    }

    /**
     * Historical name: used to mean "chunk is loaded in the attachment cache".
     * Now means the chunk is in the current diffusion active set (or was
     * written recently enough to be treated as active).
     */
    public static boolean isChunkLoaded(ChunkPos pos) {
        return lastActive.contains(pos.toLong()) || recentlyWritten.containsKey(pos.toLong());
    }

    public static boolean isEmpty() {
        MinecraftServer server = serverOrNull();
        if (server == null) return true;
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld == null || OriDustSavedData.get(overworld).isEmpty();
    }

    /**
     * Snapshot of the active set for one diffusion tick. Includes zero-dust
     * chunks inside the player radius so pollution can flow into clean neighbors.
     */
    public static Map<ChunkPos, Integer> snapshotActive(ServerLevel level) {
        OriDustSavedData data = OriDustSavedData.get(level);
        Set<ChunkPos> active = collectActiveChunks(level);
        Map<ChunkPos, Integer> snapshot = new HashMap<>(Math.max(16, active.size() * 2));
        Set<Long> nextActive = ConcurrentHashMap.newKeySet();
        for (ChunkPos pos : active) {
            snapshot.put(pos, data.get(pos));
            nextActive.add(pos.toLong());
        }
        lastActive = nextActive;
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Records an external mutation (machine, filter, death, debug, migration)
     * so the key stays in the active set for {@link COIConfig#RECENT_WRITE_TTL_TICKS}.
     * Diffusion and decay must not call this, or pollution would walk across
     * the entire map one chunk per cycle.
     */
    public static void markWritten(ServerLevel level, ChunkPos pos) {
        recentlyWritten.put(pos.toLong(), level.getGameTime());
    }

    public static void persist(ServerLevel level, ChunkPos pos, int newLevel) {
        OriDustSavedData.get(level).set(pos, newLevel);
    }

    // --- Active set ---

    private static volatile java.util.Set<Long> lastActive = ConcurrentHashMap.newKeySet();

    private static boolean lastActiveSetContains(ChunkPos pos) {
        return lastActive.contains(pos.toLong());
    }

    private static boolean isRecentlyWritten(ServerLevel level, ChunkPos pos) {
        Long writtenAt = recentlyWritten.get(pos.toLong());
        if (writtenAt == null) return false;
        return level.getGameTime() - writtenAt <= COIConfig.recentWriteTtlTicks();
    }

    static Set<ChunkPos> collectActiveChunks(ServerLevel level) {
        Set<ChunkPos> active = new HashSet<>();
        int radius = COIConfig.effectiveInitChunkRadius(COIConfig.isDedicated(level.getServer()));
        long now = level.getGameTime();

        for (ServerPlayer player : level.players()) {
            ChunkPos center = WorldSpace.toDustChunk(player);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    active.add(new ChunkPos(center.x + x, center.z + z));
                }
            }
        }

        int ttl = COIConfig.recentWriteTtlTicks();
        recentlyWritten.entrySet().removeIf(entry -> now - entry.getValue() > ttl);
        boolean isolate = COIConfig.isolateMachineSpread(COIConfig.isDedicated(level.getServer()));
        recentlyWritten.forEach((key, writtenAt) -> {
            ChunkPos pos = new ChunkPos(key);
            active.add(pos);
            if (isolate) {
                return;
            }
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                active.add(new ChunkPos(pos.x + dir.getStepX(), pos.z + dir.getStepZ()));
            }
        });

        return active;
    }

    // --- Legacy attachment migration ---

    /**
     * If this Overworld chunk still has a non-zero {@code CHUNK_DUST_TYPE}
     * attachment, merge it into SavedData (add + clamp) and clear the
     * attachment. {@link OriDustSavedData} records the chunk as migrated so
     * a crash between merge and attachment save cannot double-count.
     */
    public static void migrateLoadedChunk(ServerLevel level, LevelChunk chunk) {
        if (level.dimension() != Level.OVERWORLD) return;

        chunk.getExistingData(COIAttachments.CHUNK_DUST_TYPE).ifPresent(data -> {
            int legacy = data.getDustLevel();
            if (legacy <= 0) {
                return;
            }
            OriDustSavedData saved = OriDustSavedData.get(level);
            saved.mergeLegacyAttachment(chunk.getPos(), legacy);
            markWritten(level, chunk.getPos());
            data.setDustLevel(0);
            chunk.setUnsaved(true);
            if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
                CreateOriginiumIndustry.LOGGER.debug(
                        "[OriDust] Migrated attachment dust {} from chunk {}",
                        legacy, chunk.getPos()
                );
            }
        });
    }

    // --- Event handlers ---

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.dimension() == Level.OVERWORLD
                && event.getChunk() instanceof LevelChunk chunk) {
            migrateLoadedChunk(serverLevel, chunk);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        recentlyWritten.clear();
        lastActive = ConcurrentHashMap.newKeySet();
        CreateOriginiumIndustry.LOGGER.info("Initializing originium dust SavedData (active-set radius {}).",
                COIConfig.effectiveInitChunkRadius(COIConfig.isDedicated(event.getServer())));

        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        // One-shot merge for chunks that are already loaded (spawn / player
        // radius). Remaining chunks migrate lazily on ChunkEvent.Load.
        // v1 does not scan unloaded region files.
        int radius = COIConfig.effectiveInitChunkRadius(COIConfig.isDedicated(event.getServer()));
        for (ServerPlayer player : overworld.players()) {
            ChunkPos center = WorldSpace.toDustChunk(player);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    LevelChunk chunk = overworld.getChunkSource().getChunkNow(center.x + x, center.z + z);
                    if (chunk != null) {
                        migrateLoadedChunk(overworld, chunk);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        recentlyWritten.clear();
        lastActive = ConcurrentHashMap.newKeySet();
    }

    private static MinecraftServer serverOrNull() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
