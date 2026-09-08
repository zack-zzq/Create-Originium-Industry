package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.ByproductBuffer;
import com.mealuet.create_originium_industry.core.oridust.DustByproduct;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Passive {@link IDustPurifier} for a placed {@code originium_dust_sieve}.
 * No rotation required: attaching it to a Basin / mill / mixer is enough.
 * Durability matches the kinetic filter's inserted sieve; worn-out sieves break.
 */
public class ProcessSieveBlockEntity extends SmartBlockEntity implements IDustPurifier, IHaveGoggleInformation {

    private static final String NBT_DURABILITY = "SieveDurability";

    private int durability;
    private final ByproductBuffer byproduct = new ByproductBuffer();

    public ProcessSieveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.durability = COIConfig.processSieveDurability();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public boolean isPurifierActive() {
        return durability > 0;
    }

    @Override
    public double emissionCaptureFactor() {
        return isPurifierActive() ? COIConfig.PROCESS_SIEVE_EMISSION_CAPTURE.get() : 0.0;
    }

    @Override
    public int acceptCapturedDust(int captured) {
        if (captured <= 0 || !isPurifierActive()) {
            return 0;
        }
        durability--;
        int items = byproduct.add(captured, COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get());
        DustByproduct.dropItems(level, worldPosition, items);
        setChanged();
        if (durability <= 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlock(worldPosition, false);
        }
        return items;
    }

    public int durability() {
        return durability;
    }

    public int byproductStored() {
        return byproduct.stored();
    }

    /**
     * GameTest helper: full durability without placing via item.
     */
    public void activatePurifierForGameTest() {
        this.durability = COIConfig.processSieveDurability();
        setChanged();
    }

    public void sendStatusMessage(ServerPlayer player) {
        int max = COIConfig.processSieveDurability();
        int percent = max > 0 ? (durability * 100 / max) : 0;
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_dust_sieve.status",
                durability, max, percent
        ));
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_dust_sieve.capture",
                String.format("%.0f", emissionCaptureFactor() * 100)
        ));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            return true;
        }
        int max = COIConfig.processSieveDurability();
        int percent = max > 0 ? (durability * 100 / max) : 0;
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_dust_sieve.goggle.durability",
                percent
        )));
        if (COIClientOptions.verboseUi()) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_sieve.goggle.detail",
                    durability, max
            )));
        }
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_dust_sieve.goggle.capture",
                String.format("%.0f", emissionCaptureFactor() * 100)
        )));
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt(NBT_DURABILITY, durability);
        byproduct.save(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        durability = tag.getInt(NBT_DURABILITY);
        byproduct.load(tag);
    }
}
