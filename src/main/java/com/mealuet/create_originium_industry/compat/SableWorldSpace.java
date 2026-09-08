package com.mealuet.create_originium_industry.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Isolated compileOnly bridge to Sable Companion.
 * <p>
 * Do not reference this class unless {@code sable} is loaded — the companion
 * types live in Sable's Jar-in-Jar and are not packaged with this mod.
 * <p>
 * Equivalent to the Sable-side call
 * {@code Sable.HELPER.getContaining(level, position).logicalPose()}
 * ({@code Sable.HELPER} is {@code ActiveSableCompanion}, which implements
 * {@link SableCompanion}).
 */
public final class SableWorldSpace {

    private SableWorldSpace() {}

    /**
     * If {@code position} is inside a loaded Sable sublevel plot, returns the
     * corresponding logical (global) world position. Otherwise returns
     * {@code position} unchanged.
     */
    public static Vec3 toLogical(Level level, Vec3 position) {
        SubLevelAccess containing = SableCompanion.INSTANCE.getContaining(level, position);
        if (containing == null) {
            return position;
        }
        return containing.logicalPose().transformPosition(position);
    }
}
