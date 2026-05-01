package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

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
        int exposure = data.getExposure();
        int infection = data.getInfection();

        // Only burst if player had significant exposure or infection
        if (exposure < 100 && infection < 100) return;

        int burstAmount = COIConfig.DEATH_DUST_BURST_AMOUNT.get();

        // Scale burst by exposure + infection ratio (more contaminated = bigger burst)
        double scale = Math.min(2.0, (exposure + infection) / 1000.0);
        int scaledBurst = Math.max(1, (int) (burstAmount * scale));

        ChunkPos chunkPos = serverPlayer.chunkPosition();
        OriginiumDustManager.addDust(serverLevel, chunkPos, scaledBurst, DustReason.DEATH_BURST);

        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] Player {} died with exposure={}, infection={}, burst {} dust at chunk [{}, {}]",
                    serverPlayer.getName().getString(), exposure, infection,
                    scaledBurst, chunkPos.x, chunkPos.z
            );
        }
    }
}
