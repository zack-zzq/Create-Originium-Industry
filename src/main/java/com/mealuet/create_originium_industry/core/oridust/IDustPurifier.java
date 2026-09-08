package com.mealuet.create_originium_industry.core.oridust;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Device that reduces machine emission and/or absorbs chunk dust, optionally
 * converting captured dust into recoverable {@code originium_dust} byproduct
 * so pollution stays in the main processing loop.
 * <p>
 * Shared by the kinetic filter block and the Basin / process sieve attachment.
 * Callers should go through {@link DustPurification} rather than invoking
 * capture math themselves.
 */
public interface IDustPurifier {

    /**
     * Whether this device currently captures emission or absorbs ambient dust.
     */
    boolean isPurifierActive();

    /**
     * Fraction of a nearby machine's incoming emission this device captures
     * (0 = none, 1 = all). Ignored when {@link #isPurifierActive()} is false.
     */
    default double emissionCaptureFactor() {
        return 0.0;
    }

    /**
     * Record {@code captured} dust units taken from an emission or from the
     * chunk. Implementations may convert units into {@code originium_dust}
     * items (see {@link ByproductBuffer}) and must not duplicate or void
     * already-accounted items.
     *
     * @return count of {@code originium_dust} items produced from this capture
     *         (0 if byproduct is disabled or the buffer has not reached a full item)
     */
    default int acceptCapturedDust(int captured) {
        return 0;
    }

    /**
     * Absorb existing chunk dust at the purifier's logical position.
     *
     * @param requested maximum dust to pull from the chunk this cycle
     * @return dust actually removed from the chunk
     */
    default int absorbAmbient(ServerLevel level, BlockPos purifierPos, int requested) {
        return 0;
    }
}
