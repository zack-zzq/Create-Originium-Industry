package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import com.mealuet.create_originium_industry.index.COIEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Checks each player's current chunk dust level and applies
 * {@link com.mealuet.create_originium_industry.effect.OriDustSicknessEffect}
 * when the threshold is exceeded.
 * <p>
 * In Phase 3 this will be expanded with per-player exposure & infection values
 * via a dedicated PlayerExposureData attachment.
 */
public class PlayerExposureHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

        int checkInterval = COIConfig.EFFECT_CHECK_INTERVAL.get();
        if (serverPlayer.tickCount % checkInterval != 0) return;

        LevelChunk chunk = serverPlayer.level().getChunkAt(serverPlayer.blockPosition());
        COIAttachments.getChunkDustData(chunk).ifPresent(data -> {
            int dustLevel = data.getDustLevel();
            int threshold = COIConfig.DUST_EFFECT_THRESHOLD.get();

            if (dustLevel > threshold) {
                int perLevel = COIConfig.DUST_PER_EFFECT_LEVEL.get();
                int maxAmp = COIConfig.MAX_EFFECT_AMPLIFIER.get();
                int amplifier = Math.min(maxAmp, (dustLevel - threshold) / perLevel);

                serverPlayer.addEffect(new MobEffectInstance(
                        COIEffects.ORI_DUST_SICKNESS_EFFECT,
                        checkInterval + 5,
                        amplifier,
                        true, false, true
                ));
            }
        });
    }
}
