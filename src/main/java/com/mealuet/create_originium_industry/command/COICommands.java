package com.mealuet.create_originium_industry.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Central command registration for Create: Originium Industry.
 * Listens to {@link RegisterCommandsEvent} to register all mod commands.
 */
public class COICommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        COIDebugCommand.register(event.getDispatcher());
    }
}
