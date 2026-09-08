package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.index.COIBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Originium Dust Nozzle. Attaches to an Encased Fan (any {@link IAirCurrentSource})
 * the same way Create's own nozzle does. Spinning airflow redirects chunk dust
 * into the neighbouring chunk along the flow — dilution/redirection, not a void.
 */
public class DustNozzleBlock extends WrenchableDirectionalBlock implements IBE<DustNozzleBlockEntity> {

    public DustNozzleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        BlockState state = defaultBlockState().setValue(FACING, clicked);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction supportDir = state.getValue(FACING).getOpposite();
        BlockEntity be = level.getBlockEntity(pos.relative(supportDir));
        if (be instanceof IAirCurrentSource source) {
            return source.getAirflowOriginSide() == state.getValue(FACING);
        }
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        if (fromPos.equals(pos.relative(state.getValue(FACING).getOpposite())) && !state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.NOZZLE.get(state.getValue(FACING));
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
    public Class<DustNozzleBlockEntity> getBlockEntityClass() {
        return DustNozzleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DustNozzleBlockEntity> getBlockEntityType() {
        return COIBlockEntityTypes.DUST_NOZZLE.get();
    }
}
