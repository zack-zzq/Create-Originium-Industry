package com.mealuet.create_originium_industry.client;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.client.ponder.COIPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Physical-client entry. Registers HUD / particle / tooltip listeners that
 * must not load on a dedicated server, plus Ponder scenes.
 */
@Mod(value = CreateOriginiumIndustry.MODID, dist = Dist.CLIENT)
public class CreateOriginiumIndustryClient {

    public CreateOriginiumIndustryClient(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(COIClientEvents.class);
        PonderIndex.addPlugin(new COIPonderPlugin());
    }
}
