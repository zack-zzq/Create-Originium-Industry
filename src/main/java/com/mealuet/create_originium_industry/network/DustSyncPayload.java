package com.mealuet.create_originium_industry.network;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.core.oridust.ClientDustCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Nearby chunk dust patch. Keys are logical {@code ChunkPos.toLong()} values.
 */
public record DustSyncPayload(long[] chunkKeys, int[] dustValues) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DustSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, "dust_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DustSyncPayload> STREAM_CODEC =
            StreamCodec.of(DustSyncPayload::write, DustSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, DustSyncPayload payload) {
        int n = Math.min(payload.chunkKeys.length, payload.dustValues.length);
        buf.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            buf.writeLong(payload.chunkKeys[i]);
            buf.writeVarInt(payload.dustValues[i]);
        }
    }

    private static DustSyncPayload read(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        long[] keys = new long[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = buf.readLong();
            values[i] = buf.readVarInt();
        }
        return new DustSyncPayload(keys, values);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DustSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientDustCache.apply(payload.chunkKeys, payload.dustValues));
    }
}
