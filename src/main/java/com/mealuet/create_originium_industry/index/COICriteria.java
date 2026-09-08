package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import com.mealuet.create_originium_industry.advancement.SimplePlayerTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom advancement criterion triggers. Additive registry ids — do not rename.
 */
public final class COICriteria {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, CreateOriginiumIndustry.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> DUST_EXPOSURE =
            TRIGGERS.register("dust_exposure", SimplePlayerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> DUST_PURIFIED =
            TRIGGERS.register("dust_purified", SimplePlayerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> POWER_CORE_STARTED =
            TRIGGERS.register("power_core_started", SimplePlayerTrigger::new);

    private COICriteria() {}

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }
}
