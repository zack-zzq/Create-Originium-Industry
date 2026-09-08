package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
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
 * Supercooling attachment. Each successful supercooling recipe wears
 * durability; a worn-out chamber breaks (no GUI).
 */
public class CoolingChamberBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final String NBT_DURABILITY = "ChamberDurability";
    private static final String NBT_VERSION = "version";

    private int durability;

    public CoolingChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.durability = COIConfig.coolingChamberDurability();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public boolean isChamberActive() {
        return durability > 0;
    }

    public int durability() {
        return durability;
    }

    /**
     * GameTest helper: full durability without placing via item.
     */
    public void activateForGameTest() {
        this.durability = COIConfig.coolingChamberDurability();
        setChanged();
    }

    public void consumeOperation() {
        if (durability <= 0) {
            return;
        }
        durability--;
        setChanged();
        if (durability <= 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlock(worldPosition, false);
        }
    }

    public void sendStatusMessage(ServerPlayer player) {
        int max = COIConfig.coolingChamberDurability();
        int percent = max > 0 ? (durability * 100 / max) : 0;
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_cooling_chamber.status",
                durability, max, percent
        ));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            return true;
        }
        int max = COIConfig.coolingChamberDurability();
        int percent = max > 0 ? (durability * 100 / max) : 0;
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_cooling_chamber.goggle.durability",
                percent
        )));
        if (COIClientOptions.verboseUi()) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_cooling_chamber.goggle.detail",
                    durability, max
            )));
        }
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_cooling_chamber.goggle.hint"
        )));
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt(NBT_VERSION, 1);
        tag.putInt(NBT_DURABILITY, durability);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        durability = tag.getInt(NBT_DURABILITY);
    }
}
