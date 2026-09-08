package com.mealuet.create_originium_industry.core.purest;

/**
 * Shared contract for originium accidents (M2 superheat miss, M3 reactor).
 * Meltdown / accident dumps {@code purest_molten_originium} and chunk dust
 * back into the factory loop. It never explodes blocks or spawns TNT.
 */
public final class MeltdownPolicy {

    private MeltdownPolicy() {}

    public static boolean explodesBlocks() {
        return false;
    }

    public static boolean spawnsTnt() {
        return false;
    }
}
