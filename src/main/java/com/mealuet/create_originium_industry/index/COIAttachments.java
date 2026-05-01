package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.core.oridust.OriDustData;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class COIAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CreateOriginiumIndustry.MODID);

    public static final Supplier<AttachmentType<OriDustData>> CHUNK_DUST_TYPE = ATTACHMENT_TYPES.register(
            "chunk_oridust_data",
            () -> AttachmentType.serializable(OriDustData::new).build()
    );

    public static final Supplier<AttachmentType<PlayerExposureData>> PLAYER_EXPOSURE_TYPE = ATTACHMENT_TYPES.register(
            "player_exposure_data",
            () -> AttachmentType.serializable(PlayerExposureData::new).copyOnDeath().build()
    );

    public static Optional<OriDustData> getChunkDustData(IAttachmentHolder holder) {
        if (holder == null) return Optional.empty();
        return Optional.of(holder.getExistingData(CHUNK_DUST_TYPE).orElseGet(() -> holder.getData(CHUNK_DUST_TYPE)));
    }

    public static PlayerExposureData getPlayerExposure(IAttachmentHolder holder) {
        return holder.getData(PLAYER_EXPOSURE_TYPE);
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
