package com.mealuet.create_originium_industry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class COIConfig {

    public static final ModConfigSpec COMMON_SPEC;

    // --- Dust Diffusion ---
    public static final ModConfigSpec.BooleanValue ENABLE_DUST_DIFFUSION;
    public static final ModConfigSpec.IntValue MAX_DUST_LEVEL;
    public static final ModConfigSpec.IntValue DIFFUSION_INTERVAL;
    public static final ModConfigSpec.IntValue INIT_CHUNK_RADIUS;
    public static final ModConfigSpec.DoubleValue DIFFUSION_RATE;
    public static final ModConfigSpec.IntValue DUST_DECAY_RATE;

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

    // --- Feature Toggles ---
    public static final ModConfigSpec.BooleanValue ENABLE_REACTOR_MELTDOWN;
    public static final ModConfigSpec.BooleanValue ENABLE_DEATH_DUST_BURST;

    // --- Dust Filter ---
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_RATE;
    public static final ModConfigSpec.IntValue FILTER_ABSORPTION_INTERVAL;
    public static final ModConfigSpec.IntValue FILTER_SIEVE_DURABILITY;

    // --- Debug ---
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_COMMANDS;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING;

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
                .comment("Radius around players for initial cache population on server start")
                .defineInRange("initChunkRadius", 8, 1, 32);
        DIFFUSION_RATE = builder
                .comment("Diffusion speed multiplier (higher = faster spread)")
                .defineInRange("diffusionRate", 1.0, 0.1, 10.0);
        DUST_DECAY_RATE = builder
                .comment("Amount of dust that naturally decays per diffusion tick (0 = no decay)")
                .defineInRange("dustDecayRate", 1, 0, 100);

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
                .comment("Multiplier for player exposure gain rate")
                .defineInRange("exposureGainMultiplier", 1.0, 0.0, 10.0);
        INFECTION_GAIN_MULTIPLIER = builder
                .comment("Multiplier for player infection gain rate")
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

        builder.pop();

        // ==================== Feature Toggles ====================
        builder.comment("Feature toggle settings").push("feature_toggles");

        ENABLE_REACTOR_MELTDOWN = builder
                .comment("Whether reactor meltdown is enabled (Phase 6)")
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
    }

    private COIConfig() {}
}
