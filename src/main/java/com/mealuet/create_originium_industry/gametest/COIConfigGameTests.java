package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustFilterBlockEntity;
import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.DebugOverlayDetail;
import com.mealuet.create_originium_industry.config.PollutionSpreadStrategy;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.PlayerDeathDustHandler;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureHandler;
import com.mealuet.create_originium_industry.core.oridust.ProtectionHooks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Coverage for issue #6: COMMON/CLIENT config skeleton, protection hooks,
 * death burst formula, and multiplayer spread defaults.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class COIConfigGameTests {

    private COIConfigGameTests() {}

    @GameTest(template = "empty", batch = "config")
    public static void commonSpecLoadedWithFrozenDefaults(GameTestHelper helper) {
        helper.assertTrue(COIConfig.COMMON_SPEC.isLoaded(), "COMMON spec must load on GameTestServer");

        helper.assertTrue(COIConfig.ENABLE_DUST_DIFFUSION.get(), "enableDustDiffusion");
        helper.assertValueEqual(COIConfig.MAX_DUST_LEVEL.get(), 10000, "maxDustLevel");
        helper.assertValueEqual(COIConfig.DIFFUSION_INTERVAL.get(), 20, "diffusionInterval");
        helper.assertValueEqual(COIConfig.INIT_CHUNK_RADIUS.get(), 8, "initChunkRadius");
        helper.assertValueEqual(COIConfig.DIFFUSION_RATE.get(), 1.0, "diffusionRate");
        helper.assertValueEqual(COIConfig.DUST_DECAY_RATE.get(), 1, "dustDecayRate");

        helper.assertTrue(COIConfig.ENABLE_DUST_PRODUCTION.get(), "enableDustProduction");
        helper.assertValueEqual(COIConfig.DUST_FROM_MILLING.get(), 80, "dustFromMilling");
        helper.assertValueEqual(COIConfig.DUST_FROM_CRUSHING.get(), 100, "dustFromCrushing");

        helper.assertValueEqual(COIConfig.EFFECT_CHECK_INTERVAL.get(), 20, "effectCheckInterval");
        helper.assertValueEqual(COIConfig.DUST_EFFECT_THRESHOLD.get(), 2000, "dustEffectThreshold");
        helper.assertValueEqual(COIConfig.INFECTION_THRESHOLD.get(), 500, "infectionThreshold");
        helper.assertValueEqual(COIConfig.DEATH_DUST_BURST_AMOUNT.get(), 500, "deathDustBurstAmount");

        helper.assertTrue(COIConfig.ENABLE_REACTOR_MELTDOWN.get(), "enableReactorMeltdown");
        helper.assertTrue(COIConfig.ENABLE_DEATH_DUST_BURST.get(), "enableDeathDustBurst");

        helper.assertValueEqual(COIConfig.FILTER_ABSORPTION_RATE.get(), 5, "filterAbsorptionRate");
        helper.assertValueEqual(COIConfig.FILTER_SIEVE_DURABILITY.get(), 500, "filterSieveDurability");

        helper.assertTrue(COIConfig.ENABLE_DEBUG_COMMANDS.get(), "enableDebugCommands");
        helper.assertFalse(COIConfig.ENABLE_DEBUG_TOOLTIPS.get(), "enableDebugTooltips");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void additiveServerKnobsMatchDocumentedDefaults(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.DIFFUSION_LOSS_FACTOR.get(), 0.2, "diffusionLossFactor");
        helper.assertValueEqual(COIConfig.DIFFUSION_MIN_DIFFERENCE.get(), 50, "diffusionMinDifference");
        helper.assertValueEqual(COIConfig.DIFFUSION_TRANSFER_DIVISOR.get(), 128, "diffusionTransferDivisor");
        helper.assertValueEqual(COIConfig.RECENT_WRITE_TTL_TICKS.get(), 1200, "recentWriteTtlTicks");
        helper.assertValueEqual(COIConfig.MAX_EXPOSURE.get(), 10000, "maxExposure");
        helper.assertValueEqual(COIConfig.INFECTION_SYMPTOM_RATIO.get(), 0.5, "infectionSymptomRatio");
        helper.assertValueEqual(COIConfig.EXPOSURE_PER_AMPLIFIER.get(), 200, "exposurePerAmplifier");
        helper.assertValueEqual(COIConfig.DEATH_BURST_MIN_CONTAMINATION.get(), 100, "deathBurstMinContamination");
        helper.assertValueEqual(COIConfig.FILTER_MAX_SPEED_MULTIPLIER.get(), 4.0, "filterMaxSpeedMultiplier");
        helper.assertValueEqual(COIConfig.FILTER_SPEED_REFERENCE.get(), 64.0, "filterSpeedReference");
        helper.assertValueEqual(COIConfig.PROCESS_SIEVE_EMISSION_CAPTURE.get(), 0.4, "processSieveEmissionCapture");
        helper.assertValueEqual(COIConfig.NOZZLE_TRANSFER_AMOUNT.get(), 80, "nozzleTransferAmount");
        helper.assertValueEqual(COIConfig.METER_COMPARATOR_FULL_DUST.get(), 8000, "meterComparatorFullDust");
        helper.assertTrue(COIConfig.ENABLE_RAW_ORIGINIUM_ORE.get(), "enableRawOriginiumOre");
        helper.assertValueEqual(COIConfig.RAW_ORIGINIUM_VEIN_SIZE.get(), 4, "veinSize");
        helper.assertValueEqual(COIConfig.RAW_ORIGINIUM_VEINS_PER_CHUNK.get(), 4, "veinsPerChunk");
        helper.assertTrue(COIConfig.ENABLE_PROTECTION.get(), "enableProtection");
        helper.assertValueEqual(COIConfig.PROTECTION_EXPOSURE_REDUCTION.get(), 0.5, "protectionExposureReduction");
        helper.assertValueEqual(COIConfig.DEATH_EXPOSURE_RETAIN.get(), 0.0, "deathExposureRetain");
        helper.assertValueEqual(COIConfig.DEATH_INFECTION_RETAIN.get(), 0.25, "deathInfectionRetain");
        helper.assertTrue(COIConfig.ENABLE_INFECTION_STAGES.get(), "enableInfectionStages");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void reactorSkeletonDefaults(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.REACTOR_CORE_HEAT.get(), 1000, "coreHeatValue");
        helper.assertValueEqual(COIConfig.REACTOR_MOLTEN_HEAT_CAPACITY.get(), 1.0, "moltenHeatCapacity");
        helper.assertValueEqual(COIConfig.REACTOR_PUREST_HEAT_CAPACITY.get(), 2.5, "purestHeatCapacity");
        helper.assertValueEqual(COIConfig.REACTOR_COOLING_MULTIPLIER.get(), 1.0, "coolingMultiplier");
        helper.assertValueEqual(COIConfig.REACTOR_MELTDOWN_THRESHOLD.get(), 100.0, "meltdownThreshold");
        helper.assertValueEqual(COIConfig.REACTOR_MELTDOWN_DUST_BURST.get(), 5000, "meltdownDustBurst");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void multiplayerPolicyDefaultsMatchSingleplayer(GameTestHelper helper) {
        helper.assertValueEqual(
                COIConfig.POLLUTION_SPREAD_STRATEGY.get(),
                PollutionSpreadStrategy.PLAYER_LOCAL,
                "pollutionSpreadStrategy"
        );
        helper.assertValueEqual(COIConfig.DEDICATED_SERVER_DIFFUSION_MULTIPLIER.get(), 1.0, "dedicated multiplier");
        helper.assertValueEqual(COIConfig.DEDICATED_SERVER_RADIUS_BONUS.get(), 0, "dedicated radius bonus");
        helper.assertTrue(COIConfig.SYNC_DUST_TO_CLIENTS.get(), "sync nearby dust to clients");
        helper.assertValueEqual(COIConfig.DUST_SYNC_INTERVAL.get(), 20, "dustSyncInterval");
        helper.assertValueEqual(COIConfig.DUST_SYNC_RADIUS.get(), 8, "dustSyncRadius");
        helper.assertValueEqual(COIConfig.dustSyncRadius(false), 8, "SP sync radius clamped to active set");

        helper.assertValueEqual(COIConfig.effectiveDiffusionRate(false), 1.0, "SP diffusion");
        helper.assertValueEqual(COIConfig.effectiveDiffusionRate(true), 1.0, "dedicated default diffusion");
        helper.assertValueEqual(COIConfig.effectiveInitChunkRadius(false), 8, "SP radius");
        helper.assertValueEqual(COIConfig.effectiveInitChunkRadius(true), 8, "dedicated default radius");
        helper.assertFalse(COIConfig.isolateMachineSpread(false), "SP isolate");
        helper.assertFalse(COIConfig.isolateMachineSpread(true), "dedicated isolate under player_local");

        helper.assertValueEqual(PollutionSpreadStrategy.AGGRESSIVE.dedicatedDiffusionScale(), 1.5, "aggressive scale");
        helper.assertValueEqual(PollutionSpreadStrategy.AGGRESSIVE.dedicatedRadiusBonus(), 4, "aggressive radius");
        helper.assertTrue(PollutionSpreadStrategy.FROZEN_FAR.isolateMachineSpread(), "frozen_far isolates");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void clientOptionsSafeWhenClientSpecMissing(GameTestHelper helper) {
        helper.assertValueEqual(COIClientOptions.particleDensity(), 0.4, "default density");
        helper.assertFalse(COIClientOptions.reduceFlicker(), "default reduceFlicker");
        helper.assertFalse(COIClientOptions.simplifyParticles(), "default simplifyParticles");
        helper.assertFalse(COIClientOptions.highContrastIndicators(), "default highContrast");
        helper.assertValueEqual(COIClientOptions.uiDetailLevel(), UiDetailLevel.STANDARD, "default ui");
        helper.assertValueEqual(COIClientOptions.debugOverlayDetail(), DebugOverlayDetail.OFF, "default overlay");
        helper.assertTrue(COIClientOptions.showSicknessHud(), "sickness HUD on by default");
        helper.assertValueEqual(COIClientOptions.particleCount(10), 4, "0.4 * 10 particles");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void protectionHooksUnprotectedByDefault(GameTestHelper helper) {
        helper.assertValueEqual(ProtectionHooks.incomingFactor(0, 0.5), 1.0, "no gear");
        helper.assertValueEqual(ProtectionHooks.incomingFactor(2, 0.5), 0.5, "full set of 2");
        helper.assertValueEqual(ProtectionHooks.incomingFactor(1, 0.5), 0.75, "half set scales");
        helper.assertValueEqual(ProtectionHooks.incomingFactor(4, 0.5), 0.5, "extra pieces cap at full");
        helper.assertValueEqual(ProtectionHooks.countProtectionPieces(null), 0, "null entity");
        helper.assertValueEqual(COIConfig.PROTECTION_FULL_SET_PIECES.get(), 2, "default full set");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void deathBurstAndExposureClampUseConfig(GameTestHelper helper) {
        helper.assertValueEqual(PlayerDeathDustHandler.computeBurstAmount(0, 0), 0, "clean death");
        helper.assertValueEqual(PlayerDeathDustHandler.computeBurstAmount(99, 99), 0, "below threshold");
        helper.assertValueEqual(PlayerDeathDustHandler.computeBurstAmount(100, 0), 50, "min burst scale");
        helper.assertValueEqual(PlayerDeathDustHandler.computeBurstAmount(0, 100), 50, "infection-only burst");

        PlayerExposureData data = new PlayerExposureData();
        data.setExposure(Integer.MAX_VALUE);
        data.setInfection(Integer.MAX_VALUE);
        helper.assertValueEqual(data.getExposure(), COIConfig.maxExposure(), "exposure clamp");
        helper.assertValueEqual(data.getInfection(), COIConfig.maxInfection(), "infection clamp");

        helper.assertValueEqual(PlayerExposureHandler.positiveGain(0.0), 0, "zero gain stays zero");
        helper.assertValueEqual(PlayerExposureHandler.positiveGain(0.4), 1, "fractional gain");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "config")
    public static void dustLevelAndFilterSpeedUseConfig(GameTestHelper helper) {
        helper.assertValueEqual(DustLevel.fromDust(0), DustLevel.SAFE, "safe");
        helper.assertValueEqual(DustLevel.fromDust(499), DustLevel.SAFE, "safe high");
        helper.assertValueEqual(DustLevel.fromDust(500), DustLevel.LOW, "low");
        helper.assertValueEqual(DustLevel.fromDust(1500), DustLevel.MEDIUM, "medium");
        helper.assertValueEqual(DustLevel.fromDust(4000), DustLevel.HIGH, "high");
        helper.assertValueEqual(DustLevel.fromDust(8000), DustLevel.CRITICAL, "critical");

        helper.assertValueEqual(DustFilterBlockEntity.speedMultiplier(32f), 1.0, "floor 1x");
        helper.assertValueEqual(DustFilterBlockEntity.speedMultiplier(64f), 1.0, "reference 1x");
        helper.assertValueEqual(DustFilterBlockEntity.speedMultiplier(256f), 4.0, "cap 4x");
        helper.succeed();
    }
}
