package com.mealuet.create_originium_industry.config;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common (server) and client config specs for Create: Originium Industry.
 * <p>
 * Existing top-level COMMON sections are frozen: {@code dust_diffusion},
 * {@code dust_production}, {@code player_exposure}, {@code feature_toggles},
 * {@code dust_filter}, {@code debug}. New keys are additive only.
 * <p>
 * Client spec is registered as {@link net.neoforged.fml.config.ModConfig.Type#CLIENT}
 * and is <em>not</em> loaded on a dedicated server — read it through
 * {@link COIClientOptions}.
 */
public class COIConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // --- Dust Diffusion ---
    public static final ModConfigSpec.BooleanValue ENABLE_DUST_DIFFUSION;
    public static final ModConfigSpec.IntValue MAX_DUST_LEVEL;
    public static final ModConfigSpec.IntValue DIFFUSION_INTERVAL;
    public static final ModConfigSpec.IntValue INIT_CHUNK_RADIUS;
    public static final ModConfigSpec.DoubleValue DIFFUSION_RATE;
    public static final ModConfigSpec.IntValue DUST_DECAY_RATE;
    public static final ModConfigSpec.DoubleValue DIFFUSION_LOSS_FACTOR;
    public static final ModConfigSpec.IntValue DIFFUSION_MIN_DIFFERENCE;
    public static final ModConfigSpec.IntValue DIFFUSION_TRANSFER_DIVISOR;
    public static final ModConfigSpec.IntValue RECENT_WRITE_TTL_TICKS;
    public static final ModConfigSpec.IntValue DUST_LEVEL_LOW;
    public static final ModConfigSpec.IntValue DUST_LEVEL_MEDIUM;
    public static final ModConfigSpec.IntValue DUST_LEVEL_HIGH;
    public static final ModConfigSpec.IntValue DUST_LEVEL_CRITICAL;

    // --- Dust Production ---
    public static final ModConfigSpec.BooleanValue ENABLE_DUST_PRODUCTION;
    public static final ModConfigSpec.IntValue DUST_FROM_MILLING;
    public static final ModConfigSpec.IntValue DUST_FROM_CRUSHING;
    public static final ModConfigSpec.IntValue DUST_FROM_SHARD_MIXING;
    public static final ModConfigSpec.IntValue DUST_FROM_ORIGINIUM_MELTING;
    public static final ModConfigSpec.IntValue DUST_FROM_ALLOY_MIXING;

    // --- Player Exposure ---
    public static final ModConfigSpec.IntValue EFFECT_CHECK_INTERVAL;
    public static final ModConfigSpec.IntValue DUST_EFFECT_THRESHOLD;
    public static final ModConfigSpec.IntValue DUST_PER_EFFECT_LEVEL;
    public static final ModConfigSpec.IntValue MAX_EFFECT_AMPLIFIER;
    public static final ModConfigSpec.DoubleValue EXPOSURE_GAIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue INFECTION_GAIN_MULTIPLIER;
    public static final ModConfigSpec.IntValue EXPOSURE_DECAY_RATE;
    public static final ModConfigSpec.IntValue INFECTION_THRESHOLD;
    public static final ModConfigSpec.IntValue DEATH_DUST_BURST_AMOUNT;
    public static final ModConfigSpec.IntValue MAX_EXPOSURE;
    public static final ModConfigSpec.IntValue MAX_INFECTION;
    public static final ModConfigSpec.DoubleValue INFECTION_SYMPTOM_RATIO;
    public static final ModConfigSpec.IntValue EXPOSURE_PER_AMPLIFIER;
    public static final ModConfigSpec.IntValue EFFECT_APPLY_THRESHOLD;
    public static final ModConfigSpec.IntValue DEATH_BURST_MIN_CONTAMINATION;
    public static final ModConfigSpec.DoubleValue DEATH_BURST_SCALE_DIVISOR;
    public static final ModConfigSpec.DoubleValue DEATH_BURST_MAX_SCALE;
    public static final ModConfigSpec.IntValue SICKNESS_EFFECT_INTERVAL;

    // --- Feature Toggles ---
    public static final ModConfigSpec.BooleanValue ENABLE_REACTOR_MELTDOWN;
    public static final ModConfigSpec.BooleanValue ENABLE_DEATH_DUST_BURST;

    // --- Dust Filter ---
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_RATE;
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_INTERVAL;
    public static final ModConfigSpec.IntValue FILTER_SIEVE_DURABILITY;
    public static final ModConfigSpec.DoubleValue FILTER_MAX_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FILTER_SPEED_REFERENCE;

    // --- Protection (hooks; no gear registered yet) ---
    public static final ModConfigSpec.BooleanValue ENABLE_PROTECTION;
    public static final ModConfigSpec.DoubleValue PROTECTION_EXPOSURE_REDUCTION;
    public static final ModConfigSpec.DoubleValue PROTECTION_INFECTION_REDUCTION;
    public static final ModConfigSpec.BooleanValue PROTECTION_REQUIRES_FULL_SET;
    public static final ModConfigSpec.IntValue PROTECTION_FULL_SET_PIECES;

    // --- Reactor (M3 stub knobs) ---
    public static final ModConfigSpec.IntValue REACTOR_CORE_HEAT;
    public static final ModConfigSpec.DoubleValue REACTOR_MOLTEN_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue REACTOR_PUREST_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue REACTOR_COOLING_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_GAIN;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_DECAY;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_WARNING;
    public static final ModConfigSpec.DoubleValue REACTOR_MELTDOWN_THRESHOLD;
    public static final ModConfigSpec.IntValue REACTOR_MELTDOWN_DUST_BURST;
    public static final ModConfigSpec.IntValue REACTOR_COOLANT_MIN_FLOW;

    // --- Multiplayer pollution ---
    public static final ModConfigSpec.EnumValue<PollutionSpreadStrategy> POLLUTION_SPREAD_STRATEGY;
    public static final ModConfigSpec.DoubleValue DEDICATED_SERVER_DIFFUSION_MULTIPLIER;
    public static final ModConfigSpec.IntValue DEDICATED_SERVER_RADIUS_BONUS;
    public static final ModConfigSpec.BooleanValue SYNC_DUST_TO_CLIENTS;

    // --- Debug ---
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_COMMANDS;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING;

    // --- Client / accessibility ---
    public static final ModConfigSpec.BooleanValue REDUCE_FLICKER;
    public static final ModConfigSpec.BooleanValue SIMPLIFY_PARTICLES;
    public static final ModConfigSpec.DoubleValue PARTICLE_DENSITY;
    public static final ModConfigSpec.BooleanValue HIGH_CONTRAST_INDICATORS;
    public static final ModConfigSpec.EnumValue<UiDetailLevel> UI_DETAIL_LEVEL;
    public static final ModConfigSpec.EnumValue<DebugOverlayDetail> DEBUG_OVERLAY_DETAIL;
    public static final ModConfigSpec.BooleanValue SHOW_SICKNESS_HUD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // ==================== Dust Diffusion ====================
        builder.comment("Dust diffusion settings").push("dust_diffusion");

        ENABLE_DUST_DIFFUSION = builder
                .comment("Whether originium dust diffusion between chunks is enabled")
                .define("enableDustDiffusion", true);
        MAX_DUST_LEVEL = builder
                .comment("Maximum dust level per chunk")
                .defineInRange("maxDustLevel", 10000, 1000, 100000);
        DIFFUSION_INTERVAL = builder
                .comment("Ticks between diffusion updates (20 = 1 second)")
                .defineInRange("diffusionInterval", 20, 1, 200);
        INIT_CHUNK_RADIUS = builder
                .comment("Logical chunk radius around players for the dust diffusion/decay active set (and startup migration). Far pollution outside this set plus recently written keys is left frozen until a player approaches.")
                .defineInRange("initChunkRadius", 8, 1, 32);
        DIFFUSION_RATE = builder
                .comment("Diffusion speed multiplier (higher = faster spread)")
                .defineInRange("diffusionRate", 1.0, 0.1, 10.0);
        DUST_DECAY_RATE = builder
                .comment("Amount of dust that naturally decays per diffusion tick (0 = no decay)")
                .defineInRange("dustDecayRate", 1, 0, 100);
        DIFFUSION_LOSS_FACTOR = builder
                .comment("Fraction of transferred dust lost to the environment (neighbor receives 1 - this). Default 0.2 matches the original 80% receive factor.")
                .defineInRange("diffusionLossFactor", 0.2, 0.0, 0.9);
        DIFFUSION_MIN_DIFFERENCE = builder
                .comment("Minimum dust difference between neighbors before transfer (filters low-level oscillation)")
                .defineInRange("diffusionMinDifference", 50, 1, 5000);
        DIFFUSION_TRANSFER_DIVISOR = builder
                .comment("Divides (difference * diffusionRate) to keep spread gradual. Default 128 matches original pacing.")
                .defineInRange("diffusionTransferDivisor", 128, 8, 1024);
        RECENT_WRITE_TTL_TICKS = builder
                .comment("How long a machine/filter/death write keeps its chunk in the active set without a player nearby (20 ticks = 1 second)")
                .defineInRange("recentWriteTtlTicks", 20 * 60, 20, 20 * 600);
        DUST_LEVEL_LOW = builder
                .comment("Dust classification: values below this are Safe")
                .defineInRange("dustLevelLow", 500, 1, 100000);
        DUST_LEVEL_MEDIUM = builder
                .comment("Dust classification: Low risk until this value")
                .defineInRange("dustLevelMedium", 1500, 1, 100000);
        DUST_LEVEL_HIGH = builder
                .comment("Dust classification: Medium risk until this value")
                .defineInRange("dustLevelHigh", 4000, 1, 100000);
        DUST_LEVEL_CRITICAL = builder
                .comment("Dust classification: High risk until this value; at or above is Critical")
                .defineInRange("dustLevelCritical", 8000, 1, 100000);

        builder.pop();

        // ==================== Dust Production ====================
        builder.comment("Dust production from Create machine processing").push("dust_production");

        ENABLE_DUST_PRODUCTION = builder
                .comment("Whether Create machines produce originium dust when processing originium recipes")
                .define("enableDustProduction", true);
        DUST_FROM_MILLING = builder
                .comment("Dust emitted when milling raw originium")
                .defineInRange("dustFromMilling", 80, 0, 1000);
        DUST_FROM_CRUSHING = builder
                .comment("Dust emitted when crushing raw originium")
                .defineInRange("dustFromCrushing", 100, 0, 1000);
        DUST_FROM_SHARD_MIXING = builder
                .comment("Dust emitted when mixing originium shards into originium")
                .defineInRange("dustFromShardMixing", 120, 0, 1000);
        DUST_FROM_ORIGINIUM_MELTING = builder
                .comment("Dust emitted when melting originium into molten originium (superheated mixing)")
                .defineInRange("dustFromOriginiumMelting", 200, 0, 1000);
        DUST_FROM_ALLOY_MIXING = builder
                .comment("Dust emitted when mixing molten originium with iron to create alloy")
                .defineInRange("dustFromAlloyMixing", 60, 0, 1000);

        builder.pop();

        // ==================== Player Exposure ====================
        builder.comment("Player exposure settings").push("player_exposure");

        EFFECT_CHECK_INTERVAL = builder
                .comment("Ticks between player effect checks (20 = 1 second)")
                .defineInRange("effectCheckInterval", 20, 1, 200);
        DUST_EFFECT_THRESHOLD = builder
                .comment("Chunk dust level at which effects start applying to players")
                .defineInRange("dustEffectThreshold", 2000, 100, 50000);
        DUST_PER_EFFECT_LEVEL = builder
                .comment("Additional dust needed for each higher effect amplifier level")
                .defineInRange("dustPerEffectLevel", 1500, 100, 10000);
        MAX_EFFECT_AMPLIFIER = builder
                .comment("Maximum effect amplifier (0-based, so 4 = level V)")
                .defineInRange("maxEffectAmplifier", 4, 0, 10);
        EXPOSURE_GAIN_MULTIPLIER = builder
                .comment("Multiplier for player exposure gain rate (0 = no exposure gain)")
                .defineInRange("exposureGainMultiplier", 1.0, 0.0, 10.0);
        INFECTION_GAIN_MULTIPLIER = builder
                .comment("Multiplier for player infection gain rate (0 = no infection gain)")
                .defineInRange("infectionGainMultiplier", 1.0, 0.0, 10.0);
        EXPOSURE_DECAY_RATE = builder
                .comment("Exposure decay per check interval when player is in a safe area (dust < threshold)")
                .defineInRange("exposureDecayRate", 5, 0, 100);
        INFECTION_THRESHOLD = builder
                .comment("Exposure level at which infection starts accumulating")
                .defineInRange("infectionThreshold", 500, 100, 5000);
        DEATH_DUST_BURST_AMOUNT = builder
                .comment("Amount of dust released into chunk when player dies from originium exposure")
                .defineInRange("deathDustBurstAmount", 500, 0, 5000);
        MAX_EXPOSURE = builder
                .comment("Hard cap for stored player exposure (NBT clamp; raising this does not rewrite old saves)")
                .defineInRange("maxExposure", 10000, 100, 100000);
        MAX_INFECTION = builder
                .comment("Hard cap for stored player infection (NBT clamp; infection still never decays)")
                .defineInRange("maxInfection", 10000, 100, 100000);
        INFECTION_SYMPTOM_RATIO = builder
                .comment("How much infection counts as lingering exposure in clean areas (0.5 = 50%)")
                .defineInRange("infectionSymptomRatio", 0.5, 0.0, 1.0);
        EXPOSURE_PER_AMPLIFIER = builder
                .comment("Effective exposure required per sickness amplifier step")
                .defineInRange("exposurePerAmplifier", 200, 10, 10000);
        EFFECT_APPLY_THRESHOLD = builder
                .comment("Minimum effective exposure before ori_dust_sickness is applied")
                .defineInRange("effectApplyThreshold", 100, 0, 10000);
        DEATH_BURST_MIN_CONTAMINATION = builder
                .comment("Death burst requires exposure OR infection to be at least this value")
                .defineInRange("deathBurstMinContamination", 100, 0, 10000);
        DEATH_BURST_SCALE_DIVISOR = builder
                .comment("Burst scale = min(maxScale, (exposure + infection) / this). Default 1000 matches original pacing.")
                .defineInRange("deathBurstScaleDivisor", 1000.0, 1.0, 100000.0);
        DEATH_BURST_MAX_SCALE = builder
                .comment("Maximum multiplier applied to deathDustBurstAmount")
                .defineInRange("deathBurstMaxScale", 2.0, 0.1, 10.0);
        SICKNESS_EFFECT_INTERVAL = builder
                .comment("Ticks between ori_dust_sickness slowness/weakness pulses (20 = 1 second)")
                .defineInRange("sicknessEffectInterval", 20, 1, 200);

        builder.pop();

        // ==================== Feature Toggles ====================
        builder.comment("Feature toggle settings").push("feature_toggles");

        ENABLE_REACTOR_MELTDOWN = builder
                .comment("Whether reactor meltdown is enabled (Phase 6 / M3)")
                .define("enableReactorMeltdown", true);
        ENABLE_DEATH_DUST_BURST = builder
                .comment("Whether players release dust upon death from originium exposure (Phase 3+)")
                .define("enableDeathDustBurst", true);

        builder.pop();

        // ==================== Dust Filter ====================
        builder.comment("Dust filter block settings").push("dust_filter");

        FILTER_ABSORPTION_RATE = builder
                .comment("Base dust absorbed per cycle at minimum speed")
                .defineInRange("filterAbsorptionRate", 5, 1, 100);
        FILTER_ABSORPTION_INTERVAL = builder
                .comment("Ticks between each absorption cycle (20 = 1 second)")
                .defineInRange("filterAbsorptionInterval", 20, 5, 200);
        FILTER_SIEVE_DURABILITY = builder
                .comment("Total absorption cycles before a sieve is consumed")
                .defineInRange("filterSieveDurability", 500, 50, 10000);
        FILTER_MAX_SPEED_MULTIPLIER = builder
                .comment("Maximum absorption multiplier from rotational speed")
                .defineInRange("filterMaxSpeedMultiplier", 4.0, 1.0, 16.0);
        FILTER_SPEED_REFERENCE = builder
                .comment("RPM at which the speed multiplier reaches 1x before clamping (Create speed / this)")
                .defineInRange("filterSpeedReference", 64.0, 1.0, 256.0);

        builder.pop();

        // ==================== Protection ====================
        builder.comment(
                "Protection equipment hooks. No protection items are registered yet;",
                "these knobs are used once gear (or the originium_protection item tag) exists.",
                "Unprotected players always take full exposure; changing these values does not rewrite saves."
        ).push("protection");

        ENABLE_PROTECTION = builder
                .comment("Master switch for protection-gear reduction. Off = ignore tagged gear.")
                .define("enableProtection", true);
        PROTECTION_EXPOSURE_REDUCTION = builder
                .comment("Fraction of exposure gain removed by a full protection set (0.5 = half gain)")
                .defineInRange("protectionExposureReduction", 0.5, 0.0, 1.0);
        PROTECTION_INFECTION_REDUCTION = builder
                .comment("Fraction of infection gain removed by a full protection set")
                .defineInRange("protectionInfectionReduction", 0.35, 0.0, 1.0);
        PROTECTION_REQUIRES_FULL_SET = builder
                .comment("If true, partial sets give no reduction. If false, reduction scales with equipped pieces / fullSetPieces.")
                .define("protectionRequiresFullSet", false);
        PROTECTION_FULL_SET_PIECES = builder
                .comment("How many tagged protection items count as a full set")
                .defineInRange("protectionFullSetPieces", 4, 1, 8);

        builder.pop();

        // ==================== Reactor ====================
        builder.comment(
                "Reactor gameplay is not implemented yet (M3). These knobs are the contract",
                "future heat/cooling/meltdown code will read. /coi_debug reactor status prints them.",
                "enableReactorMeltdown lives under feature_toggles (frozen key)."
        ).push("reactor");

        REACTOR_CORE_HEAT = builder
                .comment("Core heat units produced per tick at nominal load (stub)")
                .defineInRange("coreHeatValue", 1000, 1, 1_000_000);
        REACTOR_MOLTEN_HEAT_CAPACITY = builder
                .comment("Heat capacity multiplier for molten originium coolant/fuel (stub)")
                .defineInRange("moltenHeatCapacity", 1.0, 0.01, 100.0);
        REACTOR_PUREST_HEAT_CAPACITY = builder
                .comment("Heat capacity multiplier for purest molten originium (hotter fuel, stub)")
                .defineInRange("purestHeatCapacity", 2.5, 0.01, 100.0);
        REACTOR_COOLING_MULTIPLIER = builder
                .comment("Global cooling effectiveness (stub)")
                .defineInRange("coolingMultiplier", 1.0, 0.0, 10.0);
        REACTOR_INSTABILITY_GAIN = builder
                .comment("Instability gained per tick when cooling is insufficient (stub)")
                .defineInRange("instabilityGainPerTick", 0.01, 0.0, 10.0);
        REACTOR_INSTABILITY_DECAY = builder
                .comment("Instability lost per tick when adequately cooled (stub)")
                .defineInRange("instabilityDecayPerTick", 0.005, 0.0, 10.0);
        REACTOR_INSTABILITY_WARNING = builder
                .comment("Instability at which warning indicators should fire (stub)")
                .defineInRange("instabilityWarningThreshold", 50.0, 0.0, 10000.0);
        REACTOR_MELTDOWN_THRESHOLD = builder
                .comment("Instability that triggers meltdown when feature_toggles.enableReactorMeltdown is true (stub)")
                .defineInRange("meltdownThreshold", 100.0, 1.0, 10000.0);
        REACTOR_MELTDOWN_DUST_BURST = builder
                .comment("Chunk dust released on meltdown (stub)")
                .defineInRange("meltdownDustBurst", 5000, 0, 100000);
        REACTOR_COOLANT_MIN_FLOW = builder
                .comment("Minimum coolant flow (mB/t) treated as adequate cooling (stub)")
                .defineInRange("coolantMinimumFlow", 10, 0, 10000);

        builder.pop();

        // ==================== Multiplayer ====================
        builder.comment(
                "Dedicated-server pollution spread policy. Singleplayer ignores strategy scaling",
                "and uses dust_diffusion knobs only. Additive; default matches current simulation."
        ).push("multiplayer");

        POLLUTION_SPREAD_STRATEGY = builder
                .comment("PLAYER_LOCAL = same as singleplayer; AGGRESSIVE = faster/farther on dedicated; FROZEN_FAR = no isolated-machine neighbor bleed")
                .defineEnum("pollutionSpreadStrategy", PollutionSpreadStrategy.PLAYER_LOCAL);
        DEDICATED_SERVER_DIFFUSION_MULTIPLIER = builder
                .comment("Extra multiplier on diffusionRate for dedicated servers only (1.0 = no change). Stacks with strategy.")
                .defineInRange("dedicatedServerDiffusionMultiplier", 1.0, 0.1, 10.0);
        DEDICATED_SERVER_RADIUS_BONUS = builder
                .comment("Extra chunks added to initChunkRadius on dedicated servers (0 = no change). Stacks with strategy.")
                .defineInRange("dedicatedServerRadiusBonus", 0, 0, 24);
        SYNC_DUST_TO_CLIENTS = builder
                .comment("Reserved: when true, future networking will sync chunk dust to nearby players for HUD/particles. Currently unused (no packet).")
                .define("syncDustToClients", false);

        builder.pop();

        // ==================== Debug ====================
        builder.comment("Debug settings").push("debug");

        ENABLE_DEBUG_COMMANDS = builder
                .comment("Whether /coi_debug commands are available (requires OP)")
                .define("enableDebugCommands", true);
        ENABLE_DEBUG_TOOLTIPS = builder
                .comment("Whether debug tooltips are shown on originium items")
                .define("enableDebugTooltips", false);
        ENABLE_DEBUG_LOGGING = builder
                .comment("Whether verbose debug logging is enabled (may spam console)")
                .define("enableDebugLogging", false);

        builder.pop();

        COMMON_SPEC = builder.build();

        // ==================== Client ====================
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();

        client.comment(
                "Client-only accessibility and presentation. Does not affect simulation or saves.",
                "Missing keys use the defaults below; adding this file to an old instance is safe."
        ).push("client");

        REDUCE_FLICKER = client
                .comment("Disable pulsing/flashing on the sickness HUD and slow ambient particle spawn")
                .define("reduceFlicker", false);
        SIMPLIFY_PARTICLES = client
                .comment("Disable Originium Industry ambient particles entirely (overrides particleDensity)")
                .define("simplifyParticles", false);
        PARTICLE_DENSITY = client
                .comment("Ambient particle density when the player has ori_dust_sickness (0 = none, 1 = full)")
                .defineInRange("particleDensity", 0.4, 0.0, 1.0);
        HIGH_CONTRAST_INDICATORS = client
                .comment("High-contrast sickness HUD (opaque background, brighter text)")
                .define("highContrastIndicators", false);
        UI_DETAIL_LEVEL = client
                .comment("How much COI UI to show: minimal (hide HUD extras), standard, verbose (goggle numbers)")
                .defineEnum("uiDetailLevel", UiDetailLevel.STANDARD);
        DEBUG_OVERLAY_DETAIL = client
                .comment("Extra debug overlay: off, compact (sickness amplifier), full (also notes unsynced dust)")
                .defineEnum("debugOverlayDetail", DebugOverlayDetail.OFF);
        SHOW_SICKNESS_HUD = client
                .comment("Show a small HUD hint when the player has Originium Exposure Sickness")
                .define("showSicknessHud", true);

        client.pop();

        CLIENT_SPEC = client.build();
    }

    private COIConfig() {}

    public static int maxExposure() {
        return COMMON_SPEC.isLoaded() ? MAX_EXPOSURE.get() : 10000;
    }

    public static int maxInfection() {
        return COMMON_SPEC.isLoaded() ? MAX_INFECTION.get() : 10000;
    }

    public static int recentWriteTtlTicks() {
        return COMMON_SPEC.isLoaded() ? RECENT_WRITE_TTL_TICKS.get() : 20 * 60;
    }

    public static boolean debugTooltipsEnabled() {
        return COMMON_SPEC.isLoaded() && ENABLE_DEBUG_TOOLTIPS.get();
    }

    /**
     * Effective diffusion rate for this server. Singleplayer uses
     * {@link #DIFFUSION_RATE} only. Dedicated servers also apply
     * {@link #DEDICATED_SERVER_DIFFUSION_MULTIPLIER} and the spread strategy.
     */
    public static double effectiveDiffusionRate(boolean dedicatedServer) {
        double rate = DIFFUSION_RATE.get();
        if (!dedicatedServer) {
            return rate;
        }
        return rate * DEDICATED_SERVER_DIFFUSION_MULTIPLIER.get()
                * POLLUTION_SPREAD_STRATEGY.get().dedicatedDiffusionScale();
    }

    /**
     * Effective player-radius for the dust active set, clamped to the same
     * 1–32 range as {@link #INIT_CHUNK_RADIUS}.
     */
    public static int effectiveInitChunkRadius(boolean dedicatedServer) {
        int radius = INIT_CHUNK_RADIUS.get();
        if (dedicatedServer) {
            radius += DEDICATED_SERVER_RADIUS_BONUS.get();
            radius += POLLUTION_SPREAD_STRATEGY.get().dedicatedRadiusBonus();
        }
        return Math.max(1, Math.min(32, radius));
    }

    public static boolean isolateMachineSpread(boolean dedicatedServer) {
        return dedicatedServer && POLLUTION_SPREAD_STRATEGY.get().isolateMachineSpread();
    }

    public static boolean isDedicated(MinecraftServer server) {
        return server != null && !server.isSingleplayer();
    }
}
