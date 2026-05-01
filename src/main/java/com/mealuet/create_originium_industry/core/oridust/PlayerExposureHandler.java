package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIAttachments;
import com.mealuet.create_originium_industry.index.COIEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Per-tick handler for player originium exposure and infection.
 * <p>
 * <b>Exposure</b> (暴露值):
 * <ul>
 *   <li>Increases when player is in a chunk with dust above threshold</li>
 *   <li>Gain rate: {@code (dustLevel - threshold) / dustPerLevel * gainMultiplier}</li>
 *   <li>Decays naturally when in a safe area (dust below threshold)</li>
 *   <li>Drives {@code ori_dust_sickness} effect amplifier</li>
 * </ul>
 * <p>
 * <b>Infection</b> (感染值):
 * <ul>
 *   <li>Increases slowly when exposure exceeds the infection threshold</li>
 *   <li>Does NOT decay naturally — permanent contamination</li>
 *   <li>At high levels, causes effects even in clean areas</li>
 * </ul>
 */
public class PlayerExposureHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

        int checkInterval = COIConfig.EFFECT_CHECK_INTERVAL.get();
        if (serverPlayer.tickCount % checkInterval != 0) return;

        PlayerExposureData data = COIAttachments.getPlayerExposure(serverPlayer);
        ChunkPos chunkPos = serverPlayer.chunkPosition();
        int dustLevel = OriginiumDustManager.getDust(chunkPos);
        int threshold = COIConfig.DUST_EFFECT_THRESHOLD.get();

        // --- Update Exposure ---
        if (dustLevel > threshold) {
            // In a dusty area: gain exposure
            int perLevel = COIConfig.DUST_PER_EFFECT_LEVEL.get();
            double gainMultiplier = COIConfig.EXPOSURE_GAIN_MULTIPLIER.get();
            int gain = Math.max(1, (int) ((dustLevel - threshold) / (double) perLevel * gainMultiplier));
            data.addExposure(gain);
        } else {
            // In a clean area: decay exposure
            int decayRate = COIConfig.EXPOSURE_DECAY_RATE.get();
            if (decayRate > 0 && data.getExposure() > 0) {
                data.addExposure(-decayRate);
            }
        }

        // --- Update Infection ---
        int infectionThreshold = COIConfig.INFECTION_THRESHOLD.get();
        if (data.getExposure() > infectionThreshold) {
            // Accumulate infection when exposure is high
            double infectionMultiplier = COIConfig.INFECTION_GAIN_MULTIPLIER.get();
            int infectionGain = Math.max(1, (int) (infectionMultiplier));
            data.addInfection(infectionGain);
        }

        // --- Apply Effects ---
        int effectiveExposure = data.getExposure();

        // Infection contributes to effective exposure even in clean areas
        // (at 50% rate — infection causes lingering symptoms)
        if (data.getInfection() > 0) {
            effectiveExposure = Math.max(effectiveExposure, data.getInfection() / 2);
        }

        if (effectiveExposure > 0) {
            int maxAmp = COIConfig.MAX_EFFECT_AMPLIFIER.get();
            // Amplifier based on effective exposure: every 200 exposure = +1 amplifier
            int amplifier = Math.min(maxAmp, effectiveExposure / 200);

            if (amplifier >= 0 && effectiveExposure >= 100) {
                serverPlayer.addEffect(new MobEffectInstance(
                        COIEffects.ORI_DUST_SICKNESS_EFFECT,
                        checkInterval + 5,
                        amplifier,
                        true, false, true
                ));
            }
        }

        // --- Debug Logging ---
        if (COIConfig.ENABLE_DEBUG_LOGGING.get() && serverPlayer.tickCount % (checkInterval * 10) == 0) {
            CreateOriginiumIndustry.LOGGER.debug(
                    "[OriDust] Player {} — exposure: {}, infection: {}, chunk dust: {}",
                    serverPlayer.getName().getString(), data.getExposure(), data.getInfection(), dustLevel
            );
        }
    }
}
