package com.mealuet.create_originium_industry.config;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common (server) and client config specs for Create: Originium Industry.
 * <p>
 * Existing top-level COMMON sections are frozen: {@code dust_diffusion},
 * {@code dust_production}, {@code player_exposure}, {@code feature_toggles},
 * {@code dust_filter}, {@code debug}. Additive sections: {@code protection},
 * {@code infection}, {@code reactor}, {@code multiplayer}, {@code dust_nozzle},
 * {@code dust_meter}, {@code worldgen}, {@code purest_line}, {@code alloy_parts}.
 * New keys are additive only. Frozen {@code reactor} keys keep their names;
 * M3 adds coolant/chamber/RPM knobs beside them.
 * {@code dust_meter}, {@code worldgen}, {@code purest_line}, {@code alloy_parts}.
 * New keys are additive only.
 * <p>
 * Client spec is registered as {@link net.neoforged.fml.config.ModConfig.Type#CLIENT}
 * and is <em>not</em> loaded on a dedicated server — read it through
 * {@link COIClientOptions}. Additive client keys {@code enableIndustrialSounds},
 * {@code soundDensity}, and {@code nonColorAlerts} sit beside the original
 * accessibility knobs.
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
    public static final ModConfigSpec.IntValue DUST_FROM_TAGGED_ITEM;

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
    public static final ModConfigSpec.DoubleValue DEATH_EXPOSURE_RETAIN;
    public static final ModConfigSpec.DoubleValue DEATH_INFECTION_RETAIN;

    // --- Feature Toggles ---
    public static final ModConfigSpec.BooleanValue ENABLE_REACTOR_MELTDOWN;
    public static final ModConfigSpec.BooleanValue ENABLE_DEATH_DUST_BURST;

    // --- Dust Filter ---
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_RATE;
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_INTERVAL;
    public static final ModConfigSpec.IntValue FILTER_SIEVE_DURABILITY;
    public static final ModConfigSpec.DoubleValue FILTER_MAX_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FILTER_SPEED_REFERENCE;
    public static final ModConfigSpec.DoubleValue FILTER_EMISSION_CAPTURE;
    public static final ModConfigSpec.IntValue FILTER_BYPRODUCT_DUST_PER_ITEM;
    public static final ModConfigSpec.DoubleValue PROCESS_SIEVE_EMISSION_CAPTURE;

    // --- Dust nozzle (Encased Fan attachment) ---
    public static final ModConfigSpec.IntValue NOZZLE_TRANSFER_AMOUNT;
    public static final ModConfigSpec.IntValue NOZZLE_TRANSFER_INTERVAL;
    public static final ModConfigSpec.DoubleValue NOZZLE_MAX_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue NOZZLE_SPEED_REFERENCE;

    // --- Dust meter ---
    public static final ModConfigSpec.IntValue METER_COMPARATOR_FULL_DUST;
    public static final ModConfigSpec.IntValue METER_SYNC_INTERVAL;

    // --- Worldgen (raw originium ore) ---
    public static final ModConfigSpec.BooleanValue ENABLE_RAW_ORIGINIUM_ORE;
    public static final ModConfigSpec.IntValue RAW_ORIGINIUM_VEIN_SIZE;
    public static final ModConfigSpec.IntValue RAW_ORIGINIUM_VEINS_PER_CHUNK;
    public static final ModConfigSpec.IntValue RAW_ORIGINIUM_MIN_Y;
    public static final ModConfigSpec.IntValue RAW_ORIGINIUM_MAX_Y;
    public static final ModConfigSpec.DoubleValue RAW_ORIGINIUM_DISCARD_CHANCE;

    // --- Protection ---
    public static final ModConfigSpec.BooleanValue ENABLE_PROTECTION;
    public static final ModConfigSpec.DoubleValue PROTECTION_EXPOSURE_REDUCTION;
    public static final ModConfigSpec.DoubleValue PROTECTION_INFECTION_REDUCTION;
    public static final ModConfigSpec.BooleanValue PROTECTION_REQUIRES_FULL_SET;
    public static final ModConfigSpec.IntValue PROTECTION_FULL_SET_PIECES;
    public static final ModConfigSpec.IntValue PROTECTION_DURABILITY_LOSS;

    // --- Infection stages ---
    public static final ModConfigSpec.BooleanValue ENABLE_INFECTION_STAGES;
    public static final ModConfigSpec.IntValue INFECTION_STAGE_WEAKNESS;
    public static final ModConfigSpec.IntValue INFECTION_STAGE_RESTRICTED;
    public static final ModConfigSpec.IntValue INFECTION_STAGE_GROWTH;
    public static final ModConfigSpec.IntValue INFECTION_STAGE_BARGAIN;

    /**
     * Default Create RPM for {@code originium_power_core}. Used as the
     * {@code reactor.generatedRpm} spec default, the unloaded-spec fallback,
     * and the value Create's {@code BlockStressValues.RPM} display re-reads.
     * Keep this at 32 so existing worlds keep the original generator feel.
     */
    public static final int DEFAULT_REACTOR_GENERATED_RPM = 32;
    /**
     * Default stress capacity (SU at 1 RPM). Same single-source role as
     * {@link #DEFAULT_REACTOR_GENERATED_RPM}; Create's capacity registry
     * already accepts a live {@link java.util.function.DoubleSupplier}.
     */
    public static final double DEFAULT_REACTOR_STRESS_CAPACITY = 256.0;

    // --- Reactor ---
    public static final ModConfigSpec.IntValue REACTOR_CORE_HEAT;
    public static final ModConfigSpec.DoubleValue REACTOR_MOLTEN_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue REACTOR_PUREST_HEAT_CAPACITY;
    public static final ModConfigSpec.DoubleValue REACTOR_COOLING_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_GAIN;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_DECAY;
    public static final ModConfigSpec.DoubleValue REACTOR_INSTABILITY_WARNING;
    public static final ModConfigSpec.DoubleValue REACTOR_MELTDOWN_THRESHOLD;
    public static final ModConfigSpec.IntValue REACTOR_MELTDOWN_DUST_BURST;
    public static final ModConfigSpec.IntValue REACTOR_MELTDOWN_MOLTEN_SOURCES;
    public static final ModConfigSpec.IntValue REACTOR_COOLANT_MIN_FLOW;
    public static final ModConfigSpec.DoubleValue REACTOR_COOLANT_HEAT_PER_MB;
    public static final ModConfigSpec.DoubleValue REACTOR_WATER_HEAT_PER_MB;
    public static final ModConfigSpec.DoubleValue REACTOR_HOT_WATER_HEAT_PER_MB;
    public static final ModConfigSpec.DoubleValue REACTOR_NORMAL_CHAMBER_COOLING;
    public static final ModConfigSpec.DoubleValue REACTOR_SUPER_CHAMBER_COOLING;
    public static final ModConfigSpec.DoubleValue REACTOR_STABILITY_EPSILON;
    public static final ModConfigSpec.IntValue REACTOR_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue REACTOR_FUEL_TICKS_PER_ITEM;
    public static final ModConfigSpec.IntValue REACTOR_GENERATED_RPM;
    public static final ModConfigSpec.DoubleValue REACTOR_STRESS_CAPACITY;
    public static final ModConfigSpec.IntValue REACTOR_UNSTABLE_DUST;
    public static final ModConfigSpec.IntValue SUPER_COOLING_CHAMBER_DURABILITY;

    // --- Multiplayer pollution ---
    public static final ModConfigSpec.EnumValue<PollutionSpreadStrategy> POLLUTION_SPREAD_STRATEGY;
    public static final ModConfigSpec.DoubleValue DEDICATED_SERVER_DIFFUSION_MULTIPLIER;
    public static final ModConfigSpec.IntValue DEDICATED_SERVER_RADIUS_BONUS;
    public static final ModConfigSpec.BooleanValue SYNC_DUST_TO_CLIENTS;
    public static final ModConfigSpec.IntValue DUST_SYNC_INTERVAL;
    public static final ModConfigSpec.IntValue DUST_SYNC_RADIUS;

    // --- Alloy parts (M2 housing / filter upgrade / sealed protection) ---
    public static final ModConfigSpec.DoubleValue CASING_HOUSING_REDUCTION_PER_FACE;
    public static final ModConfigSpec.DoubleValue CORE_HOUSING_REDUCTION_PER_FACE;
    public static final ModConfigSpec.DoubleValue HOUSING_MAX_REDUCTION;
    public static final ModConfigSpec.IntValue ALLOY_SIEVE_DURABILITY;
    public static final ModConfigSpec.DoubleValue ALLOY_PROCESS_SIEVE_EMISSION_CAPTURE;
    public static final ModConfigSpec.DoubleValue ALLOY_FILTER_EMISSION_CAPTURE;
    public static final ModConfigSpec.DoubleValue SEALED_PROTECTION_BONUS;

    // --- Purest line ---
    public static final ModConfigSpec.IntValue COOLING_CHAMBER_DURABILITY;

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
    public static final ModConfigSpec.BooleanValue ENABLE_INDUSTRIAL_SOUNDS;
    public static final ModConfigSpec.DoubleValue SOUND_DENSITY;
    public static final ModConfigSpec.BooleanValue NON_COLOR_ALERTS;

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
                .comment("Whether Create machines produce originium dust when processing originium recipes (mill, crush, mix including heated/superheated). catalyst_mixing is excluded via datapack amount 0.")
                .define("enableDustProduction", true);
        DUST_FROM_MILLING = builder
                .comment("Dust emitted when milling raw originium")
                .defineInRange("dustFromMilling", 80, 0, 1000);
        DUST_FROM_CRUSHING = builder
                .comment("Dust emitted when crushing raw originium")
                .defineInRange("dustFromCrushing", 100, 0, 1000);
        DUST_FROM_SHARD_MIXING = builder
                .comment("Dust emitted when mixing originium shards into originium (heated mixing). Also the heat-aware fallback for unmapped heated originium recipes.")
                .defineInRange("dustFromShardMixing", 120, 0, 1000);
        DUST_FROM_ORIGINIUM_MELTING = builder
                .comment("Dust emitted when melting originium into molten originium (superheated mixing). Also the heat-aware fallback for unmapped superheated originium recipes.")
                .defineInRange("dustFromOriginiumMelting", 200, 0, 1000);
        DUST_FROM_ALLOY_MIXING = builder
                .comment("Dust emitted when mixing molten originium with iron to create alloy (unheated). Also the fallback for unmapped recipes with originium fluid inputs.")
                .defineInRange("dustFromAlloyMixing", 60, 0, 1000);
        DUST_FROM_TAGGED_ITEM = builder
                .comment("Fallback dust when processing an item in create_originium_industry:dust_producing with no datapack item/item_tag amount")
                .defineInRange("dustFromTaggedItem", 40, 0, 1000);

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
        DEATH_EXPOSURE_RETAIN = builder
                .comment("Fraction of exposure kept on respawn (0 = clear). Singleplayer-friendly default wipes short-term exposure.")
                .defineInRange("deathExposureRetain", 0.0, 0.0, 1.0);
        DEATH_INFECTION_RETAIN = builder
                .comment("Fraction of infection kept on respawn (0 = clear, 1 = keep all). Default 0.25 avoids a death spiral without a full reset.")
                .defineInRange("deathInfectionRetain", 0.25, 0.0, 1.0);

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
        FILTER_EMISSION_CAPTURE = builder
                .comment("Fraction of a neighbouring machine's emission an active filter captures (0 = ambient absorb only, 1 = capture all)")
                .defineInRange("filterEmissionCapture", 0.5, 0.0, 1.0);
        FILTER_BYPRODUCT_DUST_PER_ITEM = builder
                .comment("Captured dust units per originium_dust item (0 = no byproduct; remainder is stored on the filter, never duplicated)")
                .defineInRange("filterByproductDustPerItem", 100, 0, 10000);
        PROCESS_SIEVE_EMISSION_CAPTURE = builder
                .comment("Fraction of neighbouring machine emission a placed originium_dust_sieve captures (passive Basin/process attachment, no RPM)")
                .defineInRange("processSieveEmissionCapture", 0.4, 0.0, 1.0);

        builder.pop();

        // ==================== Dust Nozzle ====================
        builder.comment(
                "Encased Fan nozzle (originium_dust_nozzle). Redirects chunk dust downwind;",
                "never voids dust. Additive section."
        ).push("dust_nozzle");

        NOZZLE_TRANSFER_AMOUNT = builder
                .comment("Dust units moved to the downwind chunk per cycle at reference RPM")
                .defineInRange("nozzleTransferAmount", 80, 1, 10000);
        NOZZLE_TRANSFER_INTERVAL = builder
                .comment("Ticks between nozzle redirects (20 = 1 second)")
                .defineInRange("nozzleTransferInterval", 20, 5, 200);
        NOZZLE_MAX_SPEED_MULTIPLIER = builder
                .comment("Maximum transfer multiplier from Encased Fan RPM")
                .defineInRange("nozzleMaxSpeedMultiplier", 4.0, 1.0, 16.0);
        NOZZLE_SPEED_REFERENCE = builder
                .comment("Fan RPM at which the transfer multiplier reaches 1x")
                .defineInRange("nozzleSpeedReference", 64.0, 1.0, 256.0);

        builder.pop();

        // ==================== Dust Meter ====================
        builder.comment(
                "Dust meter block. Reads server chunk dust onto the block entity for goggles",
                "and comparator output. Additive section."
        ).push("dust_meter");

        METER_COMPARATOR_FULL_DUST = builder
                .comment("Chunk dust that maps to comparator signal 15")
                .defineInRange("meterComparatorFullDust", 8000, 1, 100000);
        METER_SYNC_INTERVAL = builder
                .comment("Ticks between meter block-entity snapshots (goggle / comparator)")
                .defineInRange("meterSyncInterval", 10, 1, 200);

        builder.pop();

        // ==================== Worldgen ====================
        builder.comment(
                "Overworld raw originium ore. Uncommon on purpose (dangerous industrial mineral).",
                "Additive section; missing keys use the defaults below and do not rewrite old saves.",
                "Datapack authors can replace worldgen/placed_feature/raw_originium_ore.json or set",
                "neoforge/biome_modifier/add_raw_originium_ore.json to type neoforge:none.",
                "Config knobs apply only while the shipped placed feature (config_count / config_height) is used."
        ).push("worldgen");

        ENABLE_RAW_ORIGINIUM_ORE = builder
                .comment("Master switch for Overworld raw originium ore generation")
                .define("enableRawOriginiumOre", true);
        RAW_ORIGINIUM_VEIN_SIZE = builder
                .comment("Blocks attempted per vein (vanilla diamond small = 4). Keep small for rarity.")
                .defineInRange("veinSize", 4, 1, 16);
        RAW_ORIGINIUM_VEINS_PER_CHUNK = builder
                .comment("Vein attempts per chunk (vanilla diamond small = 7). Default 4 is intentionally scarcer.")
                .defineInRange("veinsPerChunk", 4, 0, 32);
        RAW_ORIGINIUM_MIN_Y = builder
                .comment("Inclusive min Y for the triangle height band (swapped with maxY if inverted)")
                .defineInRange("minY", -64, -64, 320);
        RAW_ORIGINIUM_MAX_Y = builder
                .comment("Inclusive max Y for the triangle height band. Default 16 keeps most veins in deepslate")
                .defineInRange("maxY", 16, -64, 320);
        RAW_ORIGINIUM_DISCARD_CHANCE = builder
                .comment("Chance to skip a vein block exposed to air (0 = cave walls keep ore, 1 = fully buried)")
                .defineInRange("discardChanceOnAirExposure", 0.7, 0.0, 1.0);

        builder.pop();

        // ==================== Protection ====================
        builder.comment(
                "Protection equipment. Tagged items (originium_respirator, originium_filter_canister,",
                "or the originium_protection item tag) reduce incoming exposure/infection.",
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
                .comment("How many tagged protection items count as a full set (respirator + canister = 2)")
                .defineInRange("protectionFullSetPieces", 2, 1, 8);
        PROTECTION_DURABILITY_LOSS = builder
                .comment("Durability lost per exposure check while standing in dusty air (0 = no wear). Broken gear drops originium_dust.")
                .defineInRange("protectionDurabilityLoss", 1, 0, 64);

        builder.pop();

        // ==================== Infection stages ====================
        builder.comment(
                "Long-term infection course. ori_dust_sickness remains the exposure-layer effect;",
                "these stages add vanilla effects from stored infection. Additive section."
        ).push("infection");

        ENABLE_INFECTION_STAGES = builder
                .comment("Master switch for infection-stage effects. Off = infection still stores, no extra stage debuffs.")
                .define("enableInfectionStages", true);
        INFECTION_STAGE_WEAKNESS = builder
                .comment("Infection at which Weakness starts (虚弱)")
                .defineInRange("stageWeakness", 200, 0, 100000);
        INFECTION_STAGE_RESTRICTED = builder
                .comment("Infection at which action is limited (受限): weakness + mining fatigue")
                .defineInRange("stageRestricted", 800, 0, 100000);
        INFECTION_STAGE_GROWTH = builder
                .comment("Infection at which growth/crystallization symptoms start (增生)")
                .defineInRange("stageGrowth", 2500, 0, 100000);
        INFECTION_STAGE_BARGAIN = builder
                .comment("Infection at which cost/benefit starts (代价交换): haste + strength, hunger cost")
                .defineInRange("stageBargain", 6000, 0, 100000);

        builder.pop();

        // ==================== Reactor ====================
        builder.comment(
                "M3 originium power core. Frozen keys keep their names; new keys are additive.",
                "Stability S = C * M - H. enableReactorMeltdown lives under feature_toggles.",
                "Meltdown dumps chunk dust, leaks remaining fuel as world purest molten, and converts tanks to hot water — it does not explode."
        ).push("reactor");

        REACTOR_CORE_HEAT = builder
                .comment("Core heat units produced per tick at nominal load (H base)")
                .defineInRange("coreHeatValue", 1000, 1, 1_000_000);
        REACTOR_MOLTEN_HEAT_CAPACITY = builder
                .comment("Heat-load multiplier when the core is not running on purest fuel")
                .defineInRange("moltenHeatCapacity", 1.0, 0.01, 100.0);
        REACTOR_PUREST_HEAT_CAPACITY = builder
                .comment("Heat-load multiplier for purest originium (hotter fuel)")
                .defineInRange("purestHeatCapacity", 2.5, 0.01, 100.0);
        REACTOR_COOLING_MULTIPLIER = builder
                .comment("Global cooling effectiveness applied to attached chambers (M)")
                .defineInRange("coolingMultiplier", 1.0, 0.0, 10.0);
        REACTOR_INSTABILITY_GAIN = builder
                .comment("Instability gained per tick when S < 0")
                .defineInRange("instabilityGainPerTick", 0.01, 0.0, 10.0);
        REACTOR_INSTABILITY_DECAY = builder
                .comment("Instability lost per tick when S > 0")
                .defineInRange("instabilityDecayPerTick", 0.005, 0.0, 10.0);
        REACTOR_INSTABILITY_WARNING = builder
                .comment("Instability at which goggles / comparator warn")
                .defineInRange("instabilityWarningThreshold", 50.0, 0.0, 10000.0);
        REACTOR_MELTDOWN_THRESHOLD = builder
                .comment("Instability that triggers meltdown when feature_toggles.enableReactorMeltdown is true")
                .defineInRange("meltdownThreshold", 100.0, 1.0, 10000.0);
        REACTOR_MELTDOWN_DUST_BURST = builder
                .comment("Chunk dust released on meltdown (no explosion)")
                .defineInRange("meltdownDustBurst", 5000, 0, 100000);
        REACTOR_MELTDOWN_MOLTEN_SOURCES = builder
                .comment("Max world source blocks of purest_molten_originium leaked on meltdown (remaining fuel remelts into this spray; 0 = no world leak)")
                .defineInRange("meltdownMoltenSources", 6, 0, 64);
        REACTOR_COOLANT_MIN_FLOW = builder
                .comment("Fluid conversion budget (mB/t) along coolant ↔ water ↔ hot water")
                .defineInRange("coolantMinimumFlow", 10, 0, 10000);
        REACTOR_COOLANT_HEAT_PER_MB = builder
                .comment("Heat capacity C contributed per mB of originium coolant")
                .defineInRange("coolantHeatPerMb", 1.0, 0.0, 10.0);
        REACTOR_WATER_HEAT_PER_MB = builder
                .comment("Heat capacity C contributed per mB of water")
                .defineInRange("waterHeatPerMb", 0.625, 0.0, 10.0);
        REACTOR_HOT_WATER_HEAT_PER_MB = builder
                .comment("Heat capacity C contributed per mB of hot water")
                .defineInRange("hotWaterHeatPerMb", 0.2, 0.0, 10.0);
        REACTOR_NORMAL_CHAMBER_COOLING = builder
                .comment("M contribution of one originium_cooling_chamber attached to the core")
                .defineInRange("normalChamberCooling", 1.0, 0.0, 10.0);
        REACTOR_SUPER_CHAMBER_COOLING = builder
                .comment("M contribution of one originium_super_cooling_chamber")
                .defineInRange("superChamberCooling", 2.5, 0.0, 10.0);
        REACTOR_STABILITY_EPSILON = builder
                .comment("Absolute |S| treated as borderline (S ≈ 0)")
                .defineInRange("stabilityEpsilon", 1.0, 0.0, 1000.0);
        REACTOR_TANK_CAPACITY = builder
                .comment("mB capacity of each internal coolant / water / hot-water tank")
                .defineInRange("tankCapacityMb", 4000, 100, 64000);
        REACTOR_FUEL_TICKS_PER_ITEM = builder
                .comment("Ticks one purest originium item keeps the core running")
                .defineInRange("fuelTicksPerItem", 1200, 1, 72000);
        REACTOR_GENERATED_RPM = builder
                .comment(
                        "Create RPM generated while the core is running. Single source for",
                        "PowerCoreBlockEntity.getGeneratedSpeed and Create's BlockStressValues RPM",
                        "display (KineticStats tooltip). Create stores RPM as an int snapshot, not a",
                        "supplier — this mod re-reads the live value on COMMON config load/reload.",
                        "Default " + DEFAULT_REACTOR_GENERATED_RPM + " matches the previous hardcoded generator speed."
                )
                .defineInRange("generatedRpm", DEFAULT_REACTOR_GENERATED_RPM, 1, 256);
        REACTOR_STRESS_CAPACITY = builder
                .comment(
                        "Create stress capacity (SU at 1 RPM) while generating. Live-bound through",
                        "BlockStressValues.CAPACITIES (DoubleSupplier). Default "
                                + (int) DEFAULT_REACTOR_STRESS_CAPACITY + "."
                )
                .defineInRange("stressCapacity", DEFAULT_REACTOR_STRESS_CAPACITY, 0.0, 16384.0);
        REACTOR_UNSTABLE_DUST = builder
                .comment("Chunk dust leaked per second while S < 0 (risk byproduct into the dust loop)")
                .defineInRange("unstableDustPerSecond", 10, 0, 10000);
        SUPER_COOLING_CHAMBER_DURABILITY = builder
                .comment("Basin supercool operations a super cooling chamber survives (reactor cooling does not wear it)")
                .defineInRange("superCoolingChamberDurability", 800, 1, 10000);

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
                .comment("When true, nearby chunk dust is synced to clients (dirty set / on-demand window, not the full map) for meters, HUD, and particles.")
                .define("syncDustToClients", true);
        DUST_SYNC_INTERVAL = builder
                .comment("Ticks between nearby dust / exposure client sync flushes (20 = 1 second). Additive.")
                .defineInRange("dustSyncInterval", 20, 5, 200);
        DUST_SYNC_RADIUS = builder
                .comment("Chebyshev chunk radius synced around each player. Clamped to the dust active-set radius so this never walks farther than simulation.")
                .defineInRange("dustSyncRadius", 8, 1, 16);

        builder.pop();

        // ==================== Purest line ====================
        builder.comment(
                "M2 purest-originium line. Recipes stay Create mixing JSON; sieve / cooling-chamber",
                "gates live in coi_basin_process datapack. Additive section."
        ).push("purest_line");

        COOLING_CHAMBER_DURABILITY = builder
                .comment("Operations a basin cooling-chamber attachment survives before it breaks")
                .defineInRange("coolingChamberDurability", 250, 1, 10000);

        builder.pop();

        // ==================== Alloy parts ====================
        builder.comment(
                "M2 alloy uses: pollution-resistant casings, alloy sieve upgrades, sealed canisters.",
                "Housing reduces neighbouring machine emission before purifiers. Additive section."
        ).push("alloy_parts");

        CASING_HOUSING_REDUCTION_PER_FACE = builder
                .comment("Fraction of process emission removed per adjacent originium alloy casing")
                .defineInRange("casingHousingReductionPerFace", 0.10, 0.0, 1.0);
        CORE_HOUSING_REDUCTION_PER_FACE = builder
                .comment("Fraction of process emission removed per adjacent originium core housing (M3 shell)")
                .defineInRange("coreHousingReductionPerFace", 0.20, 0.0, 1.0);
        HOUSING_MAX_REDUCTION = builder
                .comment("Cap on combined housing emission reduction (0.5 = at most half the leak is sealed)")
                .defineInRange("housingMaxReduction", 0.50, 0.0, 1.0);
        ALLOY_SIEVE_DURABILITY = builder
                .comment("Absorption cycles an originium_alloy_sieve survives (kinetic insert or placed attachment)")
                .defineInRange("alloySieveDurability", 1500, 50, 10000);
        ALLOY_PROCESS_SIEVE_EMISSION_CAPTURE = builder
                .comment("Placed alloy sieve capture fraction (passive Basin/process attachment)")
                .defineInRange("alloyProcessSieveEmissionCapture", 0.70, 0.0, 1.0);
        ALLOY_FILTER_EMISSION_CAPTURE = builder
                .comment("Kinetic filter capture fraction when an alloy sieve is inserted")
                .defineInRange("alloyFilterEmissionCapture", 0.75, 0.0, 1.0);
        SEALED_PROTECTION_BONUS = builder
                .comment("Extra fraction of remaining exposure/infection gain removed when wearing originium_reinforced_protection")
                .defineInRange("sealedProtectionBonus", 0.10, 0.0, 1.0);

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
                .comment("Hold HUD / reactor-alarm indicators steady (no pulse) and slow ambient particle spawn")
                .define("reduceFlicker", false);
        SIMPLIFY_PARTICLES = client
                .comment("Disable Originium Industry ambient particles entirely (overrides particleDensity)")
                .define("simplifyParticles", false);
        PARTICLE_DENSITY = client
                .comment("Ambient dust-fog particle density (sickness or synced chunk dust). 0 = none, 1 = full. Default 0.4 matches the original spawn rate.")
                .defineInRange("particleDensity", 0.4, 0.0, 1.0);
        HIGH_CONTRAST_INDICATORS = client
                .comment("High-contrast sickness HUD, dust-meter goggles, and reactor instability cues (opaque plates, brighter text)")
                .define("highContrastIndicators", false);
        UI_DETAIL_LEVEL = client
                .comment("How much COI UI to show: minimal (hide HUD extras), standard, verbose (goggle numbers)")
                .defineEnum("uiDetailLevel", UiDetailLevel.STANDARD);
        DEBUG_OVERLAY_DETAIL = client
                .comment("Extra debug overlay: off, compact (sickness amplifier), full (VisibleDust chunk + exposure)")
                .defineEnum("debugOverlayDetail", DebugOverlayDetail.OFF);
        SHOW_SICKNESS_HUD = client
                .comment("Show a small HUD hint when the player has Originium Exposure Sickness")
                .define("showSicknessHud", true);
        ENABLE_INDUSTRIAL_SOUNDS = client
                .comment("Play Originium Industry factory SFX (filter work, high-dust ambience, reactor). Additive.")
                .define("enableIndustrialSounds", true);
        SOUND_DENSITY = client
                .comment("Master volume scale for those SFX (0 = mute, 1 = full). Additive; stacks with particleDensity for dust ambience.")
                .defineInRange("soundDensity", 1.0, 0.0, 1.0);
        NON_COLOR_ALERTS = client
                .comment(
                        "Prefix ASCII icons on goggles / HUD for high dust, exposure, and reactor-unstable,",
                        "and reuse high_dust / reactor_alarm (plus their subtitles) as a non-color channel.",
                        "Additive. Off = current color-coded tags only."
                )
                .define("nonColorAlerts", false);

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

    public static int processSieveDurability() {
        return COMMON_SPEC.isLoaded() ? FILTER_SIEVE_DURABILITY.get() : 500;
    }

    public static int alloySieveDurability() {
        return COMMON_SPEC.isLoaded() ? ALLOY_SIEVE_DURABILITY.get() : 1500;
    }

    public static int coolingChamberDurability() {
        return COMMON_SPEC.isLoaded() ? COOLING_CHAMBER_DURABILITY.get() : 250;
    }

    public static int superCoolingChamberDurability() {
        return COMMON_SPEC.isLoaded() ? SUPER_COOLING_CHAMBER_DURABILITY.get() : 800;
    }

    public static double reactorCoreHeat() {
        return COMMON_SPEC.isLoaded() ? REACTOR_CORE_HEAT.get() : 1000;
    }

    public static double reactorMoltenHeatCapacity() {
        return COMMON_SPEC.isLoaded() ? REACTOR_MOLTEN_HEAT_CAPACITY.get() : 1.0;
    }

    public static double reactorPurestHeatCapacity() {
        return COMMON_SPEC.isLoaded() ? REACTOR_PUREST_HEAT_CAPACITY.get() : 2.5;
    }

    public static double reactorCoolingMultiplier() {
        return COMMON_SPEC.isLoaded() ? REACTOR_COOLING_MULTIPLIER.get() : 1.0;
    }

    public static double reactorInstabilityGain() {
        return COMMON_SPEC.isLoaded() ? REACTOR_INSTABILITY_GAIN.get() : 0.01;
    }

    public static double reactorInstabilityDecay() {
        return COMMON_SPEC.isLoaded() ? REACTOR_INSTABILITY_DECAY.get() : 0.005;
    }

    public static double reactorInstabilityWarning() {
        return COMMON_SPEC.isLoaded() ? REACTOR_INSTABILITY_WARNING.get() : 50.0;
    }

    public static double reactorMeltdownThreshold() {
        return COMMON_SPEC.isLoaded() ? REACTOR_MELTDOWN_THRESHOLD.get() : 100.0;
    }

    public static int reactorMeltdownDustBurst() {
        return COMMON_SPEC.isLoaded() ? REACTOR_MELTDOWN_DUST_BURST.get() : 5000;
    }

    public static int reactorMeltdownMoltenSources() {
        return COMMON_SPEC.isLoaded() ? REACTOR_MELTDOWN_MOLTEN_SOURCES.get() : 6;
    }

    public static int reactorCoolantMinimumFlow() {
        return COMMON_SPEC.isLoaded() ? REACTOR_COOLANT_MIN_FLOW.get() : 10;
    }

    public static double reactorCoolantHeatPerMb() {
        return COMMON_SPEC.isLoaded() ? REACTOR_COOLANT_HEAT_PER_MB.get() : 1.0;
    }

    public static double reactorWaterHeatPerMb() {
        return COMMON_SPEC.isLoaded() ? REACTOR_WATER_HEAT_PER_MB.get() : 0.625;
    }

    public static double reactorHotWaterHeatPerMb() {
        return COMMON_SPEC.isLoaded() ? REACTOR_HOT_WATER_HEAT_PER_MB.get() : 0.2;
    }

    public static double reactorNormalChamberCooling() {
        return COMMON_SPEC.isLoaded() ? REACTOR_NORMAL_CHAMBER_COOLING.get() : 1.0;
    }

    public static double reactorSuperChamberCooling() {
        return COMMON_SPEC.isLoaded() ? REACTOR_SUPER_CHAMBER_COOLING.get() : 2.5;
    }

    public static double reactorStabilityEpsilon() {
        return COMMON_SPEC.isLoaded() ? REACTOR_STABILITY_EPSILON.get() : 1.0;
    }

    public static int reactorTankCapacity() {
        return COMMON_SPEC.isLoaded() ? REACTOR_TANK_CAPACITY.get() : 4000;
    }

    public static int reactorFuelTicksPerItem() {
        return COMMON_SPEC.isLoaded() ? REACTOR_FUEL_TICKS_PER_ITEM.get() : 1200;
    }

    /**
     * Integer RPM used by Create's {@code BlockStressValues.GeneratedRpm}
     * snapshot and by {@link #reactorGeneratedRpm()}.
     */
    public static int reactorGeneratedRpmInt() {
        return COMMON_SPEC.isLoaded() ? REACTOR_GENERATED_RPM.get() : DEFAULT_REACTOR_GENERATED_RPM;
    }

    public static float reactorGeneratedRpm() {
        return reactorGeneratedRpmInt();
    }

    public static double reactorStressCapacity() {
        return COMMON_SPEC.isLoaded() ? REACTOR_STRESS_CAPACITY.get() : DEFAULT_REACTOR_STRESS_CAPACITY;
    }

    public static int reactorUnstableDustPerSecond() {
        return COMMON_SPEC.isLoaded() ? REACTOR_UNSTABLE_DUST.get() : 10;
    }

    public static boolean reactorMeltdownEnabled() {
        return !COMMON_SPEC.isLoaded() || ENABLE_REACTOR_MELTDOWN.get();
    }

    public static int meterSyncInterval() {
        return COMMON_SPEC.isLoaded() ? METER_SYNC_INTERVAL.get() : 10;
    }

    public static boolean rawOriginiumOreEnabled() {
        return !COMMON_SPEC.isLoaded() || ENABLE_RAW_ORIGINIUM_ORE.get();
    }

    public static int rawOriginiumVeinSize() {
        return COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_VEIN_SIZE.get() : 4;
    }

    public static int rawOriginiumVeinsPerChunk() {
        return COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_VEINS_PER_CHUNK.get() : 4;
    }

    public static int rawOriginiumMinY() {
        int min = COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_MIN_Y.get() : -64;
        int max = COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_MAX_Y.get() : 16;
        return Math.min(min, max);
    }

    public static int rawOriginiumMaxY() {
        int min = COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_MIN_Y.get() : -64;
        int max = COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_MAX_Y.get() : 16;
        int hi = Math.max(min, max);
        int lo = Math.min(min, max);
        return hi == lo ? lo + 1 : hi;
    }

    public static float rawOriginiumDiscardChance() {
        return COMMON_SPEC.isLoaded() ? RAW_ORIGINIUM_DISCARD_CHANCE.get().floatValue() : 0.7f;
    }

    /**
     * Nearby chunk-dust packets. Default on once #17 landed. Missing spec
     * (early boot) matches the shipped default.
     */
    public static boolean syncDustToClients() {
        return !COMMON_SPEC.isLoaded() || SYNC_DUST_TO_CLIENTS.get();
    }

    public static int dustSyncInterval() {
        return COMMON_SPEC.isLoaded() ? DUST_SYNC_INTERVAL.get() : 20;
    }

    /**
     * Sync window never exceeds the diffusion active-set radius.
     */
    public static int dustSyncRadius(boolean dedicatedServer) {
        int configured = COMMON_SPEC.isLoaded() ? DUST_SYNC_RADIUS.get() : 8;
        if (!COMMON_SPEC.isLoaded()) {
            return configured;
        }
        int active = effectiveInitChunkRadius(dedicatedServer);
        return Math.max(1, Math.min(configured, active));
    }
}
