package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.CoolingChamberBlock;
import com.mealuet.create_originium_industry.block.CoolingChamberBlockEntity;
import com.mealuet.create_originium_industry.block.PowerCoreBlockEntity;
import com.mealuet.create_originium_industry.compat.WorldSpace;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.DustReason;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.ProcessAttachments;
import com.mealuet.create_originium_industry.core.purest.MeltdownPolicy;
import com.mealuet.create_originium_industry.core.reactor.CoolingKind;
import com.mealuet.create_originium_industry.core.reactor.ReactorFluids;
import com.mealuet.create_originium_industry.core.reactor.StabilityMath;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #18: power core, snow-golem cooling chambers,
 * H/C/M/S stability, coolant↔water↔hot water, dust dump, molten leak, and
 * non-explosive meltdown.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class ReactorGameTests {

    private ReactorGameTests() {}

    @GameTest(template = "empty", batch = "reactor")
    public static void stabilityPositiveWithCoolantAndChamber(GameTestHelper helper) {
        var snap = StabilityMath.compute(true, true, 4000, 0, 0, 1.0);
        helper.assertValueEqual(snap.heat(), 2500.0, "H = 1000 * 2.5");
        helper.assertValueEqual(snap.capacity(), 4000.0, "C = 4000 coolant");
        helper.assertValueEqual(snap.cooling(), 1.0, "M = 1 chamber");
        helper.assertValueEqual(snap.stability(), 1500.0, "S = 1500");
        helper.assertValueEqual(snap.sign(), StabilityMath.Sign.POSITIVE, "S>0");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void stabilityBorderlineAtMatchingCapacity(GameTestHelper helper) {
        var snap = StabilityMath.compute(true, true, 2500, 0, 0, 1.0);
        helper.assertValueEqual(snap.stability(), 0.0, "S = 0");
        helper.assertValueEqual(snap.sign(), StabilityMath.Sign.ZERO, "S≈0");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void stabilityNegativeWithoutCooling(GameTestHelper helper) {
        var snap = StabilityMath.compute(true, true, 4000, 0, 0, 0.0);
        helper.assertValueEqual(snap.cooling(), 0.0, "M = 0");
        helper.assertValueEqual(snap.stability(), -2500.0, "S = -H");
        helper.assertValueEqual(snap.sign(), StabilityMath.Sign.NEGATIVE, "S<0");

        var hot = StabilityMath.compute(true, true, 0, 0, 4000, 1.0);
        helper.assertTrue(hot.stability() < 0.0, "hot water cannot hold S>=0 with one chamber");
        helper.assertValueEqual(hot.sign(), StabilityMath.Sign.NEGATIVE, "hot water S<0");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void fluidsConvertTowardCoolantWhenStable(GameTestHelper helper) {
        var start = new ReactorFluids.Amounts(3900, 100, 0);
        var next = ReactorFluids.convert(start, 1.0, 10, 4000);
        helper.assertValueEqual(next.coolant(), 3910, "water → coolant");
        helper.assertValueEqual(next.water(), 90, "water drained");
        helper.assertValueEqual(next.hotWater(), 0, "no hot water");
        helper.assertValueEqual(next.total(), start.total(), "no void");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void fluidsConvertTowardHotWhenUnstable(GameTestHelper helper) {
        var start = new ReactorFluids.Amounts(4000, 0, 0);
        var next = ReactorFluids.convert(start, -1.0, 10, 4000);
        helper.assertValueEqual(next.coolant(), 3990, "coolant → water");
        helper.assertValueEqual(next.water(), 10, "water produced");
        helper.assertValueEqual(next.hotWater(), 0, "one step only");
        helper.assertValueEqual(next.total(), start.total(), "no void");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void powerCoreStableTickKeepsPositiveS(GameTestHelper helper) {
        PowerCoreBlockEntity core = placeCore(helper, true);
        core.configureForGameTest(1, 4000, 0, 0, 0.0);
        core.tickReactor(helper.getLevel());
        helper.assertValueEqual(core.snapshot().sign(), StabilityMath.Sign.POSITIVE, "wired S>0");
        helper.assertTrue(core.isRunning(), "running with housing + fuel + chamber");
        helper.assertFalse(core.shutdown(), "not shut down");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void powerCoreUnstableTickConvertsCoolant(GameTestHelper helper) {
        PowerCoreBlockEntity core = placeCore(helper, false);
        core.configureForGameTest(1, 4000, 0, 0, 0.0);
        helper.assertTrue(core.isRunning(), "housing+fuel is enough to run");
        core.tickReactor(helper.getLevel());
        helper.assertValueEqual(core.snapshot().sign(), StabilityMath.Sign.NEGATIVE, "wired S<0");
        helper.assertValueEqual(core.coolantTank().getFluidAmount(), 3990, "coolant stepped toward water");
        helper.assertValueEqual(core.waterTank().getFluidAmount(), 10, "water appeared");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void dumpHeatConvertsTanksWithoutVoidBeyondCap(GameTestHelper helper) {
        var dumped = ReactorFluids.dumpHeat(new ReactorFluids.Amounts(1000, 500, 0), 4000);
        helper.assertValueEqual(dumped.coolant(), 0, "coolant gone");
        helper.assertValueEqual(dumped.water(), 0, "water gone");
        helper.assertValueEqual(dumped.hotWater(), 1500, "all heat in hot water");
        helper.assertValueEqual(dumped.total(), 1500, "no void under cap");

        var capped = ReactorFluids.dumpHeat(new ReactorFluids.Amounts(3000, 2000, 0), 4000);
        helper.assertValueEqual(capped.hotWater(), 4000, "cap, not TNT");
        helper.assertValueEqual(capped.coolant(), 0, "capped coolant empty");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void meltdownDumpsDustWithoutExplosion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        PowerCoreBlockEntity core = placeCore(helper, false);
        int fuel = 2;
        core.configureForGameTest(fuel, 1000, 500, 0, COIConfig.reactorMeltdownThreshold());
        BlockPos coreRel = new BlockPos(2, 1, 1);
        BlockPos abs = helper.absolutePos(coreRel);
        ChunkPos chunk = WorldSpace.toDustChunk(level, abs);
        OriginiumDustManager.clearDust(level, chunk, DustReason.DEBUG);

        helper.assertFalse(MeltdownPolicy.explodesBlocks(), "policy: no block explosion");
        helper.assertFalse(MeltdownPolicy.spawnsTnt(), "policy: no TNT");
        helper.assertValueEqual(MeltdownPolicy.moltenSourceCount(fuel), 2, "two remaining fuel → two sources");

        core.tickReactor(level);

        helper.assertTrue(core.shutdown(), "meltdown shuts down");
        helper.assertValueEqual(core.fuelCount(), 0, "purest fuel consumed");
        helper.assertTrue(helper.getBlockState(coreRel).is(COIBlocks.POWER_CORE.get()), "core survives");
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 1, 1)).is(COIBlocks.CORE_HOUSING.get()), "housing survives");
        // Meltdown dump is the dust event for this tick; unstable leak is skipped.
        helper.assertValueEqual(OriginiumDustManager.getDust(level, chunk), 5000, "dust pressure");
        helper.assertValueEqual(core.coolantTank().getFluidAmount(), 0, "coolant dumped to heat");
        helper.assertValueEqual(core.waterTank().getFluidAmount(), 0, "water dumped to heat");
        helper.assertTrue(core.hotWaterTank().getFluidAmount() > 0, "hot water remains");
        helper.assertValueEqual(countPurestMolten(helper, coreRel, 3), 2, "remaining fuel leaked as molten");
        helper.assertTrue(hasPurestMoltenSource(helper, coreRel.above()), "leak prefers the open face above");
        helper.assertTrue(
                level.getEntitiesOfClass(PrimedTnt.class, new AABB(abs).inflate(8)).isEmpty(),
                "no primed TNT"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void meltdownPolicyLeaksMoltenWithoutExplosion(GameTestHelper helper) {
        BlockPos originRel = new BlockPos(2, 1, 1);
        helper.setBlock(originRel, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(originRel);

        helper.assertFalse(MeltdownPolicy.explodesBlocks(), "policy: no block explosion");
        helper.assertFalse(MeltdownPolicy.spawnsTnt(), "policy: no TNT");
        helper.assertValueEqual(MeltdownPolicy.moltenSourceCount(3), 3, "three sources requested");

        int placed = MeltdownPolicy.leakMolten(level, origin, 3);
        helper.assertValueEqual(placed, 3, "three replaceable neighbours filled");
        helper.assertTrue(helper.getBlockState(originRel).is(Blocks.STONE), "origin block not replaced");
        helper.assertValueEqual(countPurestMolten(helper, originRel, 2), 3, "world leak is purest molten");
        helper.assertTrue(hasPurestMoltenSource(helper, originRel.above()), "first source goes up");
        helper.assertTrue(
                level.getEntitiesOfClass(PrimedTnt.class, new AABB(origin).inflate(8)).isEmpty(),
                "leak does not spawn TNT"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void housingAndRecipesAndSuperChamber(GameTestHelper helper) {
        helper.assertTrue(recipePresent(helper, "crafting/originium_power_core"), "power core craft");
        helper.assertTrue(recipePresent(helper, "crafting/originium_super_cooling_chamber"), "super chamber craft");
        helper.assertTrue(recipePresent(helper, "mixing/originium_coolant"), "coolant mix");

        var coreRecipe = recipe(helper, "crafting/originium_power_core");
        helper.assertTrue(
                coreRecipe.getIngredients().stream().anyMatch(ing -> ing.test(COIItems.PUREST_ORIGINIUM.asStack())),
                "core consumes purest"
        );
        helper.assertTrue(
                coreRecipe.getIngredients().stream().anyMatch(ing -> ing.test(COIBlocks.CORE_HOUSING.asStack())),
                "core consumes housing"
        );
        var superRecipe = recipe(helper, "crafting/originium_super_cooling_chamber");
        helper.assertTrue(
                superRecipe.getIngredients().stream().anyMatch(ing -> ing.test(COIBlocks.COOLING_CHAMBER.asStack())),
                "super upgrades the basin chamber"
        );
        helper.assertTrue(
                superRecipe.getIngredients().stream().anyMatch(ing -> ing.test(Items.CARVED_PUMPKIN.getDefaultInstance())),
                "snow-golem pumpkin"
        );

        helper.assertValueEqual(CoolingKind.SUPER.coolingFactor(), 2.5, "super M");
        helper.assertValueEqual(CoolingKind.NORMAL.coolingFactor(), 1.0, "normal M");
        helper.assertTrue(
                COIBlocks.COOLING_CHAMBER.getDefaultState().is(COITags.Blocks.REACTOR_COOLING),
                "normal chamber tagged reactor_cooling"
        );
        helper.assertTrue(
                COIBlocks.SUPER_COOLING_CHAMBER.getDefaultState().is(COITags.Blocks.REACTOR_COOLING),
                "super chamber tagged reactor_cooling"
        );
        helper.assertTrue(
                COIBlocks.CORE_HOUSING.getDefaultState().is(COITags.Blocks.REACTOR_HOUSING),
                "housing still tagged"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor_rpm")
    public static void generatedSpeedFollowsReactorConfig(GameTestHelper helper) {
        PowerCoreBlockEntity core = placeCore(helper, true);
        helper.assertValueEqual(core.getGeneratedSpeed(), 0f, "idle core generates 0");
        core.configureForGameTest(1, 4000, 0, 0, 0.0);
        helper.assertTrue(core.isGenerating(), "housing + fuel + chamber generates");
        helper.assertValueEqual(core.getGeneratedSpeed(), COIConfig.reactorGeneratedRpm(), "runtime uses config");
        helper.assertValueEqual(core.getGeneratedSpeed(), (float) COIConfig.DEFAULT_REACTOR_GENERATED_RPM, "default 32");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor_rpm")
    public static void stressDisplayAndRuntimeShareConfig(GameTestHelper helper) {
        PowerCoreBlockEntity core = placeCore(helper, true);
        core.configureForGameTest(1, 4000, 0, 0, 0.0);
        int previous = COIConfig.reactorGeneratedRpmInt();
        try {
            helper.assertValueEqual(previous, COIConfig.DEFAULT_REACTOR_GENERATED_RPM, "start at default");
            assertRpmWiring(helper, previous, "default wiring");
            helper.assertValueEqual(core.getGeneratedSpeed(), (float) previous, "default runtime");

            int mutated = 48;
            COIConfig.REACTOR_GENERATED_RPM.set(mutated);
            COIBlocks.invalidatePowerCoreRpmDisplay();
            helper.assertValueEqual(COIConfig.reactorGeneratedRpmInt(), mutated, "config helper follows set");
            assertRpmWiring(helper, mutated, "mutated wiring");
            helper.assertValueEqual(core.getGeneratedSpeed(), (float) mutated, "mutated runtime");
        } finally {
            COIConfig.REACTOR_GENERATED_RPM.set(previous);
            COIBlocks.invalidatePowerCoreRpmDisplay();
        }

        helper.assertValueEqual(core.getGeneratedSpeed(), (float) previous, "restored runtime");
        assertRpmWiring(helper, previous, "restored display");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "reactor")
    public static void chamberAttachesToPowerCore(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.POWER_CORE.getDefaultState());
        helper.assertTrue(ProcessAttachments.isReactorSupport(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))),
                "core is cooling support");
        helper.assertTrue(ProcessAttachments.isCoolingSupport(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))),
                "cooling support includes core");
        helper.setBlock(new BlockPos(2, 1, 1), COIBlocks.COOLING_CHAMBER.getDefaultState()
                .setValue(CoolingChamberBlock.FACING, Direction.EAST));
        CoolingChamberBlockEntity chamber = helper.getBlockEntity(new BlockPos(2, 1, 1));
        helper.assertTrue(chamber != null, "chamber BE on core");
        helper.assertTrue(chamber.getBlockState().canSurvive(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 1))),
                "chamber survives on core");
        helper.succeed();
    }

    private static int countPurestMolten(GameTestHelper helper, BlockPos centerRel, int radius) {
        Fluid molten = COIFluids.PUREST_MOLTEN_ORIGINIUM.getSource();
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos rel = centerRel.offset(dx, dy, dz);
                    if (helper.getLevel().getFluidState(helper.absolutePos(rel)).getType().isSame(molten)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean hasPurestMoltenSource(GameTestHelper helper, BlockPos rel) {
        var state = helper.getLevel().getFluidState(helper.absolutePos(rel));
        return state.isSource() && state.getType().isSame(COIFluids.PUREST_MOLTEN_ORIGINIUM.getSource());
    }

    private static void assertRpmWiring(GameTestHelper helper, int expected, String label) {
        var display = BlockStressValues.RPM.get(COIBlocks.POWER_CORE.get());
        helper.assertTrue(display != null, label + ": BlockStressValues.RPM registered");
        helper.assertValueEqual(display.value(), expected, label + ": display RPM");
        helper.assertValueEqual(COIBlocks.powerCoreGeneratedRpm().value(), expected, label + ": snapshot helper");
        helper.assertValueEqual(BlockStressValues.getCapacity(COIBlocks.POWER_CORE.get()),
                COIConfig.reactorStressCapacity(), label + ": capacity supplier");
    }

    private static PowerCoreBlockEntity placeCore(GameTestHelper helper, boolean withChamber) {
        BlockPos coreRel = new BlockPos(2, 1, 1);
        helper.setBlock(coreRel, COIBlocks.POWER_CORE.getDefaultState());
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.CORE_HOUSING.getDefaultState());
        if (withChamber) {
            helper.setBlock(new BlockPos(2, 1, 2), COIBlocks.COOLING_CHAMBER.getDefaultState()
                    .setValue(CoolingChamberBlock.FACING, Direction.SOUTH));
            CoolingChamberBlockEntity chamber = helper.getBlockEntity(new BlockPos(2, 1, 2));
            helper.assertTrue(chamber != null, "chamber BE");
            chamber.activateForGameTest();
        }
        PowerCoreBlockEntity core = helper.getBlockEntity(coreRel);
        helper.assertTrue(core != null, "power core BE");
        return core;
    }

    private static boolean recipePresent(GameTestHelper helper, String path) {
        return helper.getLevel().getRecipeManager().byKey(id(path)).isPresent();
    }

    private static Recipe<?> recipe(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(id(path));
        helper.assertTrue(holder.isPresent(), "recipe loaded: " + path);
        return holder.get().value();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
    }
}
