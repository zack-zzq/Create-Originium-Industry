package com.mealuet.create_originium_industry.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Coverage for issue #42: every COI world fluid has a {@code LiquidBlock},
 * {@code level=0..15} blockstates, still-particle models, and atlas sprites.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class FluidAssetsGameTests {

    private FluidAssetsGameTests() {}

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void allFluidsRegisterLiquidBlocks(GameTestHelper helper) {
        helper.assertValueEqual(COIFluids.ALL.size(), 7, "seven world fluids");
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            String id = blockId(fluid);
            Optional<Block> block = fluid.getBlock();
            helper.assertTrue(block.isPresent(), id + " LiquidBlock");
            helper.assertTrue(block.get() instanceof LiquidBlock, id + " is LiquidBlock");
            helper.assertTrue(
                    BuiltInRegistries.BLOCK.containsKey(id(id)),
                    "block registry " + id
            );
            helper.assertTrue(
                    block.get().defaultBlockState().hasProperty(LiquidBlock.LEVEL),
                    id + " has level"
            );
            helper.assertValueEqual(
                    LiquidBlock.LEVEL.getPossibleValues().size(),
                    16,
                    id + " level count"
            );
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void fluidBlocksPlaceEveryLevel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            LiquidBlock block = (LiquidBlock) fluid.getBlock().orElseThrow();
            String id = blockId(fluid);
            for (int level = 0; level <= 15; level++) {
                BlockState state = block.defaultBlockState().setValue(LiquidBlock.LEVEL, level);
                helper.setBlock(pos, state);
                BlockState placed = helper.getBlockState(pos);
                helper.assertTrue(placed.getBlock() == block, id + " placed level " + level);
                helper.assertValueEqual(placed.getValue(LiquidBlock.LEVEL), level, id + " level " + level);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void blockstatesCoverLevelZeroToFifteen(GameTestHelper helper) {
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            String id = blockId(fluid);
            JsonObject root = readJson("/assets/create_originium_industry/blockstates/" + id + ".json");
            JsonObject variants = root.getAsJsonObject("variants");
            helper.assertTrue(variants != null, id + " variants");
            for (int level = 0; level <= 15; level++) {
                String key = "level=" + level;
                helper.assertTrue(variants.has(key), id + " missing " + key);
                helper.assertValueEqual(
                        variants.getAsJsonObject(key).get("model").getAsString(),
                        CreateOriginiumIndustry.MODID + ":block/" + id,
                        id + " " + key + " model"
                );
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void blockModelsAndTexturesExist(GameTestHelper helper) {
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            String id = blockId(fluid);
            JsonObject model = readJson("/assets/create_originium_industry/models/block/" + id + ".json");
            helper.assertValueEqual(
                    model.getAsJsonObject("textures").get("particle").getAsString(),
                    CreateOriginiumIndustry.MODID + ":fluid/" + id + "_still",
                    id + " particle"
            );
            assertPng(helper, "/assets/create_originium_industry/textures/fluid/" + id + "_still.png");
            assertPng(helper, "/assets/create_originium_industry/textures/fluid/" + id + "_flow.png");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void blocksAtlasListsEveryFluidSprite(GameTestHelper helper) {
        JsonObject atlas = readJson("/assets/minecraft/atlases/blocks.json");
        Set<String> resources = new HashSet<>();
        for (JsonElement source : atlas.getAsJsonArray("sources")) {
            resources.add(source.getAsJsonObject().get("resource").getAsString());
        }
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            String id = blockId(fluid);
            helper.assertTrue(
                    resources.contains(CreateOriginiumIndustry.MODID + ":fluid/" + id + "_still"),
                    "atlas still " + id
            );
            helper.assertTrue(
                    resources.contains(CreateOriginiumIndustry.MODID + ":fluid/" + id + "_flow"),
                    "atlas flow " + id
            );
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_assets")
    public static void fluidBlockLangKeysExist(GameTestHelper helper) {
        Language language = Language.getInstance();
        for (FluidEntry<BaseFlowingFluid.Flowing> fluid : COIFluids.ALL) {
            String id = blockId(fluid);
            String key = "block." + CreateOriginiumIndustry.MODID + "." + id;
            helper.assertTrue(language.has(key), "en_us loaded " + key);
            helper.assertFalse(language.getOrDefault(key).equals(key), "en_us translates " + key);
        }
        helper.succeed();
    }

    private static String blockId(FluidEntry<BaseFlowingFluid.Flowing> fluid) {
        Block block = fluid.getBlock().orElseThrow();
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private static void assertPng(GameTestHelper helper, String resource) {
        try (InputStream in = FluidAssetsGameTests.class.getResourceAsStream(resource)) {
            helper.assertTrue(in != null, "missing " + resource);
            helper.assertTrue(in != null && in.read() == 0x89, resource + " png");
        } catch (IOException e) {
            throw new IllegalStateException(resource, e);
        }
    }

    private static JsonObject readJson(String resource) {
        try (InputStream in = FluidAssetsGameTests.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing " + resource);
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(resource, e);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }
}
