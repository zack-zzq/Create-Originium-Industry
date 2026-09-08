package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side originium-dust producer API.
 * <p>
 * Create-machine hooks and future blocks call {@link #submitDust} with an
 * <em>expected</em> delta at a world position. Implementations must remap
 * through {@link com.mealuet.create_originium_industry.compat.WorldSpace}
 * and persist via {@link OriginiumDustManager}. Nearby {@link IDustPurifier}s
 * may reduce the amount actually deposited.
 * <p>
 * The default implementation is {@link DustSubmission#INSTANCE}.
 */
public interface IOridustProducer {

    /**
     * Submit an expected dust delta at a block position.
     *
     * @return the amount actually deposited into the logical chunk after
     *         purification (0 if nothing was written)
     */
    int submitDust(ServerLevel level, BlockPos pos, int expectedAmount, DustReason reason);

    /**
     * Submit an expected dust delta at an arbitrary world position.
     */
    default int submitDust(ServerLevel level, Vec3 pos, int expectedAmount, DustReason reason) {
        if (pos == null) {
            return 0;
        }
        return submitDust(level, BlockPos.containing(pos), expectedAmount, reason);
    }
}
