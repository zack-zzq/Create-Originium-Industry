package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.ByproductBuffer;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.IDustPurifier;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Block entity for the Originium Dust Filter.
 * <p>
 * When powered by rotation and loaded with a sieve, absorbs originium dust
 * from the chunk and can capture a neighbouring machine's emission
 * ({@link IDustPurifier}). Sieve has limited durability and is consumed after
 * N cycles. Captured dust may convert into {@code originium_dust} byproduct.
 * <p>
 * Higher rotational speed = faster absorption (speed multiplier up to 4x).
 */
public class DustFilterBlockEntity extends KineticBlockEntity implements IDustPurifier {

    private static final String NBT_HAS_SIEVE = "HasSieve";
    private static final String NBT_SIEVE_DURABILITY = "SieveDurability";

    private boolean hasSieve = false;
    private int sieveDurability = 0;
    private final ByproductBuffer byproduct = new ByproductBuffer();

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

        int absorption = Math.max(1, (int) (COIConfig.FILTER_ABSORPTION_RATE.get() * speedMultiplier(speed)));
        absorbAmbient(serverLevel, worldPosition, absorption);
    }

    // ==================== IDustPurifier ====================

    @Override
    public boolean isPurifierActive() {
        return hasSieve && sieveDurability > 0 && Math.abs(getSpeed()) > 0;
    }

    @Override
    public double emissionCaptureFactor() {
        return isPurifierActive() ? COIConfig.FILTER_EMISSION_CAPTURE.get() : 0.0;
    }

    @Override
    public int acceptCapturedDust(int captured) {
        if (captured <= 0) {
            return 0;
        }
        consumeSieveCycle();
        int items = byproduct.add(captured, COIConfig.FILTER_BYPRODUCT_DUST_PER_ITEM.get());
        dropByproduct(items);
        setChanged();
        return items;
    }

    @Override
    public int absorbAmbient(ServerLevel level, BlockPos purifierPos, int requested) {
        if (level == null || !isPurifierActive() || requested <= 0) {
            return 0;
        }
        ChunkPos chunkPos = WorldSpace.toDustChunk(level, purifierPos);
        int currentDust = OriginiumDustManager.getDust(level, chunkPos);
        int actual = Math.min(requested, currentDust);
        if (actual <= 0) {
            return 0;
        }
        OriginiumDustManager.addDust(level, chunkPos, -actual, DustReason.FILTER);
        acceptCapturedDust(actual);
        return actual;
    }

    private void consumeSieveCycle() {
        if (!hasSieve || sieveDurability <= 0) {
            return;
        }
        sieveDurability--;
        if (sieveDurability <= 0) {
            hasSieve = false;
            sieveDurability = 0;
        }
    }

    private void dropByproduct(int items) {
        if (items <= 0 || level == null || level.isClientSide) {
            return;
        }
        int remaining = items;
        int max = COIItems.ORIGINIUM_DUST.get().getDefaultMaxStackSize();
        while (remaining > 0) {
            int n = Math.min(max, remaining);
            Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5,
                    new ItemStack(COIItems.ORIGINIUM_DUST.get(), n));
            remaining -= n;
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
            double speedMultiplier = speedMultiplier(speed);
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

        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            return true;
        }

        if (hasSieve) {
            int maxDurability = COIConfig.FILTER_SIEVE_DURABILITY.get();
            int percent = maxDurability > 0 ? (sieveDurability * 100 / maxDurability) : 0;
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.sieve",
                    percent
            )));
            if (COIClientOptions.verboseUi()) {
                tooltip.add(Component.literal("    ").append(Component.translatable(
                        "block.create_originium_industry.originium_dust_filter.goggle.sieve_detail",
                        sieveDurability, maxDurability
                )));
            }
        } else {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.no_sieve"
            )));
        }

        float speed = Math.abs(getSpeed());
        if (speed > 0) {
            double speedMultiplier = speedMultiplier(speed);
            int effectiveRate = Math.max(1, (int) (COIConfig.FILTER_ABSORPTION_RATE.get() * speedMultiplier));
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_dust_filter.goggle.rate",
                    effectiveRate
            )));
        }

        return true;
    }

    /**
     * Absorption speed multiplier from kinetic RPM. Floor 1x so any rotation
     * meets the configured base rate; cap at {@link COIConfig#FILTER_MAX_SPEED_MULTIPLIER}.
     */
    public static double speedMultiplier(float speed) {
        double reference = Math.max(1.0, COIConfig.FILTER_SPEED_REFERENCE.get());
        double max = COIConfig.FILTER_MAX_SPEED_MULTIPLIER.get();
        return Math.max(1.0, Math.min(max, Math.abs(speed) / reference));
    }

    // ==================== Serialization ====================

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean(NBT_HAS_SIEVE, hasSieve);
        compound.putInt(NBT_SIEVE_DURABILITY, sieveDurability);
        byproduct.save(compound);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        hasSieve = compound.getBoolean(NBT_HAS_SIEVE);
        sieveDurability = compound.getInt(NBT_SIEVE_DURABILITY);
        byproduct.load(compound);
    }
}
