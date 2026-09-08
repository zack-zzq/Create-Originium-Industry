package com.mealuet.create_originium_industry.network;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.core.oridust.ClientExposureCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Local-player exposure and infection. Never broadcast to other players.
 */
public record ExposureSyncPayload(int exposure, int infection) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ExposureSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, "exposure_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExposureSyncPayload> STREAM_CODEC =
            StreamCodec.of(ExposureSyncPayload::write, ExposureSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, ExposureSyncPayload payload) {
        buf.writeVarInt(payload.exposure);
        buf.writeVarInt(payload.infection);
    }

    private static ExposureSyncPayload read(RegistryFriendlyByteBuf buf) {
        return new ExposureSyncPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExposureSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientExposureCache.apply(payload.exposure, payload.infection));
    }
}
