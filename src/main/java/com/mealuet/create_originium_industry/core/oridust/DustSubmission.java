package com.mealuet.create_originium_industry.core.oridust;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Default {@link IOridustProducer}: purify nearby, then write through
 * {@link OriginiumDustManager#addDustAt} (WorldSpace remap included).
 */
public final class DustSubmission implements IOridustProducer {

    public static final DustSubmission INSTANCE = new DustSubmission();

    private DustSubmission() {}

    /**
     * Convenience for machine hooks that do not need a custom producer.
     */
    public static int submit(ServerLevel level, BlockPos pos, int expectedAmount, DustReason reason) {
        return INSTANCE.submitDust(level, pos, expectedAmount, reason);
    }

    @Override
    public int submitDust(ServerLevel level, BlockPos pos, int expectedAmount, DustReason reason) {
        if (level == null || pos == null || expectedAmount <= 0) {
            return 0;
        }
        if (reason == DustReason.MACHINE_PROCESSING && !COIConfig.ENABLE_DUST_PRODUCTION.get()) {
            return 0;
        }

        DustReason effectiveReason = reason == null ? DustReason.UNKNOWN : reason;
        int incoming = expectedAmount;
        if (effectiveReason == DustReason.MACHINE_PROCESSING) {
            incoming = AlloyHousing.reduceEmission(level, pos, incoming);
        }
        PurificationResult purified = DustPurification.reduceNearby(level, pos, incoming);
        int deposited = purified.remaining();
        if (deposited > 0) {
            OriginiumDustManager.addDustAt(level, pos, deposited, effectiveReason);
        }

        if (COIConfig.ENABLE_DEBUG_LOGGING.get()) {
            CreateOriginiumIndustry.LOGGER.info(
                    "[OriDust] submit at [{}, {}, {}]: expected={}, sealed={}, captured={}, deposited={} (reason: {})",
                    pos.getX(), pos.getY(), pos.getZ(),
                    expectedAmount, incoming, purified.captured(), deposited, effectiveReason.getId()
            );
        }
        return deposited;
    }
}
