package com.mealuet.create_originium_industry.client.ponder;

import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Dust generation / diffusion and kinetic filter / sieve recovery scenes.
 */
public final class DustScenes {

    private DustScenes() {}

    public static void generation(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(COIPonderKeys.DUST_GENERATION, COIPonderKeys.copy(COIPonderKeys.DUST_GENERATION_HEADER));
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        var mill = util.grid().at(2, 1, 2);
        var kinetics = util.select().fromTo(2, 1, 2, 4, 1, 2);
        var meter = util.grid().at(5, 1, 4);

        scene.world().setKineticSpeed(kinetics, 0);
        scene.world().showSection(kinetics, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(kinetics, 32);
        scene.effects().indicateSuccess(mill);
        scene.idle(10);

        Vec3 millTop = util.vector().topOf(mill);
        scene.world().createItemEntity(millTop.add(0, 0.4, 0), Vec3.ZERO, COIItems.RAW_ORIGINIUM.asStack());
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_GENERATION_TEXT_1, millTop)
                .colored(PonderPalette.RED);
        scene.idle(90);

        scene.effects().emitParticles(
                millTop.add(0, 0.2, 0),
                scene.effects().simpleParticleEmitter(ParticleTypes.WHITE_ASH, new Vec3(0.08, 0.12, 0.08)),
                2.5f,
                40);
        scene.overlay().showOutline(PonderPalette.RED, "dust", util.select().fromTo(1, 1, 1, 5, 1, 5), 60);
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_GENERATION_TEXT_2, util.vector().topOf(meter));
        scene.world().showSection(util.select().position(meter), Direction.DOWN);
        scene.idle(90);

        scene.overlay().showControls(millTop, Pointing.DOWN, 50)
                .rightClick()
                .withItem(COIItems.ORIGINIUM_RESPIRATOR.asStack());
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_GENERATION_TEXT_3, millTop)
                .colored(PonderPalette.GREEN);
        scene.idle(80);
    }

    public static void filterRecovery(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(COIPonderKeys.DUST_FILTER, COIPonderKeys.copy(COIPonderKeys.DUST_FILTER_HEADER));
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        var filter = util.grid().at(2, 1, 3);
        var filterLine = util.select().fromTo(2, 1, 2, 2, 1, 4);
        var basin = util.grid().at(5, 1, 3);
        var sieve = util.grid().at(4, 1, 3);
        var alloy = util.grid().at(5, 1, 4);
        var fan = util.select().fromTo(0, 1, 5, 1, 1, 5);

        scene.world().setKineticSpeed(filterLine, 0);
        scene.world().showSection(filterLine, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(filterLine, 64);

        scene.overlay().showControls(util.vector().topOf(filter), Pointing.DOWN, 50)
                .rightClick()
                .withItem(COIBlocks.DUST_SIEVE.asStack());
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_FILTER_TEXT_1, util.vector().topOf(filter));
        scene.idle(90);

        scene.world().showSection(util.select().fromTo(4, 1, 3, 5, 1, 4), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, "sieve", util.select().position(sieve), 50);
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_FILTER_TEXT_2, util.vector().blockSurface(sieve, Direction.WEST))
                .colored(PonderPalette.GREEN);
        scene.idle(90);

        scene.world().showSection(fan, Direction.EAST);
        scene.world().setKineticSpeed(util.select().position(1, 1, 5), 32);
        scene.overlay().showControls(util.vector().topOf(alloy), Pointing.DOWN, 40)
                .withItem(COIBlocks.ALLOY_SIEVE.asStack());
        COIPonderOverlay.show(scene, 80, COIPonderKeys.DUST_FILTER_TEXT_3, util.vector().topOf(basin));
        scene.world().createItemEntity(
                util.vector().topOf(sieve).add(0, 0.15, 0),
                new Vec3(0, 0.05, 0),
                COIItems.ORIGINIUM_DUST.asStack());
        scene.idle(80);
    }
}
