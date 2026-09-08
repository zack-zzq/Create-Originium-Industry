package com.mealuet.create_originium_industry.client.ponder;

import com.mealuet.create_originium_industry.ponder.COIPonderKeys;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.TextElementBuilder;
import net.minecraft.world.phys.Vec3;

/**
 * Overlay helper so every Ponder line goes through {@link COIPonderKeys#copy(String)}.
 */
final class COIPonderOverlay {

    private COIPonderOverlay() {}

    static TextElementBuilder show(CreateSceneBuilder scene, int ticks, String key, Vec3 point) {
        return scene.overlay().showText(ticks)
                .text(COIPonderKeys.copy(key))
                .pointAt(point)
                .placeNearTarget()
                .attachKeyFrame();
    }
}
