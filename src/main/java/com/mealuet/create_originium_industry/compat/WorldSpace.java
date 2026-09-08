package com.mealuet.create_originium_industry.compat;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/**
 * Logical-world coordinate facade for originium dust.
 * <p>
 * Dust is keyed by <em>logical</em> overworld {@link ChunkPos}. When Create:
 * Aeronautics / Sable is present, plot-grid (physical) positions inside a
 * sublevel are projected to the sublevel's {@code logicalPose()} so pollution
 * is deposited at the ship's location in the overworld at emit time and does
 * not travel with the ship. Without Sable this is identity.
 * <p>
 * Sable is an optional runtime dependency. {@link SableWorldSpace} is loaded
 * only after {@link ModList#isLoaded(String)} confirms {@code sable}, so the
 * dedicated companion types are never resolved on a classpath that does not
 * include Sable.
 */
public final class WorldSpace {

    public static final String SABLE_MODID = "sable";
    public static final String AERONAUTICS_MODID = "aeronautics";

    private static final boolean SABLE_LOADED = ModList.get().isLoaded(SABLE_MODID);
    private static volatile boolean sableBridgeFailed = false;

    private WorldSpace() {}

    public static boolean isSableLoaded() {
        return SABLE_LOADED;
    }

    /**
     * Maps a block position to the logical dust chunk.
     */
    public static ChunkPos toDustChunk(Level level, BlockPos pos) {
        return toDustChunk(level, Vec3.atCenterOf(pos));
    }

    /**
     * Maps an entity's current position to the logical dust chunk.
     * Prefer this over {@link Entity#chunkPosition()} so Sable can remap
     * plot-grid coordinates.
     */
    public static ChunkPos toDustChunk(Entity entity) {
        return toDustChunk(entity.level(), entity.position());
    }

    /**
     * Maps a world-space position to the logical dust chunk.
     */
    public static ChunkPos toDustChunk(Level level, Vec3 pos) {
        Vec3 logical = toLogical(level, pos);
        return new ChunkPos(BlockPos.containing(logical));
    }

    /**
     * Projects {@code pos} into logical overworld coordinates. Identity when
     * Sable is absent or the position is not inside a sublevel.
     */
    public static Vec3 toLogical(Level level, Vec3 pos) {
        if (!SABLE_LOADED || sableBridgeFailed || level == null || pos == null) {
            return pos;
        }
        try {
            return SableWorldSpace.toLogical(level, pos);
        } catch (Throwable t) {
            sableBridgeFailed = true;
            CreateOriginiumIndustry.LOGGER.warn(
                    "Sable was loaded but the WorldSpace bridge failed; dust will use identity coordinates. {}",
                    t.toString()
            );
            return pos;
        }
    }
}
