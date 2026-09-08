package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.worldgen.RawOriginiumCountPlacement;
import com.mealuet.create_originium_industry.worldgen.RawOriginiumHeightPlacement;
import com.mealuet.create_originium_industry.worldgen.RawOriginiumOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Overworld raw originium ore worldgen. Feature type and placement modifiers
 * read {@link COIConfig} at generation time; datapack JSON wires them into
 * biomes ({@code neoforge:add_features} + {@code #minecraft:is_overworld}).
 */
public final class COIWorldGen {

    public static final String FEATURE_PATH = "raw_originium_ore";

    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_RAW_ORIGINIUM_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, FEATURE_PATH)
    );

    public static final ResourceKey<PlacedFeature> PLACED_RAW_ORIGINIUM_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, FEATURE_PATH)
    );

    public static final ResourceLocation BIOME_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            CreateOriginiumIndustry.MODID, "add_raw_originium_ore");

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, CreateOriginiumIndustry.MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, CreateOriginiumIndustry.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> RAW_ORIGINIUM_ORE =
            FEATURES.register(FEATURE_PATH, RawOriginiumOreFeature::new);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<RawOriginiumCountPlacement>> RAW_ORIGINIUM_COUNT =
            PLACEMENT_MODIFIERS.register("raw_originium_count", () -> () -> RawOriginiumCountPlacement.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<RawOriginiumHeightPlacement>> RAW_ORIGINIUM_HEIGHT =
            PLACEMENT_MODIFIERS.register("raw_originium_height", () -> () -> RawOriginiumHeightPlacement.CODEC);

    private COIWorldGen() {}

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
        PLACEMENT_MODIFIERS.register(modEventBus);
    }

    public static List<OreConfiguration.TargetBlockState> oreTargets() {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        return List.of(
                OreConfiguration.target(stone, COIBlocks.RAW_ORIGINIUM_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.get().defaultBlockState())
        );
    }

    public static OreConfiguration oreConfiguration() {
        return new OreConfiguration(
                oreTargets(),
                COIConfig.rawOriginiumVeinSize(),
                COIConfig.rawOriginiumDiscardChance()
        );
    }

    /** GameTest helper: large vein, never discarded, so a stone cube is guaranteed to convert. */
    public static OreConfiguration testOreConfiguration() {
        return new OreConfiguration(oreTargets(), 16, 0.0f);
    }
}
