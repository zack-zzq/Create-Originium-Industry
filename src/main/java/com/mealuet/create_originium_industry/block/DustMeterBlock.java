package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.index.COIBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Originium dust meter. Comparator uses the server BE snapshot; goggles
 * share {@link com.mealuet.create_originium_industry.core.oridust.VisibleDust}
 * with HUD and debug. No GUI.
 */
public class DustMeterBlock extends HorizontalDirectionalBlock implements IBE<DustMeterBlockEntity> {

    public static final MapCodec<DustMeterBlock> CODEC = simpleCodec(DustMeterBlock::new);

    public DustMeterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        withBlockEntityDo(level, pos, be -> {
            if (player instanceof ServerPlayer serverPlayer) {
                be.sendStatusMessage(serverPlayer);
            }
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        DustMeterBlockEntity be = getBlockEntity(level, pos);
        return be == null ? 0 : Mth.clamp(be.comparatorSignal(), 0, 15);
    }

    @Override
    public Class<DustMeterBlockEntity> getBlockEntityClass() {
        return DustMeterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DustMeterBlockEntity> getBlockEntityType() {
        return COIBlockEntityTypes.DUST_METER.get();
    }
}
