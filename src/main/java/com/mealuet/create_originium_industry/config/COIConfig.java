package com.mealuet.create_originium_industry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class COIConfig {

    public static final ModConfigSpec COMMON_SPEC;

    // --- Dust Diffusion ---
    public static final ModConfigSpec.IntValue MAX_DUST_LEVEL;
    public static final ModConfigSpec.IntValue DIFFUSION_INTERVAL;
    public static final ModConfigSpec.IntValue INIT_CHUNK_RADIUS;
    public static final ModConfigSpec.DoubleValue DIFFUSION_RATE;

    // --- Player Exposure ---
    public static final ModConfigSpec.IntValue EFFECT_CHECK_INTERVAL;
    public static final ModConfigSpec.IntValue DUST_EFFECT_THRESHOLD;
    public static final ModConfigSpec.IntValue DUST_PER_EFFECT_LEVEL;
    public static final ModConfigSpec.IntValue MAX_EFFECT_AMPLIFIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Dust diffusion settings").push("dust_diffusion");
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
        builder.pop();

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
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private COIConfig() {}
}
