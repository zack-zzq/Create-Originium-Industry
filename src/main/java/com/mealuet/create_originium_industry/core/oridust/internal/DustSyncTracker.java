package com.mealuet.create_originium_industry.core.oridust.internal;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.OriDustSavedData;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.core.perf.PerfProbe;
import com.mealuet.create_originium_industry.index.COIAttachments;
import com.mealuet.create_originium_industry.network.DustSyncPayload;
import com.mealuet.create_originium_industry.network.ExposureSyncPayload;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side dirty set and low-frequency flush for nearby dust + local exposure.
 * <p>
 * Does not iterate SavedData. Far dirty keys stay queued until a player is
 * close enough to include them in a window (or they are overwritten).
 */
public final class DustSyncTracker {

    private static final LongOpenHashSet dirtyChunks = new LongOpenHashSet();
    private static final Set<UUID> exposureDirty = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> needsWindow = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long2IntOpenHashMap> lastSent = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastCenter = new ConcurrentHashMap<>();

    private DustSyncTracker() {}

    public static void markDustDirty(ChunkPos pos) {
        synchronized (dirtyChunks) {
            dirtyChunks.add(pos.toLong());
        }
    }

    public static void markDustDirty(long chunkKey) {
        synchronized (dirtyChunks) {
            dirtyChunks.add(chunkKey);
        }
    }

    public static void markExposureDirty(ServerPlayer player) {
        if (player != null) {
            exposureDirty.add(player.getUUID());
        }
    }

    public static boolean isDustDirty(long chunkKey) {
        synchronized (dirtyChunks) {
            return dirtyChunks.contains(chunkKey);
        }
    }

    public static int dirtyCount() {
        synchronized (dirtyChunks) {
            return dirtyChunks.size();
        }
    }

    public static void clearForTests() {
        synchronized (dirtyChunks) {
            dirtyChunks.clear();
        }
        exposureDirty.clear();
        needsWindow.clear();
        lastSent.clear();
        lastCenter.clear();
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }
        if (serverLevel.getGameTime() % COIConfig.dustSyncInterval() != 0) {
            return;
        }
        flush(serverLevel);
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            needsWindow.add(player.getUUID());
            exposureDirty.add(player.getUUID());
            if (player.serverLevel().dimension() == Level.OVERWORLD) {
                flushPlayer(player.serverLevel(), player);
            } else {
                flushExposure(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        lastSent.remove(id);
        lastCenter.remove(id);
        needsWindow.remove(id);
        exposureDirty.remove(id);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        clearForTests();
    }

    static void flush(ServerLevel overworld) {
        long start = System.nanoTime();
        try {
            flushInner(overworld);
        } finally {
            PerfProbe.recordSync(System.nanoTime() - start);
        }
    }

    /** GameTest / debug: run one client-sync flush immediately. */
    public static void flushNow(ServerLevel overworld) {
        flush(overworld);
    }

    private static void flushInner(ServerLevel overworld) {
        if (COIConfig.syncDustToClients()) {
            LongOpenHashSet dirtySnapshot;
            synchronized (dirtyChunks) {
                dirtySnapshot = new LongOpenHashSet(dirtyChunks);
            }
            LongOpenHashSet sentKeys = new LongOpenHashSet();
            for (ServerPlayer player : overworld.players()) {
                sentKeys.addAll(flushDust(overworld, player, dirtySnapshot));
            }
            synchronized (dirtyChunks) {
                dirtyChunks.removeAll(sentKeys);
            }
        } else {
            synchronized (dirtyChunks) {
                dirtyChunks.clear();
            }
        }
        for (ServerPlayer player : overworld.getServer().getPlayerList().getPlayers()) {
            flushExposure(player);
        }
    }

    private static void flushPlayer(ServerLevel level, ServerPlayer player) {
        if (COIConfig.syncDustToClients()) {
            LongOpenHashSet dirtySnapshot;
            synchronized (dirtyChunks) {
                dirtySnapshot = new LongOpenHashSet(dirtyChunks);
            }
            LongOpenHashSet sent = flushDust(level, player, dirtySnapshot);
            synchronized (dirtyChunks) {
                dirtyChunks.removeAll(sent);
            }
        }
        flushExposure(player);
    }

    private static LongOpenHashSet flushDust(ServerLevel level, ServerPlayer player, LongOpenHashSet dirtySnapshot) {
        LongOpenHashSet sentKeys = new LongOpenHashSet();
        if (!canSendToPlayer(player) || !COIConfig.syncDustToClients()) {
            return sentKeys;
        }
        UUID id = player.getUUID();
        ChunkPos center = WorldSpace.toDustChunk(player);
        int radius = COIConfig.dustSyncRadius(COIConfig.isDedicated(level.getServer()));
        Long2IntOpenHashMap last = lastSent.computeIfAbsent(id, uuid -> {
            Long2IntOpenHashMap map = new Long2IntOpenHashMap();
            map.defaultReturnValue(DustSyncPlanner.NEVER_SENT);
            return map;
        });
        Long previousCenter = lastCenter.get(id);
        boolean forceWindow = needsWindow.contains(id)
                || previousCenter == null
                || previousCenter != center.toLong();
        OriDustSavedData data = OriDustSavedData.get(level);
        DustSyncPlanner.DustDelta delta = DustSyncPlanner.plan(
                center,
                radius,
                dirtySnapshot,
                key -> last.get(key),
                key -> data.get(key),
                forceWindow
        );
        if (!delta.isEmpty()) {
            sendQuietly(player, new DustSyncPayload(delta.keys(), delta.values()));
            DustSyncPlanner.applySent(last, delta);
            for (long key : delta.keys()) {
                sentKeys.add(key);
            }
        }
        DustSyncPlanner.pruneOutsideRadius(last, center, radius);
        lastCenter.put(id, center.toLong());
        needsWindow.remove(id);
        return sentKeys;
    }

    private static void flushExposure(ServerPlayer player) {
        if (!canSendToPlayer(player)) {
            return;
        }
        UUID id = player.getUUID();
        if (!exposureDirty.contains(id)) {
            return;
        }
        PlayerExposureData exposure = COIAttachments.getPlayerExposure(player);
        sendQuietly(player, new ExposureSyncPayload(exposure.getExposure(), exposure.getInfection()));
        exposureDirty.remove(id);
    }

    static boolean canSendToPlayer(ServerPlayer player) {
        if (player == null || player.isRemoved() || player.isFakePlayer()) {
            return false;
        }
        if (player.connection == null) {
            return false;
        }
        try {
            return player.connection.isAcceptingMessages();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void sendQuietly(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        try {
            PacketDistributor.sendToPlayer(player, payload);
        } catch (RuntimeException ex) {
            if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
                CreateOriginiumIndustry.LOGGER.debug(
                        "[OriDust] skip client sync for {}: {}",
                        player.getGameProfile().getName(),
                        ex.toString()
                );
            }
        }
    }
}
