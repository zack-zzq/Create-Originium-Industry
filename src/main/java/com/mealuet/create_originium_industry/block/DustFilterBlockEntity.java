package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Block entity for the Originium Dust Filter.
 * <p>
 * When powered by rotation and loaded with a sieve, absorbs originium dust
 * from the chunk. Sieve has limited durability and is consumed after N cycles.
 * <p>
 * Higher rotational speed = faster absorption (speed multiplier up to 4x).
 */
public class DustFilterBlockEntity extends KineticBlockEntity {

    private static final String NBT_HAS_SIEVE = "HasSieve";
    private static final String NBT_SIEVE_DURABILITY = "SieveDurability";

    private boolean hasSieve = false;
    private int sieveDurability = 0;

    public DustFilterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!hasSieve || sieveDurability <= 0) return;

        float speed = Math.abs(getSpeed());
        if (speed == 0) return;

        int interval = COIConfig.FILTER_ABSORPTION_INTERVAL.get();
        if (level.getGameTime() % interval != 0) return;

        // Speed multiplier: speed/64, clamped to [1, 4]
        double speedMultiplier = Math.max(1.0, Math.min(4.0, speed / 64.0));
        int baseRate = COIConfig.FILTER_ABSORPTION_RATE.get();
        int absorption = Math.max(1, (int) (baseRate * speedMultiplier));

        ChunkPos chunkPos = new ChunkPos(worldPosition);
        int currentDust = OriginiumDustManager.getDust(chunkPos);

        if (currentDust > 0) {
            int actualAbsorption = Math.min(absorption, currentDust);
            OriginiumDustManager.addDust(serverLevel, chunkPos, -actualAbsorption, DustReason.FILTER);

            // Consume sieve durability
            sieveDurability--;
            if (sieveDurability <= 0) {
                hasSieve = false;
                sieveDurability = 0;
            }
            setChanged();
        }
    }

    // ==================== Sieve Management ====================

    public boolean hasSieve() {
        return hasSieve;
    }

    public void insertSieve(ItemStack sieveStack) {
        if (sieveStack.getItem() == COIItems.ORIGINIUM_DUST_SIEVE.get()) {
            this.hasSieve = true;
            this.sieveDurability = COIConfig.FILTER_SIEVE_DURABILITY.get();
            setChanged();
        }
    }

    public ItemStack removeSieve() {
        if (hasSieve) {
            hasSieve = false;
            int remaining = sieveDurability;
            sieveDurability = 0;
            setChanged();
            // Return a sieve item (regardless of remaining durability for simplicity)
            return new ItemStack(COIItems.ORIGINIUM_DUST_SIEVE.get(), 1);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sends a status message to the player about the filter's current state.
     */
    public void sendStatusMessage(ServerPlayer player) {
        if (hasSieve) {
            int maxDurability = COIConfig.FILTER_SIEVE_DURABILITY.get();
            int percent = maxDurability > 0 ? (sieveDurability * 100 / maxDurability) : 0;
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.status",
                    sieveDurability, maxDurability, percent
            ));
        } else {
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.no_sieve"));
        }

        float speed = Math.abs(getSpeed());
        if (speed > 0) {
            double speedMultiplier = Math.max(1.0, Math.min(4.0, speed / 64.0));
            int effectiveRate = Math.max(1, (int) (COIConfig.FILTER_ABSORPTION_RATE.get() * speedMultiplier));
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.speed",
                    String.format("%.0f", speed), String.format("%.1fx", speedMultiplier), effectiveRate
            ));
        } else {
            player.sendSystemMessage(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.no_power"));
        }
    }

    // ==================== Create Goggle Info ====================

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        if (hasSieve) {
            int maxDurability = COIConfig.FILTER_SIEVE_DURABILITY.get();
            int percent = maxDurability > 0 ? (sieveDurability * 100 / maxDurability) : 0;
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.sieve",
                    percent
            )));
        } else {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.no_sieve"
            )));
        }

        float speed = Math.abs(getSpeed());
        if (speed > 0) {
            double speedMultiplier = Math.max(1.0, Math.min(4.0, speed / 64.0));
            int effectiveRate = Math.max(1, (int) (COIConfig.FILTER_ABSORPTION_RATE.get() * speedMultiplier));
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.rate",
                    effectiveRate
            )));
        }

        return true;
    }

    // ==================== Serialization ====================

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean(NBT_HAS_SIEVE, hasSieve);
        compound.putInt(NBT_SIEVE_DURABILITY, sieveDurability);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        hasSieve = compound.getBoolean(NBT_HAS_SIEVE);
        sieveDurability = compound.getInt(NBT_SIEVE_DURABILITY);
    }
}
