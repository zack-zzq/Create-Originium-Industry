package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.index.COIBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Originium Dust Filter block.
 * A Create kinetic machine that absorbs originium dust from the chunk
 * when powered by rotation and loaded with an originium_dust_sieve.
 * <p>
 * Right-click with sieve to insert; right-click empty-hand to view status.
 */
public class DustFilterBlock extends KineticBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DustFilterBlock(Properties properties) {
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
    public BlockEntityType<? extends DustFilterBlockEntity> getBlockEntityType() {
        return COIBlockEntityTypes.DUST_FILTER.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DustFilterBlockEntity filter)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Insert sieve
        if (!stack.isEmpty() && stack.getItem() == com.mealuet.create_originium_industry.index.COIItems.ORIGINIUM_DUST_SIEVE.get()) {
            if (!filter.hasSieve()) {
                filter.insertSieve(stack.copyWithCount(1));
                if (!player.isCreative()) stack.shrink(1);
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.translatable(
                            "block.create_originium_industry.originium_dust_filter.sieve_inserted"));
                }
                return ItemInteractionResult.SUCCESS;
            } else {
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.translatable(
                            "block.create_originium_industry.originium_dust_filter.already_has_sieve"));
                }
                return ItemInteractionResult.CONSUME;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DustFilterBlockEntity filter)) return InteractionResult.PASS;

        if (player instanceof ServerPlayer sp) {
            if (player.isShiftKeyDown() && filter.hasSieve()) {
                // Shift+right-click: eject sieve
                ItemStack sieve = filter.removeSieve();
                if (!sp.addItem(sieve)) {
                    sp.drop(sieve, false);
                }
                sp.sendSystemMessage(Component.translatable(
                        "block.create_originium_industry.originium_dust_filter.sieve_removed"));
            } else {
                // Right-click: show status
                filter.sendStatusMessage(sp);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
