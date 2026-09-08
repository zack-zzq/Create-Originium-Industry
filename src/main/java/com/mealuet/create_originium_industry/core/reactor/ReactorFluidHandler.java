package com.mealuet.create_originium_industry.core.reactor;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Three typed tanks exposed as one {@link IFluidHandler} for pipes and buckets.
 * Tank 0 = coolant, 1 = water, 2 = hot water.
 */
public final class ReactorFluidHandler implements IFluidHandler {

    private final FluidTank coolant;
    private final FluidTank water;
    private final FluidTank hotWater;

    public ReactorFluidHandler(FluidTank coolant, FluidTank water, FluidTank hotWater) {
        this.coolant = coolant;
        this.water = water;
        this.hotWater = hotWater;
    }

    public FluidTank tank(int tank) {
        return switch (tank) {
            case 0 -> coolant;
            case 1 -> water;
            default -> hotWater;
        };
    }

    @Override
    public int getTanks() {
        return 3;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < 3; i++) {
            FluidTank tank = tank(i);
            if (tank.isFluidValid(resource)) {
                return tank.fill(resource, action);
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (int i = 0; i < 3; i++) {
            FluidTank tank = tank(i);
            if (tank.isFluidValid(resource) && !tank.isEmpty()) {
                return tank.drain(resource, action);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        // Prefer dumping heat first so pipes can bleed hot water.
        if (!hotWater.isEmpty()) {
            return hotWater.drain(maxDrain, action);
        }
        if (!water.isEmpty()) {
            return water.drain(maxDrain, action);
        }
        return coolant.drain(maxDrain, action);
    }
}
