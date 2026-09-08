package com.mealuet.create_originium_industry.client.ponder;

import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Supercooling / purest production-line scene.
 */
public final class PurestScenes {

    private PurestScenes() {}

    public static void supercooling(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(COIPonderKeys.PUREST_SUPERCOOLING, COIPonderKeys.copy(COIPonderKeys.PUREST_HEADER));
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        var basin = util.grid().at(3, 1, 3);
        var mixer = util.grid().at(3, 3, 3);
        var sieve = util.grid().at(2, 1, 3);
        var chamber = util.grid().at(4, 1, 3);
        var ice = util.grid().at(3, 1, 4);

        scene.world().showSection(util.select().position(basin), Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(util.select().position(sieve), Direction.EAST);
        scene.idle(8);
        scene.world().showSection(util.select().position(mixer), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(mixer), 32);

        COIPonderOverlay.show(scene, 80, COIPonderKeys.PUREST_TEXT_1, util.vector().blockSurface(sieve, Direction.WEST));
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 50)
                .withItem(new ItemStack(COIFluids.MOLTEN_ORIGINIUM.get().getBucket()));
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(basin), Pointing.RIGHT, 50)
                .withItem(new ItemStack(COIFluids.ORIGINIUM_CATALYST.get().getBucket()));
        COIPonderOverlay.show(scene, 80, COIPonderKeys.PUREST_TEXT_2, util.vector().topOf(mixer));
        scene.idle(90);

        scene.world().showSection(util.select().position(chamber), Direction.WEST);
        scene.world().showSection(util.select().position(ice), Direction.NORTH);
        scene.overlay().showOutline(PonderPalette.BLUE, "chamber", util.select().position(chamber), 60);
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 50)
                .withItem(new ItemStack(Items.PACKED_ICE));
        COIPonderOverlay.show(scene, 80, COIPonderKeys.PUREST_TEXT_3, util.vector().blockSurface(chamber, Direction.EAST))
                .colored(PonderPalette.BLUE);
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 40)
                .withItem(AllBlocks.BLAZE_BURNER.asStack());
        scene.world().createItemEntity(
                util.vector().topOf(basin).add(0, 0.35, 0),
                Vec3.ZERO,
                COIItems.PUREST_ORIGINIUM.asStack());
        COIPonderOverlay.show(scene, 90, COIPonderKeys.PUREST_TEXT_4, util.vector().topOf(basin))
                .colored(PonderPalette.RED);
        scene.idle(90);
    }
}
