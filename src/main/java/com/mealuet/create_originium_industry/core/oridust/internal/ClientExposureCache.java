package com.mealuet.create_originium_industry.core.oridust.internal;

/**
 * Local player's last received exposure / infection. Not other players'.
 */
public final class ClientExposureCache {

    private static int exposure;
    private static int infection;
    private static boolean received;

    private ClientExposureCache() {}

    public static void apply(int newExposure, int newInfection) {
        exposure = Math.max(0, newExposure);
        infection = Math.max(0, newInfection);
        received = true;
    }

    public static int exposure() {
        return exposure;
    }

    public static int infection() {
        return infection;
    }

    public static boolean hasReceived() {
        return received;
    }

    public static void clear() {
        exposure = 0;
        infection = 0;
        received = false;
    }
}
