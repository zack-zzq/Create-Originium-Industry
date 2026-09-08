package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import com.mealuet.create_originium_industry.index.COIWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/**
 * Coverage for issue #22: Overworld raw originium ore, loot, and config-driven worldgen.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class WorldGenGameTests {

    private WorldGenGameTests() {}

    @GameTest(template = "empty", batch = "worldgen")
    public static void worldgenConfigDefaultsAreUncommon(GameTestHelper helper) {
        helper.assertTrue(COIConfig.rawOriginiumOreEnabled(), "ore gen enabled");
        helper.assertValueEqual(COIConfig.rawOriginiumVeinSize(), 4, "veinSize");
        helper.assertValueEqual(COIConfig.rawOriginiumVeinsPerChunk(), 4, "veinsPerChunk");
        helper.assertValueEqual(COIConfig.rawOriginiumMinY(), -64, "minY");
        helper.assertValueEqual(COIConfig.rawOriginiumMaxY(), 16, "maxY");
        helper.assertValueEqual(COIConfig.RAW_ORIGINIUM_DISCARD_CHANCE.get(), 0.7, "discardChance");
        helper.assertTrue(COIConfig.rawOriginiumVeinsPerChunk() < 7, "scarcer than diamond small (7)");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void invertedHeightBandDoesNotCrash(GameTestHelper helper) {
        helper.assertTrue(COIConfig.rawOriginiumMinY() <= COIConfig.rawOriginiumMaxY(), "min <= max");
        helper.assertTrue(COIConfig.rawOriginiumMaxY() > COIConfig.rawOriginiumMinY(), "non-empty band");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void oreBlocksKeepRawOriginiumItemId(GameTestHelper helper) {
        helper.assertTrue(COIItems.RAW_ORIGINIUM.get() != Items.AIR, "raw_originium item");
        helper.assertTrue(COIBlocks.RAW_ORIGINIUM_ORE.get() != Blocks.AIR, "stone ore");
        helper.assertTrue(COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.get() != Blocks.AIR, "deepslate ore");
        helper.assertTrue(
                COIBlocks.RAW_ORIGINIUM_ORE.asItem() != COIItems.RAW_ORIGINIUM.get(),
                "ore block is a new id, not a rename of raw_originium"
        );
        helper.assertTrue(
                COIBlocks.RAW_ORIGINIUM_ORE.getId().getPath().equals("raw_originium_ore"),
                "stone ore path"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void oreIsMineableWithIronPickaxe(GameTestHelper helper) {
        BlockState stoneOre = COIBlocks.RAW_ORIGINIUM_ORE.getDefaultState();
        BlockState deepOre = COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.getDefaultState();
        helper.assertTrue(stoneOre.is(BlockTags.MINEABLE_WITH_PICKAXE), "stone mineable/pickaxe");
        helper.assertTrue(deepOre.is(BlockTags.MINEABLE_WITH_PICKAXE), "deepslate mineable/pickaxe");
        helper.assertTrue(stoneOre.is(BlockTags.NEEDS_IRON_TOOL), "stone needs iron");
        helper.assertTrue(deepOre.is(BlockTags.NEEDS_IRON_TOOL), "deepslate needs iron");
        helper.assertTrue(stoneOre.is(COITags.Blocks.RAW_ORIGINIUM_ORES), "mod ore tag");
        helper.assertTrue(stoneOre.is(Tags.Blocks.ORES), "c:ores");
        helper.assertTrue(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(stoneOre), "iron harvests");
        helper.assertFalse(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(stoneOre), "stone cannot harvest");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void miningDropsRawOriginium(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_PICKAXE);
        List<ItemStack> stoneDrops = Block.getDrops(
                COIBlocks.RAW_ORIGINIUM_ORE.getDefaultState(),
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                null,
                null,
                iron
        );
        List<ItemStack> deepDrops = Block.getDrops(
                COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.getDefaultState(),
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                null,
                null,
                iron
        );
        helper.assertValueEqual(count(stoneDrops, COIItems.RAW_ORIGINIUM.get()), 1, "stone ore -> raw");
        helper.assertValueEqual(count(deepDrops, COIItems.RAW_ORIGINIUM.get()), 1, "deepslate ore -> raw");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void silkTouchKeepsOreBlock(GameTestHelper helper) {
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH), 1);
        List<ItemStack> drops = Block.getDrops(
                COIBlocks.RAW_ORIGINIUM_ORE.getDefaultState(),
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                null,
                null,
                silk
        );
        helper.assertValueEqual(count(drops, COIBlocks.RAW_ORIGINIUM_ORE.asItem()), 1, "silk -> ore block");
        helper.assertValueEqual(count(drops, COIItems.RAW_ORIGINIUM.get()), 0, "silk does not drop raw");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void placedFeatureAndBiomeModifierAreLoaded(GameTestHelper helper) {
        Optional<Holder.Reference<PlacedFeature>> placed = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.PLACED_FEATURE)
                .get(COIWorldGen.PLACED_RAW_ORIGINIUM_ORE);
        helper.assertTrue(placed.isPresent(), "placed feature registered");

        helper.assertTrue(
                helper.getLevel().registryAccess()
                        .lookupOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                        .get(COIWorldGen.BIOME_MODIFIER)
                        .isPresent(),
                "biome modifier registered"
        );

        Holder<Biome> plains = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(Biomes.PLAINS);
        helper.assertTrue(
                plains.value().getGenerationSettings().hasFeature(placed.get().value()),
                "plains generates raw originium ore"
        );

        Holder<Biome> nether = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(Biomes.NETHER_WASTES);
        helper.assertFalse(
                nether.value().getGenerationSettings().hasFeature(placed.get().value()),
                "nether does not generate overworld originium"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void oreTargetsReplaceStoneAndDeepslate(GameTestHelper helper) {
        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        helper.assertTrue(stone.test(Blocks.STONE.defaultBlockState(), helper.getLevel().getRandom()), "stone replaceable");
        helper.assertTrue(deepslate.test(Blocks.DEEPSLATE.defaultBlockState(), helper.getLevel().getRandom()), "deepslate replaceable");
        helper.assertFalse(stone.test(Blocks.DEEPSLATE.defaultBlockState(), helper.getLevel().getRandom()), "deepslate is not stone ore");

        OreConfiguration config = COIWorldGen.oreConfiguration();
        helper.assertValueEqual(config.size, 4, "config size");
        helper.assertValueEqual(config.targetStates.size(), 2, "stone + deepslate targets");
        helper.assertTrue(
                config.targetStates.get(0).state.is(COIBlocks.RAW_ORIGINIUM_ORE.get()),
                "stone target is raw_originium_ore"
        );
        helper.assertTrue(
                config.targetStates.get(1).state.is(COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.get()),
                "deepslate target is deepslate_raw_originium_ore"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "worldgen")
    public static void oreTargetsConvertStoneAndDeepslate(GameTestHelper helper) {
        BlockPos stonePos = new BlockPos(1, 1, 1);
        BlockPos deepPos = new BlockPos(2, 1, 1);
        helper.setBlock(stonePos, Blocks.STONE);
        helper.setBlock(deepPos, Blocks.DEEPSLATE);

        OreConfiguration config = COIWorldGen.testOreConfiguration();
        applyFirstMatchingTarget(helper, stonePos, config);
        applyFirstMatchingTarget(helper, deepPos, config);

        helper.assertTrue(
                helper.getBlockState(stonePos).is(COIBlocks.RAW_ORIGINIUM_ORE.get()),
                "stone replaceable -> raw_originium_ore"
        );
        helper.assertTrue(
                helper.getBlockState(deepPos).is(COIBlocks.DEEPSLATE_RAW_ORIGINIUM_ORE.get()),
                "deepslate replaceable -> deepslate_raw_originium_ore"
        );
        helper.succeed();
    }

    private static void applyFirstMatchingTarget(GameTestHelper helper, BlockPos pos, OreConfiguration config) {
        BlockState current = helper.getBlockState(pos);
        for (OreConfiguration.TargetBlockState target : config.targetStates) {
            if (target.target.test(current, helper.getLevel().getRandom())) {
                helper.setBlock(pos, target.state);
                return;
            }
        }
    }

    private static int count(List<ItemStack> drops, net.minecraft.world.item.Item item) {
        int total = 0;
        for (ItemStack stack : drops) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
