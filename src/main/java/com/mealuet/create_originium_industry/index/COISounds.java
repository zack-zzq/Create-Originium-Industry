package com.mealuet.create_originium_industry.index;

import com.mealuet.create_originium_industry.CreateOriginiumIndustry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Industrial SFX. Additive registry ids — do not rename.
 * <p>
 * Placeholders are low metal / steam / grit. Do not replace with magic or
 * high-frequency crystal stings.
 */
public final class COISounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CreateOriginiumIndustry.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FILTER_WORK =
            register("filter_work");
    public static final DeferredHolder<SoundEvent, SoundEvent> HIGH_DUST =
            register("high_dust");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_STEADY =
            register("reactor_steady");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_ALARM =
            register("reactor_alarm");

    private COISounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateOriginiumIndustry.MODID, path);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    /**
     * One-shot filter/sieve capture cue. Safe on dedicated servers.
     * Clients also loop {@link #FILTER_WORK} on a spinning kinetic filter.
     */
    public static void playFilterWork(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        level.playSound(null, pos, FILTER_WORK.get(), SoundSource.BLOCKS, 0.45f, 0.85f + level.random.nextFloat() * 0.2f);
    }
}
