package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.core.oridust.internal.DustSyncTracker;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

/**
 * Stable accessors for per-player exposure and infection.
 * <p>
 * Server writes go through the {@code player_exposure_data} attachment
 * ({@link PlayerExposureData}). Client HUD / goggles should use
 * {@link VisibleDust} so they read the last sync packet.
 */
public final class PlayerExposure {

    private PlayerExposure() {}

    public static PlayerExposureData of(IAttachmentHolder holder) {
        return COIAttachments.getPlayerExposure(holder);
    }

    public static int getExposure(IAttachmentHolder holder) {
        return of(holder).getExposure();
    }

    public static int getInfection(IAttachmentHolder holder) {
        return of(holder).getInfection();
    }

    public static InfectionStage getStage(IAttachmentHolder holder) {
        return of(holder).getInfectionStage();
    }

    public static void setExposure(IAttachmentHolder holder, int value) {
        of(holder).setExposure(value);
        markDirty(holder);
    }

    public static void setInfection(IAttachmentHolder holder, int value) {
        of(holder).setInfection(value);
        markDirty(holder);
    }

    public static void addExposure(IAttachmentHolder holder, int amount) {
        of(holder).addExposure(amount);
        markDirty(holder);
    }

    public static void addInfection(IAttachmentHolder holder, int amount) {
        of(holder).addInfection(amount);
        markDirty(holder);
    }

    /**
     * Queues a local-player exposure/infection sync packet.
     */
    public static void markDirty(IAttachmentHolder holder) {
        if (holder instanceof ServerPlayer player) {
            DustSyncTracker.markExposureDirty(player);
        }
    }
}
