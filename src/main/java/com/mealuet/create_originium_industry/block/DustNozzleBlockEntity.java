package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.DustRedirect;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

/**
 * Reads the supporting Encased Fan's airflow and periodically redirects
 * chunk dust downwind via {@link DustRedirect}.
 */
public class DustNozzleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final String NBT_LAST_MOVED = "LastMoved";
    private static final String NBT_HAS_FLOW = "HasFlow";

    private int lastMoved;
    private boolean hasFlow;
    /**
     * GameTest-only airflow, used when no kinetic fan network is present.
     */
    private Direction testFlow;
    private float testSpeed;

    public DustNozzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction flow = currentFlow();
        float speed = currentSpeed();
        boolean flowing = flow != null && Math.abs(speed) > 0;
        if (flowing != hasFlow) {
            hasFlow = flowing;
            notifyUpdate();
        }
        if (!flowing) {
            return;
        }
        int interval = COIConfig.NOZZLE_TRANSFER_INTERVAL.get();
        if (level.getGameTime() % interval != 0) {
            return;
        }
        int requested = Math.max(1, (int) (COIConfig.NOZZLE_TRANSFER_AMOUNT.get() * speedMultiplier(speed)));
        int moved = DustRedirect.redirect(serverLevel, worldPosition, flow, requested);
        if (moved != lastMoved) {
            lastMoved = moved;
            notifyUpdate();
        }
        setChanged();
    }

    public boolean hasAirflow() {
        return currentFlow() != null && Math.abs(currentSpeed()) > 0;
    }

    public int lastMoved() {
        return lastMoved;
    }

    /**
     * GameTest helper: pretend a fan is blowing {@code flow} at {@code speed}.
     */
    public void activateAirflowForGameTest(Direction flow, float speed) {
        this.testFlow = flow;
        this.testSpeed = speed;
        this.hasFlow = flow != null && Math.abs(speed) > 0;
        setChanged();
    }

    public int redirectNowForGameTest(int requested) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        Direction flow = currentFlow();
        if (flow == null) {
            return 0;
        }
        lastMoved = DustRedirect.redirect(serverLevel, worldPosition, flow, requested);
        hasFlow = true;
        notifyUpdate();
        return lastMoved;
    }

    public void sendStatusMessage(ServerPlayer player) {
        if (!hasAirflow()) {
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_nozzle.no_airflow"));
            return;
        }
        Direction flow = currentFlow();
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_dust_nozzle.status",
                flow != null ? flow.getSerializedName() : "?",
                lastMoved
        ));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            return true;
        }
        if (!hasFlow) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_nozzle.goggle.no_airflow")));
        } else {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_nozzle.goggle.flow",
                    lastMoved
            )));
        }
        return true;
    }

    private Direction currentFlow() {
        if (testFlow != null) {
            return testFlow;
        }
        IAirCurrentSource source = attachedFan();
        return source == null ? null : source.getAirFlowDirection();
    }

    private float currentSpeed() {
        if (testFlow != null) {
            return testSpeed;
        }
        IAirCurrentSource source = attachedFan();
        return source == null ? 0f : source.getSpeed();
    }

    private IAirCurrentSource attachedFan() {
        if (level == null) {
            return null;
        }
        Direction support = getBlockState().getValue(DustNozzleBlock.FACING).getOpposite();
        BlockEntity be = level.getBlockEntity(worldPosition.relative(support));
        return be instanceof IAirCurrentSource source ? source : null;
    }

    public static double speedMultiplier(float speed) {
        double reference = Math.max(1.0, COIConfig.NOZZLE_SPEED_REFERENCE.get());
        double max = COIConfig.NOZZLE_MAX_SPEED_MULTIPLIER.get();
        return Math.max(1.0, Math.min(max, Math.abs(speed) / reference));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt(NBT_LAST_MOVED, lastMoved);
        tag.putBoolean(NBT_HAS_FLOW, hasFlow);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        lastMoved = tag.getInt(NBT_LAST_MOVED);
        hasFlow = tag.getBoolean(NBT_HAS_FLOW);
    }
}
