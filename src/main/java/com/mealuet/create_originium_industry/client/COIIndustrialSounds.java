package com.mealuet.create_originium_industry.client;

import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.core.audio.IndustrialSoundPolicy;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.VisibleDust;
import com.mealuet.create_originium_industry.index.COISounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Client looping SFX for filters, high-dust ambience, and the power core.
 * Gated by {@link COIClientOptions}: master density, reduce-flicker cadence,
 * and particle simplify/density for dust ambience.
 */
public final class COIIndustrialSounds {

    private static final double HEAR_RANGE_SQR = 16.0 * 16.0;

    private static LoopingIndustrialSound filterLoop;
    private static LoopingIndustrialSound dustLoop;
    private static LoopingIndustrialSound reactorSteadyLoop;
    private static LoopingIndustrialSound reactorAlarmLoop;

    private COIIndustrialSounds() {}

    public static void tick(Minecraft mc, LocalPlayer player) {
        if (mc.level == null || !COIClientOptions.industrialSoundsEnabled()
                || COIClientOptions.machineSoundVolume() <= 0.0F) {
            stopAll();
            return;
        }
        tickFilter(mc, player);
        tickReactor(mc, player);
        tickDust(mc, player);
    }

    public static void stopAll() {
        requestStop(filterLoop);
        requestStop(dustLoop);
        requestStop(reactorSteadyLoop);
        requestStop(reactorAlarmLoop);
        filterLoop = null;
        dustLoop = null;
        reactorSteadyLoop = null;
        reactorAlarmLoop = null;
    }

    private static void tickFilter(Minecraft mc, LocalPlayer player) {
        DustFilterBlockEntity nearest = null;
        double best = HEAR_RANGE_SQR;
        for (BlockEntity be : nearbyBlockEntities(mc.level, player)) {
            if (be instanceof DustFilterBlockEntity filter
                    && IndustrialSoundPolicy.filterWorking(filter.hasSieve(), filter.getSpeed())) {
                double d = distanceSqr(player, be.getBlockPos());
                if (d < best) {
                    best = d;
                    nearest = filter;
                }
            }
        }
        float volume = nearest == null ? 0.0F : COIClientOptions.machineSoundVolume() * 0.55F;
        BlockPos pos = nearest == null ? player.blockPosition() : nearest.getBlockPos();
        filterLoop = maintain(mc, filterLoop, COISounds.FILTER_WORK.get(), SoundSource.BLOCKS, false, pos, volume);
    }

    private static void tickReactor(Minecraft mc, LocalPlayer player) {
        PowerCoreBlockEntity nearest = null;
        double best = HEAR_RANGE_SQR;
        for (BlockEntity be : nearbyBlockEntities(mc.level, player)) {
            if (be instanceof PowerCoreBlockEntity core && core.isGenerating()) {
                double d = distanceSqr(player, be.getBlockPos());
                if (d < best) {
                    best = d;
                    nearest = core;
                }
            }
        }
        IndustrialSoundPolicy.ReactorCue cue = nearest == null
                ? IndustrialSoundPolicy.ReactorCue.NONE
                : IndustrialSoundPolicy.reactorCue(
                        nearest.isGenerating(),
                        nearest.snapshot().stability(),
                        nearest.instability()
                );
        BlockPos pos = nearest == null ? player.blockPosition() : nearest.getBlockPos();
        float machine = COIClientOptions.machineSoundVolume();
        float pulse = 1.0F;
        if (cue == IndustrialSoundPolicy.ReactorCue.ALARM && COIClientOptions.pulseAudio()) {
            pulse = 0.72F + 0.28F * (0.5F + 0.5F * Mth.sin(player.tickCount / 3.5F));
        }
        float steady = cue == IndustrialSoundPolicy.ReactorCue.STEADY ? machine * 0.5F : 0.0F;
        float alarm = cue == IndustrialSoundPolicy.ReactorCue.ALARM ? machine * 0.7F * pulse : 0.0F;
        reactorSteadyLoop = maintain(mc, reactorSteadyLoop, COISounds.REACTOR_STEADY.get(), SoundSource.BLOCKS, false, pos, steady);
        reactorAlarmLoop = maintain(mc, reactorAlarmLoop, COISounds.REACTOR_ALARM.get(), SoundSource.BLOCKS, false, pos, alarm);
    }

    private static void tickDust(Minecraft mc, LocalPlayer player) {
        float ambient = COIClientOptions.ambientDustSoundVolume();
        DustLevel level = DustLevel.SAFE;
        if (ambient > 0.0F && VisibleDust.dustSyncEnabled()) {
            level = DustLevel.fromDust(VisibleDust.chunkDustAt(player));
        }
        float gain = IndustrialSoundPolicy.dustAmbienceGain(level) * ambient * 0.45F;
        dustLoop = maintain(
                mc,
                dustLoop,
                COISounds.HIGH_DUST.get(),
                SoundSource.AMBIENT,
                true,
                player.blockPosition(),
                gain
        );
    }

    private static LoopingIndustrialSound maintain(
            Minecraft mc,
            LoopingIndustrialSound current,
            SoundEvent event,
            SoundSource source,
            boolean relative,
            BlockPos pos,
            float volume
    ) {
        SoundManager manager = mc.getSoundManager();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (volume <= 0.001F) {
            if (current != null) {
                current.requestStop();
                if (current.isStopped() || !manager.isActive(current)) {
                    return null;
                }
            }
            return current;
        }
        if (current == null || current.isStopped() || !manager.isActive(current)) {
            current = new LoopingIndustrialSound(event, source, relative, x, y, z);
            current.setFadeSpeed(COIClientOptions.reduceFlicker() ? 0.03F : 0.09F);
            current.setTarget(volume, x, y, z);
            manager.play(current);
            return current;
        }
        current.setFadeSpeed(COIClientOptions.reduceFlicker() ? 0.03F : 0.09F);
        current.setTarget(volume, x, y, z);
        return current;
    }

    private static void requestStop(LoopingIndustrialSound sound) {
        if (sound != null) {
            sound.requestStop();
        }
    }

    private static Iterable<BlockEntity> nearbyBlockEntities(Level level, LocalPlayer player) {
        List<BlockEntity> found = new ArrayList<>();
        ChunkPos origin = player.chunkPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = origin.x + dx;
                int cz = origin.z + dz;
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                found.addAll(chunk.getBlockEntities().values());
            }
        }
        return found;
    }

    private static double distanceSqr(LocalPlayer player, BlockPos pos) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dy = player.getY() - (pos.getY() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }
}
