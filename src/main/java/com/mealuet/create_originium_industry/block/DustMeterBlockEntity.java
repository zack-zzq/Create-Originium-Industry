package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.a11y.AccessibilityCues;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.ProtectionHooks;
import com.mealuet.create_originium_industry.core.oridust.VisibleDust;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Server-accurate dust readout. Comparator uses the BE snapshot.
 * Goggles / status share {@link com.mealuet.create_originium_industry.core.oridust.VisibleDust}
 * with HUD and debug so two players looking at the same chunk agree after nearby sync.
 */
public class DustMeterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final String NBT_DUST = "Dust";
    private static final String NBT_RISK = "Risk";
    private static final String NBT_PROTECTION = "ProtectionPercent";

    private int syncedDust;
    private String riskId = DustLevel.SAFE.getId();
    private int protectionPercent;

    public DustMeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(COIConfig.meterSyncInterval());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int dust = OriginiumDustManager.getDustAt(serverLevel, worldPosition);
        DustLevel risk = DustLevel.fromDust(dust);
        int protection = nearestProtectionPercent(serverLevel);
        if (dust != syncedDust || !risk.getId().equals(riskId) || protection != protectionPercent) {
            syncedDust = dust;
            riskId = risk.getId();
            protectionPercent = protection;
            notifyUpdate();
            setChanged();
            serverLevel.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public int syncedDust() {
        return syncedDust;
    }

    /**
     * Value shown to players (goggles / status). Server is SavedData;
     * client prefers the nearby sync cache and falls back to the BE snapshot.
     */
    public int displayedDust() {
        if (level == null) {
            return syncedDust;
        }
        return VisibleDust.chunkDustOrFallback(level, WorldSpace.toDustChunk(level, worldPosition), syncedDust);
    }

    public DustLevel risk() {
        return DustLevel.fromDust(displayedDust());
    }

    public int protectionPercent() {
        return protectionPercent;
    }

    public int comparatorSignal() {
        int full = Math.max(1, COIConfig.METER_COMPARATOR_FULL_DUST.get());
        return Mth.clamp(Mth.ceil(syncedDust * 15.0 / full), 0, 15);
    }

    public void sendStatusMessage(ServerPlayer player) {
        refreshFromServer();
        DustLevel level = DustLevel.fromDust(displayedDust());
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_dust_meter.status",
                displayedDust(),
                Component.translatable(level.getLangKey())
        ));
        if (protectionPercent > 0) {
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_meter.protection.active",
                    protectionPercent
            ));
        } else {
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_meter.protection.none"
            ));
        }
    }

    /**
     * Force a live server read (right-click / GameTest). Goggles use the
     * last lazy-tick snapshot so the client packet stays consistent.
     */
    public void refreshFromServer() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        syncedDust = OriginiumDustManager.getDustAt(serverLevel, worldPosition);
        riskId = DustLevel.fromDust(syncedDust).getId();
        protectionPercent = nearestProtectionPercent(serverLevel);
        notifyUpdate();
        setChanged();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            tooltip.add(Component.literal("    ").append(AccessibilityCues.dustRiskLabel(risk())));
            return true;
        }
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_dust_meter.goggle.dust",
                displayedDust()
        )));
        tooltip.add(Component.literal("    ").append(AccessibilityCues.dustRiskLabel(risk())));
        if (protectionPercent > 0) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_meter.goggle.protection",
                    protectionPercent
            )));
        } else {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_meter.goggle.no_protection"
            )));
        }
        return true;
    }

    private int nearestProtectionPercent(ServerLevel serverLevel) {
        Player nearest = null;
        double best = 64.0;
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(8))) {
            double dist = player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            if (dist < best) {
                best = dist;
                nearest = player;
            }
        }
        if (nearest == null) {
            return 0;
        }
        return ProtectionHooks.exposureReductionPercent(nearest);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt(NBT_DUST, syncedDust);
        tag.putString(NBT_RISK, riskId);
        tag.putInt(NBT_PROTECTION, protectionPercent);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        syncedDust = tag.getInt(NBT_DUST);
        riskId = tag.contains(NBT_RISK) ? tag.getString(NBT_RISK) : DustLevel.SAFE.getId();
        protectionPercent = tag.getInt(NBT_PROTECTION);
        if (clientPacket && hasLevel() && level != null && level.isClientSide) {
            VisibleDust.applyClientDust(
                    new long[] {WorldSpace.toDustChunk(level, worldPosition).toLong()},
                    new int[] {syncedDust}
            );
        }
    }
}
