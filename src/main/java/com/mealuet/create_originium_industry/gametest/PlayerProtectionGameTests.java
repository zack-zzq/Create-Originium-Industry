package com.mealuet.create_originium_industry.gametest;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.block.DustMeterBlockEntity;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.core.oridust.InfectionStage;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.core.oridust.ProtectionHooks;
import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Coverage for issue #16: protection gear, infection stages, death retain,
 * and dust-meter protection readout.
 */
@GameTestHolder(CreateOriginiumIndustry.MODID)
@PrefixGameTestTemplate(false)
public final class PlayerProtectionGameTests {

    private PlayerProtectionGameTests() {}

    @GameTest(template = "empty", batch = "player_protection")
    public static void gearIsTaggedAndCraftable(GameTestHelper helper) {
        ItemStack mask = new ItemStack(COIItems.ORIGINIUM_RESPIRATOR.get());
        ItemStack canister = new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get());
        helper.assertTrue(mask.is(COITags.Items.ORIGINIUM_PROTECTION), "respirator tagged");
        helper.assertTrue(canister.is(COITags.Items.ORIGINIUM_PROTECTION), "canister tagged");
        helper.assertTrue(ProtectionHooks.isProtectionItem(mask), "respirator hook");
        helper.assertTrue(ProtectionHooks.isProtectionItem(canister), "canister hook");
        helper.assertFalse(
                ProtectionHooks.isProtectionItem(new ItemStack(COIItems.ORIGINIUM_DUST.get())),
                "dust is not protection"
        );
        helper.assertTrue(recipePresent(helper, "crafting/originium_respirator"), "respirator recipe");
        helper.assertTrue(recipePresent(helper, "crafting/originium_filter_canister"), "canister recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "player_protection")
    public static void wornGearReducesExposureGain(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.PROTECTION_FULL_SET_PIECES.get(), 2, "full set is two pieces");
        helper.assertValueEqual(COIConfig.PROTECTION_EXPOSURE_REDUCTION.get(), 0.5, "half gain at full set");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(ProtectionHooks.countProtectionPieces(player), 0, "bare");
        helper.assertValueEqual(ProtectionHooks.incomingExposureFactor(player), 1.0, "unprotected full gain");
        helper.assertValueEqual(ProtectionHooks.exposureReductionPercent(player), 0, "no reduction");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(COIItems.ORIGINIUM_RESPIRATOR.get()));
        helper.assertValueEqual(ProtectionHooks.countProtectionPieces(player), 1, "mask only");
        helper.assertValueEqual(ProtectionHooks.incomingExposureFactor(player), 0.75, "half coverage");
        helper.assertValueEqual(ProtectionHooks.exposureReductionPercent(player), 25, "25% less exposure");

        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get()));
        helper.assertValueEqual(ProtectionHooks.countProtectionPieces(player), 2, "full set");
        helper.assertValueEqual(ProtectionHooks.incomingExposureFactor(player), 0.5, "full set halves gain");
        helper.assertValueEqual(ProtectionHooks.exposureReductionPercent(player), 50, "50% less exposure");
        helper.assertTrue(
                ProtectionHooks.incomingExposureFactor(player) < 1.0,
                "same dust zone gains slower with gear"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "player_protection")
    public static void infectionStagesMatchConfiguredTable(GameTestHelper helper) {
        helper.assertTrue(COIConfig.ENABLE_INFECTION_STAGES.get(), "stages on");
        helper.assertValueEqual(COIConfig.INFECTION_STAGE_WEAKNESS.get(), 200, "weakness");
        helper.assertValueEqual(COIConfig.INFECTION_STAGE_RESTRICTED.get(), 800, "restricted");
        helper.assertValueEqual(COIConfig.INFECTION_STAGE_GROWTH.get(), 2500, "growth");
        helper.assertValueEqual(COIConfig.INFECTION_STAGE_BARGAIN.get(), 6000, "bargain");

        helper.assertValueEqual(InfectionStage.fromInfection(0), InfectionStage.NONE, "clean");
        helper.assertValueEqual(InfectionStage.fromInfection(199), InfectionStage.NONE, "below weakness");
        helper.assertValueEqual(InfectionStage.fromInfection(200), InfectionStage.WEAKNESS, "weakness floor");
        helper.assertValueEqual(InfectionStage.fromInfection(799), InfectionStage.WEAKNESS, "still weakness");
        helper.assertValueEqual(InfectionStage.fromInfection(800), InfectionStage.RESTRICTED, "restricted");
        helper.assertValueEqual(InfectionStage.fromInfection(2499), InfectionStage.RESTRICTED, "still restricted");
        helper.assertValueEqual(InfectionStage.fromInfection(2500), InfectionStage.GROWTH, "growth");
        helper.assertValueEqual(InfectionStage.fromInfection(5999), InfectionStage.GROWTH, "still growth");
        helper.assertValueEqual(InfectionStage.fromInfection(6000), InfectionStage.BARGAIN, "bargain");
        helper.assertValueEqual(InfectionStage.fromInfection(10000), InfectionStage.BARGAIN, "capped bargain");

        PlayerExposureData data = new PlayerExposureData();
        data.setInfection(2500);
        helper.assertValueEqual(data.getInfectionStage(), InfectionStage.GROWTH, "data helper");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "player_protection")
    public static void infectionStagesApplyVanillaEffects(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        InfectionStage.WEAKNESS.apply(player, 40);
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "weakness: weakness");

        player.removeAllEffects();
        InfectionStage.RESTRICTED.apply(player, 40);
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "restricted: weakness");
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SLOWDOWN), "restricted: mining fatigue");

        player.removeAllEffects();
        InfectionStage.GROWTH.apply(player, 40);
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SLOWDOWN), "growth: mining fatigue");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "growth: slowness");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "growth: hunger");

        player.removeAllEffects();
        InfectionStage.BARGAIN.apply(player, 40);
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SPEED), "bargain benefit: haste");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "bargain benefit: strength");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "bargain cost: hunger");
        helper.assertFalse(player.hasEffect(MobEffects.WITHER), "no wither");

        player.removeAllEffects();
        InfectionStage.NONE.apply(player, 40);
        helper.assertFalse(player.hasEffect(MobEffects.WEAKNESS), "none applies nothing");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "player_protection")
    public static void deathRetainDefaultsAreSingleplayerFriendly(GameTestHelper helper) {
        helper.assertValueEqual(COIConfig.DEATH_EXPOSURE_RETAIN.get(), 0.0, "clear exposure");
        helper.assertValueEqual(COIConfig.DEATH_INFECTION_RETAIN.get(), 0.25, "keep a quarter infection");

        helper.assertValueEqual(PlayerExposureData.retain(1000, 0.0), 0, "clear");
        helper.assertValueEqual(PlayerExposureData.retain(1000, 0.25), 250, "partial");
        helper.assertValueEqual(PlayerExposureData.retain(1000, 1.0), 1000, "keep");
        helper.assertValueEqual(PlayerExposureData.retain(0, 1.0), 0, "zero stays zero");

        PlayerExposureData data = new PlayerExposureData();
        data.setExposure(800);
        data.setInfection(400);
        data.applyDeathRetention();
        helper.assertValueEqual(data.getExposure(), 0, "SP default clears exposure");
        helper.assertValueEqual(data.getInfection(), 100, "SP default keeps 25% infection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "player_protection")
    public static void dustMeterProtectionHintUsesRealGear(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), COIBlocks.DUST_METER.getDefaultState());
        DustMeterBlockEntity meter = helper.getBlockEntity(new BlockPos(1, 1, 1));
        helper.assertTrue(meter != null, "meter BE");

        meter.refreshFromServer();
        helper.assertValueEqual(meter.protectionPercent(), 0, "no player nearby");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertValueEqual(ProtectionHooks.exposureReductionPercent(player), 0, "bare mock");

        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(COIItems.ORIGINIUM_RESPIRATOR.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get()));
        helper.assertValueEqual(
                ProtectionHooks.exposureReductionPercent(player),
                50,
                "meter formula sees full set"
        );

        // In-world player so the meter's AABB scan can see tagged gear.
        var serverPlayer = helper.makeMockServerPlayerInLevel();
        BlockPos abs = helper.absolutePos(new BlockPos(1, 1, 1));
        serverPlayer.teleportTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
        serverPlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(COIItems.ORIGINIUM_RESPIRATOR.get()));
        serverPlayer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(COIItems.ORIGINIUM_FILTER_CANISTER.get()));
        meter.refreshFromServer();
        helper.assertValueEqual(meter.protectionPercent(), 50, "meter reads nearby full set");
        helper.succeed();
    }

    private static boolean recipePresent(GameTestHelper helper, String path) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path));
        return holder.isPresent();
    }
}
