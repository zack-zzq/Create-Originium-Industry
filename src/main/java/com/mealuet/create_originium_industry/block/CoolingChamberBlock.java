package com.mealuet.create_originium_industry.block;

import com.mealuet.create_originium_industry.core.oridust.ProcessAttachments;
import com.mealuet.create_originium_industry.index.COIBlockEntityTypes;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Basin attachment that enables supercooling recipes, and a reactor cooling
 * attachment when placed on an originium power core. No GUI.
 */
public class CoolingChamberBlock extends WrenchableDirectionalBlock implements IBE<CoolingChamberBlockEntity> {

    private static final VoxelShape DOWN = Block.box(1, 14, 1, 15, 16, 15);
    private static final VoxelShape UP = Block.box(1, 0, 1, 15, 2, 15);
    private static final VoxelShape NORTH = Block.box(1, 1, 14, 15, 15, 16);
    private static final VoxelShape SOUTH = Block.box(1, 1, 0, 15, 15, 2);
    private static final VoxelShape WEST = Block.box(14, 1, 1, 16, 15, 15);
    private static final VoxelShape EAST = Block.box(0, 1, 1, 2, 15, 15);

    public CoolingChamberBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        BlockPos support = context.getClickedPos().relative(clicked.getOpposite());
        if (ProcessAttachments.isCoolingSupport(context.getLevel(), support)) {
            return defaultBlockState().setValue(FACING, clicked);
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockPos other = context.getClickedPos().relative(direction.getOpposite());
            if (ProcessAttachments.isCoolingSupport(context.getLevel(), other)) {
                return defaultBlockState().setValue(FACING, direction);
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction support = state.getValue(FACING).getOpposite();
        return ProcessAttachments.isCoolingSupport(level, pos.relative(support));
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
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
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
    public Class<CoolingChamberBlockEntity> getBlockEntityClass() {
        return CoolingChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoolingChamberBlockEntity> getBlockEntityType() {
        return COIBlockEntityTypes.COOLING_CHAMBER.get();
    }
}
