package com.mealuet.create_originium_industry.core.perf;

import java.util.Locale;

/**
 * Lightweight server-thread timing for dust + reactor hot paths.
 * <p>
 * Always on: {@link System#nanoTime()} around the existing tick work is
 * cheap next to SavedData / block-entity scans. GameTests and
 * {@code /coi_debug perf} read the last sample; this is not a substitute
 * for Spark or F3+L in a real factory.
 */
public final class PerfProbe {

    /** Design-draft budget: ~100 related machines extra &lt; 2 ms/tick. */
    public static final int STATED_MACHINE_COUNT = 100;
    public static final long TARGET_NANOS = 2_000_000L;
    /**
     * CI / GameTest ceiling. Catches 50 ms disasters without flaking on
     * shared runners that miss the 2 ms design target.
     */
    public static final long CI_GUARD_NANOS = 50_000_000L;

    private static int lastDustActiveChunks;
    private static long lastDustCycleNanos;
    private static long lastSyncNanos;

    private static long reactorGameTime = Long.MIN_VALUE;
    private static long reactorTickNanos;
    private static int reactorTickCount;

    private static long filterGameTime = Long.MIN_VALUE;
    private static long filterTickNanos;
    private static int filterTickCount;

    private PerfProbe() {}

    public static void recordDustCycle(int activeChunks, long nanos) {
        lastDustActiveChunks = Math.max(0, activeChunks);
        lastDustCycleNanos = Math.max(0L, nanos);
    }

    public static void recordSync(long nanos) {
        lastSyncNanos = Math.max(0L, nanos);
    }

    public static void addReactor(long gameTime, long nanos) {
        if (gameTime != reactorGameTime) {
            reactorGameTime = gameTime;
            reactorTickNanos = 0L;
            reactorTickCount = 0;
        }
        reactorTickNanos += Math.max(0L, nanos);
        reactorTickCount++;
    }

    public static void addFilter(long gameTime, long nanos) {
        if (gameTime != filterGameTime) {
            filterGameTime = gameTime;
            filterTickNanos = 0L;
            filterTickCount = 0;
        }
        filterTickNanos += Math.max(0L, nanos);
        filterTickCount++;
    }

    public static int lastDustActiveChunks() {
        return lastDustActiveChunks;
    }

    public static long lastDustCycleNanos() {
        return lastDustCycleNanos;
    }

    public static long lastSyncNanos() {
        return lastSyncNanos;
    }

    public static long lastReactorTickNanos() {
        return reactorTickNanos;
    }

    public static int lastReactorTickCount() {
        return reactorTickCount;
    }

    public static long lastFilterTickNanos() {
        return filterTickNanos;
    }

    public static int lastFilterTickCount() {
        return filterTickCount;
    }

    /**
     * Worst-case extra if dust, filters, and client sync all fire on the
     * same tick as every running reactor (aligned intervals).
     */
    public static long lastAlignedExtraNanos() {
        return lastDustCycleNanos + reactorTickNanos + filterTickNanos + lastSyncNanos;
    }

    public static String formatNanos(long nanos) {
        if (nanos < 1_000L) {
            return nanos + " ns";
        }
        if (nanos < 1_000_000L) {
            return String.format(Locale.ROOT, "%.1f µs", nanos / 1_000.0);
        }
        return String.format(Locale.ROOT, "%.3f ms", nanos / 1_000_000.0);
    }

    public static String formatMillis(long nanos) {
        return String.format(Locale.ROOT, "%.3f", nanos / 1_000_000.0);
    }

    public static void resetForTests() {
        lastDustActiveChunks = 0;
        lastDustCycleNanos = 0L;
        lastSyncNanos = 0L;
        reactorGameTime = Long.MIN_VALUE;
        reactorTickNanos = 0L;
        reactorTickCount = 0;
        filterGameTime = Long.MIN_VALUE;
        filterTickNanos = 0L;
        filterTickCount = 0;
    }
}
