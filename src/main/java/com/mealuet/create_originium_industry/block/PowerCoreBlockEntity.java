package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.advancement.COIAdvancements;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.purest.MeltdownPolicy;
import com.mealuet.create_originium_industry.core.reactor.ReactorFluidHandler;
import com.mealuet.create_originium_industry.core.reactor.ReactorFluids;
import com.mealuet.create_originium_industry.core.reactor.StabilityMath;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

/**
 * Power-core simulation. Fuel is {@code purest_originium} (right-click).
 * Adjacent {@code reactor_housing} is required to run. Cooling chambers
 * attached to faces contribute {@code M}. Fluids convert along
 * coolant ↔ water ↔ hot water from {@code S}. Meltdown dumps dust.
 */
public class PowerCoreBlockEntity extends GeneratingKineticBlockEntity {

    private static final String NBT_VERSION = "version";
    private static final String NBT_FUEL = "FuelCount";
    private static final String NBT_FUEL_TICKS = "FuelTicks";
    private static final String NBT_INSTABILITY = "Instability";
    private static final String NBT_SHUTDOWN = "Shutdown";
    private static final String NBT_HEAT = "Heat";
    private static final String NBT_CAPACITY = "Capacity";
    private static final String NBT_COOLING = "Cooling";
    private static final String NBT_STABILITY = "Stability";
    private static final String NBT_COOLANT = "Coolant";
    private static final String NBT_WATER = "Water";
    private static final String NBT_HOT_WATER = "HotWater";

    private final FluidTank coolant = new TypedTank(() -> COIFluids.ORIGINIUM_COOLANT.getSource());
    private final FluidTank water = new TypedTank(() -> Fluids.WATER);
    private final FluidTank hotWater = new TypedTank(() -> COIFluids.HOT_WATER.getSource());
    private final ReactorFluidHandler fluids = new ReactorFluidHandler(coolant, water, hotWater);

    private int fuelCount;
    private int fuelTicks;
    private double instability;
    private boolean shutdown;
    private double heat;
    private double capacity;
    private double cooling;
    private double stability;
    private boolean lastGenerating;
    private boolean structureDirty = true;

    public PowerCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public ReactorFluidHandler fluidHandler() {
        return fluids;
    }

    public FluidTank coolantTank() {
        return coolant;
    }

    public FluidTank waterTank() {
        return water;
    }

    public FluidTank hotWaterTank() {
        return hotWater;
    }

    public int fuelCount() {
        return fuelCount;
    }

    public double instability() {
        return instability;
    }

    public boolean shutdown() {
        return shutdown;
    }

    public StabilityMath.Snapshot snapshot() {
        return new StabilityMath.Snapshot(heat, capacity, cooling, stability);
    }

    public void markStructureDirty() {
        structureDirty = true;
    }

    public void onFluidsChanged() {
        setChanged();
        notifyUpdate();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public boolean insertFuel(ItemStack stack, boolean creative) {
        if (stack == null || stack.isEmpty() || !stack.is(COIItems.PUREST_ORIGINIUM.get())) {
            return false;
        }
        if (fuelCount >= 64) {
            return false;
        }
        fuelCount++;
        if (shutdown) {
            shutdown = false;
            instability = 0.0;
        }
        if (!creative) {
            stack.shrink(1);
        }
        setChanged();
        notifyUpdate();
        refreshRotation();
        return true;
    }

    public ItemStack extractFuel() {
        if (fuelCount <= 0) {
            return ItemStack.EMPTY;
        }
        fuelCount--;
        fuelTicks = 0;
        setChanged();
        notifyUpdate();
        refreshRotation();
        return new ItemStack(COIItems.PUREST_ORIGINIUM.get());
    }

    /**
     * Debug / GameTest: reset instability and clear shutdown without consuming fuel.
     */
    public void forceStabilize() {
        shutdown = false;
        instability = 0.0;
        setChanged();
        notifyUpdate();
        refreshRotation();
    }

    /**
     * GameTest helper: load fuel, fluids, and optional pre-set instability.
     */
    public void configureForGameTest(int fuel, int coolantMb, int waterMb, int hotMb, double startingInstability) {
        this.fuelCount = Math.max(0, fuel);
        this.fuelTicks = 0;
        this.shutdown = false;
        this.instability = Math.max(0.0, startingInstability);
        int cap = COIConfig.reactorTankCapacity();
        coolant.setFluid(coolantMb <= 0 ? FluidStack.EMPTY : fluidOf(COIFluids.ORIGINIUM_COOLANT.getSource(), Math.min(cap, coolantMb)));
        water.setFluid(waterMb <= 0 ? FluidStack.EMPTY : fluidOf(Fluids.WATER, Math.min(cap, waterMb)));
        hotWater.setFluid(hotMb <= 0 ? FluidStack.EMPTY : fluidOf(COIFluids.HOT_WATER.getSource(), Math.min(cap, hotMb)));
        structureDirty = true;
        setChanged();
    }

    public boolean hasHousing() {
        if (level == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(worldPosition.relative(direction)).is(COITags.Blocks.REACTOR_HOUSING)) {
                return true;
            }
        }
        return false;
    }

