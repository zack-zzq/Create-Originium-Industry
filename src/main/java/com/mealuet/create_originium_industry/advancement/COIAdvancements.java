package com.mealuet.create_originium_industry.advancement;

import com.mealuet.create_originium_industry.index.COICriteria;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/**
 * Grants backbone advancements from gameplay hooks. Datapack JSON stays the
 * source of titles/descriptions; this only fires registered criteria.
 */
public final class COIAdvancements {

    public static final int NEARBY_RANGE = 16;

    private COIAdvancements() {}

    public static void dustExposure(ServerPlayer player) {
        if (player != null) {
            COICriteria.DUST_EXPOSURE.get().trigger(player);
        }
    }

    public static void dustPurified(ServerLevel level, BlockPos pos) {
        awardNearby(level, pos, COICriteria.DUST_PURIFIED.get());
    }

    public static void powerCoreStarted(ServerLevel level, BlockPos pos) {
        awardNearby(level, pos, COICriteria.POWER_CORE_STARTED.get());
    }

    public static void awardNearby(ServerLevel level, BlockPos pos, SimplePlayerTrigger trigger) {
        if (level == null || pos == null || trigger == null) {
            return;
        }
        AABB box = new AABB(pos).inflate(NEARBY_RANGE);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            trigger.trigger(player);
        }
    }
}
