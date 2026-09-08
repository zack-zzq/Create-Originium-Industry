package com.mealuet.create_originium_industry.client.ponder;

import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.index.COIFluids;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
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
 * Power-core housing, S = C×M−H, and non-explosive meltdown.
 */
public final class ReactorScenes {

    private ReactorScenes() {}

    public static void stability(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(COIPonderKeys.POWER_CORE, COIPonderKeys.copy(COIPonderKeys.POWER_CORE_HEADER));
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        var core = util.grid().at(3, 1, 3);
        var housing = util.select().fromTo(3, 1, 2, 3, 1, 4);
        var chamber = util.grid().at(2, 1, 3);
        var superChamber = util.grid().at(3, 2, 3);
        var shafts = util.select().fromTo(4, 1, 3, 5, 1, 3);

        scene.world().showSection(util.select().position(core), Direction.DOWN);
        scene.idle(8);
        scene.world().showSection(housing, Direction.DOWN);
        scene.idle(8);

        scene.overlay().showControls(util.vector().topOf(core), Pointing.DOWN, 50)
                .rightClick()
                .withItem(COIItems.PUREST_ORIGINIUM.asStack());
        COIPonderOverlay.show(scene, 80, COIPonderKeys.POWER_CORE_TEXT_1, util.vector().topOf(core));
        scene.idle(90);

        scene.world().showSection(util.select().position(chamber), Direction.EAST);
        scene.world().showSection(util.select().position(superChamber), Direction.DOWN);
        scene.world().showSection(shafts, Direction.WEST);
        scene.world().setKineticSpeed(util.select().fromTo(3, 1, 3, 5, 1, 3),
                COIConfig.DEFAULT_REACTOR_GENERATED_RPM);

        scene.overlay().showControls(util.vector().blockSurface(core, Direction.UP), Pointing.DOWN, 40)
                .rightClick()
                .withItem(new ItemStack(COIFluids.ORIGINIUM_COOLANT.get().getBucket()));
        scene.overlay().showOutline(PonderPalette.BLUE, "cooling", util.select().fromTo(2, 1, 3, 3, 2, 3), 70);
        COIPonderOverlay.show(scene, 90, COIPonderKeys.POWER_CORE_TEXT_2, util.vector().blockSurface(superChamber, Direction.UP))
                .colored(PonderPalette.BLUE);
        scene.idle(100);

        COIPonderOverlay.show(scene, 90, COIPonderKeys.POWER_CORE_TEXT_3, util.vector().topOf(core))
                .colored(PonderPalette.GREEN);
        scene.idle(100);

        scene.overlay().showControls(util.vector().topOf(core), Pointing.DOWN, 40)
                .withItem(new ItemStack(Items.TNT));
        scene.overlay().showControls(util.vector().blockSurface(core, Direction.EAST), Pointing.RIGHT, 40)
                .withItem(new ItemStack(COIFluids.PUREST_MOLTEN_ORIGINIUM.get().getBucket()));
        scene.overlay().showOutline(PonderPalette.RED, "meltdown", util.select().position(core), 80);
        scene.world().createItemEntity(
                util.vector().topOf(core).add(0.3, 0.2, 0),
                new Vec3(0.05, 0.08, 0),
                COIItems.ORIGINIUM_DUST.asStack());
        COIPonderOverlay.show(scene, 90, COIPonderKeys.POWER_CORE_TEXT_4, util.vector().topOf(core))
                .colored(PonderPalette.RED);
        scene.idle(90);
    }
}
