package com.mealuet.create_originium_industry.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Play-to-client payloads for nearby dust and local-player exposure.
 */
public final class COINetwork {

    private COINetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(DustSyncPayload.TYPE, DustSyncPayload.STREAM_CODEC, DustSyncPayload::handle);
        registrar.playToClient(ExposureSyncPayload.TYPE, ExposureSyncPayload.STREAM_CODEC, ExposureSyncPayload::handle);
    }
}
