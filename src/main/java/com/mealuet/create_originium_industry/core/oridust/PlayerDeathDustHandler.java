package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Handles dust burst on player death from originium exposure.
 * When a player dies while having high exposure/infection,
 * originium dust is released into the chunk as a contamination burst.
 */
public class PlayerDeathDustHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;
        if (!COIConfig.ENABLE_DEATH_DUST_BURST.get()) return;

        PlayerExposureData data = COIAttachments.getPlayerExposure(serverPlayer);
        int scaledBurst = computeBurstAmount(data.getExposure(), data.getInfection());
        if (scaledBurst <= 0) return;

        ChunkPos chunkPos = WorldSpace.toDustChunk(serverPlayer);
        OriginiumDustManager.addDust(serverLevel, chunkPos, scaledBurst, DustReason.DEATH_BURST);

        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Player {} died with exposure={}, infection={}, burst {} dust at chunk [{}, {}]",
                    serverPlayer.getName().getString(), data.getExposure(), data.getInfection(),
                    scaledBurst, chunkPos.x, chunkPos.z
            );
        }
    }

    /**
     * After NeoForge copies {@code copyOnDeath} attachments, scale exposure /
     * infection by the configured retain fractions. Defaults clear exposure
     * and keep a quarter of infection so singleplayer deaths are not a spiral.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        PlayerExposureData source = COIAttachments.getPlayerExposure(event.getOriginal());
        PlayerExposureData dest = COIAttachments.getPlayerExposure(event.getEntity());
        dest.setExposure(PlayerExposureData.retain(source.getExposure(), retainOrDefault(COIConfig.DEATH_EXPOSURE_RETAIN, 0.0)));
        dest.setInfection(PlayerExposureData.retain(source.getInfection(), retainOrDefault(COIConfig.DEATH_INFECTION_RETAIN, 0.25)));
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DustSyncTracker.markExposureDirty(serverPlayer);
        }
    }

    private static double retainOrDefault(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
        return COIConfig.COMMON_SPEC.isLoaded() ? value.get() : fallback;
    }

    /**
     * Dust released on death, or 0 if contamination is below the configured
     * threshold / the burst amount is 0.
     */
    public static int computeBurstAmount(int exposure, int infection) {
        if (!COIConfig.ENABLE_DEATH_DUST_BURST.get()) {
            return 0;
        }
        int min = COIConfig.DEATH_BURST_MIN_CONTAMINATION.get();
        if (exposure < min && infection < min) {
            return 0;
        }
        int burstAmount = COIConfig.DEATH_DUST_BURST_AMOUNT.get();
        if (burstAmount <= 0) {
            return 0;
        }
        double divisor = Math.max(1.0, COIConfig.DEATH_BURST_SCALE_DIVISOR.get());
        double scale = Math.min(COIConfig.DEATH_BURST_MAX_SCALE.get(), (exposure + infection) / divisor);
        return Math.max(1, (int) (burstAmount * scale));
    }
}
