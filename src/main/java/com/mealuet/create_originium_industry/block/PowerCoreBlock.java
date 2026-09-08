package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.core.reactor.ReactorFluidHandler;
import com.mealuet.create_originium_industry.index.COIBlockEntityTypes;
import com.mealuet.create_originium_industry.index.COIItems;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Originium power core. Create kinetic generator with adjacent
 * {@code reactor_housing}, cooling-chamber attachments, and bucket/item
 * interaction — no management GUI.
 */
public class PowerCoreBlock extends KineticBlock implements IBE<PowerCoreBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PowerCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        PowerCoreBlockEntity be = getBlockEntity(level, pos);
        return be == null ? 0 : be.comparatorSignal();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        PowerCoreBlockEntity core = getBlockEntity(level, pos);
        if (core == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(COIItems.PUREST_ORIGINIUM.get())) {
            if (core.insertFuel(stack, player.getAbilities().instabuild)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    core.sendStatusMessage(serverPlayer);
                }
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }
        ReactorFluidHandler fluids = core.fluidHandler();
        if (FluidUtil.interactWithFluidHandler(player, hand, fluids)) {
            core.onFluidsChanged();
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        withBlockEntityDo(level, pos, be -> {
            if (player instanceof ServerPlayer serverPlayer) {
                if (player.isShiftKeyDown()) {
                    ItemStack fuel = be.extractFuel();
                    if (!fuel.isEmpty()) {
                        if (!serverPlayer.addItem(fuel)) {
                            serverPlayer.drop(fuel, false);
                        }
                    }
                }
                be.sendStatusMessage(serverPlayer);
            }
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<PowerCoreBlockEntity> getBlockEntityClass() {
        return PowerCoreBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PowerCoreBlockEntity> getBlockEntityType() {
        return COIBlockEntityTypes.POWER_CORE.get();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            withBlockEntityDo(level, pos, PowerCoreBlockEntity::markStructureDirty);
        }
    }
}