    public double attachedChamberCooling() {
        if (level == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(direction);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof CoolingChamberBlockEntity chamber && chamber.isChamberActive()) {
                Direction support = chamber.getBlockState().getValue(CoolingChamberBlock.FACING).getOpposite();
                if (neighbor.relative(support).equals(worldPosition)) {
                    total += chamber.coolingFactor();
                }
            }
        }
        return total;
    }

    public boolean isRunning() {
        return !shutdown && fuelCount > 0 && hasHousing();
    }

    public boolean isGenerating() {
        return isRunning();
    }

    @Override
    public float getGeneratedSpeed() {
        return isGenerating() ? COIConfig.reactorGeneratedRpm() : 0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        tickReactor(serverLevel);
    }

    /**
     * Server simulation step. GameTests may call this directly.
     */
    public void tickReactor(ServerLevel serverLevel) {
        boolean running = isRunning();
        double chamber = attachedChamberCooling();
        StabilityMath.Snapshot snap = StabilityMath.compute(
                running,
                true,
                coolant.getFluidAmount(),
                water.getFluidAmount(),
                hotWater.getFluidAmount(),
                chamber
        );
        heat = snap.heat();
        capacity = snap.capacity();
        cooling = snap.cooling();
        stability = snap.stability();

        if (running) {
            ReactorFluids.Amounts current = new ReactorFluids.Amounts(
                    coolant.getFluidAmount(),
                    water.getFluidAmount(),
                    hotWater.getFluidAmount()
            );
            ReactorFluids.Amounts next = ReactorFluids.convert(
                    current,
                    stability,
                    COIConfig.reactorCoolantMinimumFlow(),
                    COIConfig.reactorTankCapacity()
            );
            if (!current.equals(next)) {
                applyAmounts(next);
            }
            consumeFuelTick();
            instability = StabilityMath.nextInstability(instability, stability);
            leakUnstableDust(serverLevel);
            if (COIConfig.reactorMeltdownEnabled()
                    && instability >= COIConfig.reactorMeltdownThreshold()) {
                triggerMeltdown(serverLevel);
            }
        }

        boolean generating = isGenerating();
        if (generating && !lastGenerating) {
            COIAdvancements.powerCoreStarted(serverLevel, worldPosition);
        }
        if (generating != lastGenerating || structureDirty) {
            lastGenerating = generating;
            structureDirty = false;
            refreshRotation();
        }
        setChanged();
        if (serverLevel.getGameTime() % 10 == 0) {
            notifyUpdate();
            serverLevel.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private void consumeFuelTick() {
        fuelTicks++;
        int period = Math.max(1, COIConfig.reactorFuelTicksPerItem());
        if (fuelTicks >= period) {
            fuelTicks = 0;
            fuelCount = Math.max(0, fuelCount - 1);
        }
    }

    private void leakUnstableDust(ServerLevel serverLevel) {
        if (StabilityMath.sign(stability) != StabilityMath.Sign.NEGATIVE) {
            return;
        }
        if (serverLevel.getGameTime() % 20 != 0) {
            return;
        }
        int leak = COIConfig.reactorUnstableDustPerSecond();
        if (leak > 0) {
            OriginiumDustManager.addDustAt(serverLevel, worldPosition, leak, DustReason.REACTOR);
        }
    }

    public void triggerMeltdown(ServerLevel serverLevel) {
        if (MeltdownPolicy.explodesBlocks() || MeltdownPolicy.spawnsTnt()) {
            return;
        }
        MeltdownPolicy.dumpDust(serverLevel, worldPosition);
        fuelCount = 0;
        fuelTicks = 0;
        shutdown = true;
        instability = 0.0;
        applyAmounts(ReactorFluids.dumpHeat(
                new ReactorFluids.Amounts(coolant.getFluidAmount(), water.getFluidAmount(), hotWater.getFluidAmount()),
                COIConfig.reactorTankCapacity()
        ));
        refreshRotation();
        setChanged();
        notifyUpdate();
    }

    private void applyAmounts(ReactorFluids.Amounts amounts) {
        int cap = COIConfig.reactorTankCapacity();
        coolant.setCapacity(cap);
        water.setCapacity(cap);
        hotWater.setCapacity(cap);
        coolant.setFluid(amounts.coolant() <= 0
                ? FluidStack.EMPTY
                : fluidOf(COIFluids.ORIGINIUM_COOLANT.getSource(), amounts.coolant()));
        water.setFluid(amounts.water() <= 0
                ? FluidStack.EMPTY
                : fluidOf(Fluids.WATER, amounts.water()));
        hotWater.setFluid(amounts.hotWater() <= 0
                ? FluidStack.EMPTY
                : fluidOf(COIFluids.HOT_WATER.getSource(), amounts.hotWater()));
    }

    private static FluidStack fluidOf(net.minecraft.world.level.material.Fluid fluid, int amount) {
        return new FluidStack(fluid, amount);
    }

    private void refreshRotation() {
        if (level != null && !level.isClientSide) {
            updateGeneratedRotation();
        }
    }

    public int comparatorSignal() {
        if (shutdown) {
            return 0;
        }
        double h = Math.max(1.0, heat);
        return Mth.clamp((int) Math.round((stability / h + 1.0) * 7.5), 0, 15);
    }

    public void sendStatusMessage(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_power_core.status",
                format(stability),
                format(heat),
                format(capacity),
                format(cooling)
        ));
        player.sendSystemMessage(Component.translatable(
                "block.create_originium_industry.originium_power_core.status.detail",
                fuelCount,
                format(instability),
                shutdown ? Component.translatable("block.create_originium_industry.originium_power_core.shutdown")
                        : Component.translatable(signLangKey())
        ));
    }

    private String signLangKey() {
        return switch (StabilityMath.sign(stability)) {
            case POSITIVE -> "block.create_originium_industry.originium_power_core.sign.positive";
            case ZERO -> "block.create_originium_industry.originium_power_core.sign.zero";
            case NEGATIVE -> "block.create_originium_industry.originium_power_core.sign.negative";
        };
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.MINIMAL) {
            return true;
        }
        tooltip.add(Component.literal("    ").append(Component.translatable(
                "block.create_originium_industry.originium_power_core.goggle.s",
                format(stability)
        )));
        if (COIClientOptions.verboseUi()) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_power_core.goggle.hcm",
                    format(heat), format(capacity), format(cooling)
            )));
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_power_core.goggle.fuel",
                    fuelCount, format(instability)
            )));
        }
        tooltip.add(Component.literal("    ").append(Component.translatable(signLangKey())));
        if (shutdown) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_power_core.shutdown"
            )));
        }
        if (instability >= COIConfig.reactorInstabilityWarning()) {
            tooltip.add(Component.literal("    ").append(Component.translatable(
                    "block.create_originium_industry.originium_power_core.goggle.warn"
            )));
        }
        return true;
    }

    private static String format(double value) {
        return String.format("%.1f", value);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt(NBT_VERSION, 1);
        tag.putInt(NBT_FUEL, fuelCount);
        tag.putInt(NBT_FUEL_TICKS, fuelTicks);
        tag.putDouble(NBT_INSTABILITY, instability);
        tag.putBoolean(NBT_SHUTDOWN, shutdown);
        tag.putDouble(NBT_HEAT, heat);
        tag.putDouble(NBT_CAPACITY, capacity);
        tag.putDouble(NBT_COOLING, cooling);
        tag.putDouble(NBT_STABILITY, stability);
        tag.put(NBT_COOLANT, coolant.writeToNBT(registries, new CompoundTag()));
        tag.put(NBT_WATER, water.writeToNBT(registries, new CompoundTag()));
        tag.put(NBT_HOT_WATER, hotWater.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        fuelCount = tag.getInt(NBT_FUEL);
        fuelTicks = tag.getInt(NBT_FUEL_TICKS);
        instability = tag.getDouble(NBT_INSTABILITY);
        shutdown = tag.getBoolean(NBT_SHUTDOWN);
        heat = tag.getDouble(NBT_HEAT);
        capacity = tag.getDouble(NBT_CAPACITY);
        cooling = tag.getDouble(NBT_COOLING);
        stability = tag.getDouble(NBT_STABILITY);
        if (tag.contains(NBT_COOLANT)) {
            coolant.readFromNBT(registries, tag.getCompound(NBT_COOLANT));
        }
        if (tag.contains(NBT_WATER)) {
            water.readFromNBT(registries, tag.getCompound(NBT_WATER));
        }
        if (tag.contains(NBT_HOT_WATER)) {
            hotWater.readFromNBT(registries, tag.getCompound(NBT_HOT_WATER));
        }
        int cap = COIConfig.reactorTankCapacity();
        coolant.setCapacity(cap);
        water.setCapacity(cap);
        hotWater.setCapacity(cap);
    }

    private final class TypedTank extends FluidTank {
        private final java.util.function.Supplier<net.minecraft.world.level.material.Fluid> type;

        private TypedTank(java.util.function.Supplier<net.minecraft.world.level.material.Fluid> type) {
            super(COIConfig.reactorTankCapacity());
            this.type = type;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack != null && !stack.isEmpty() && stack.getFluid().isSame(type.get());
        }

        @Override
        protected void onContentsChanged() {
            PowerCoreBlockEntity.this.onFluidsChanged();
        }
    }
}
