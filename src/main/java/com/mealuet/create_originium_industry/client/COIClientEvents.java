package com.mealuet.create_originium_industry.client;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.DebugOverlayDetail;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.VisibleDust;
import com.mealuet.create_originium_industry.index.COIEffects;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client presentation gated by {@link COIClientOptions}.
 * Dust numbers come from {@link VisibleDust} (nearby sync cache).
 */
public final class COIClientEvents {

    private COIClientEvents() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        MobEffectInstance sickness = player.getEffect(COIEffects.ORI_DUST_SICKNESS_EFFECT);
        GuiGraphics graphics = event.getGuiGraphics();
        if (sickness != null && (COIClientOptions.showSicknessHud()
                || COIClientOptions.debugOverlayDetail() != DebugOverlayDetail.OFF)) {
            drawSicknessHud(graphics, mc, player, sickness);
        } else if (COIClientOptions.debugOverlayDetail() == DebugOverlayDetail.FULL) {
            drawDebugLines(graphics, mc, player, 0);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        MobEffectInstance sickness = player.getEffect(COIEffects.ORI_DUST_SICKNESS_EFFECT);
        int sickBase = sickness == null ? 0 : 1 + sickness.getAmplifier();
        int dustBase = dustParticleBase(player);
        COIIndustrialSounds.tick(mc, player);

        int count = COIClientOptions.particleCount(Math.max(sickBase, dustBase));
        if (count <= 0) {
            return;
        }
        int period = COIClientOptions.reduceFlicker() ? 10 : 4;
        if (player.tickCount % period != 0) {
            return;
        }
        var random = player.getRandom();
        for (int i = 0; i < count; i++) {
            double dx = (random.nextDouble() - 0.5) * 1.2;
            double dz = (random.nextDouble() - 0.5) * 1.2;
            mc.level.addParticle(
                    ParticleTypes.WHITE_ASH,
                    player.getX() + dx,
                    player.getY() + 0.2 + random.nextDouble() * 1.4,
                    player.getZ() + dz,
                    0.0, 0.02, 0.0
            );
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!COIConfig.debugTooltipsEnabled()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.is(COITags.Items.ORIGINIUM_MATERIALS) || stack.is(COITags.Items.DUST_PRODUCING)) {
            event.getToolTip().add(Component.translatable(
                    "item.create_originium_industry.debug.dust_item"));
        }
        if (stack.is(COITags.Items.ORIGINIUM_PROTECTION)) {
            event.getToolTip().add(Component.translatable(
                    "item.create_originium_industry.debug.protection_item"));
        }
        Player player = event.getEntity();
        if (player != null) {
            event.getToolTip().add(Component.translatable(
                    "item.create_originium_industry.debug.client_readout",
                    VisibleDust.chunkDustAt(player),
                    VisibleDust.exposure(player),
                    VisibleDust.infection(player)
            ));
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        VisibleDust.clearClientCaches();
        COIIndustrialSounds.stopAll();
    }

    private static void drawSicknessHud(GuiGraphics graphics, Minecraft mc, LocalPlayer player, MobEffectInstance sickness) {
        int amplifier = sickness.getAmplifier();
        DustLevel visual = dustVisual(player, amplifier);
        boolean contrast = COIClientOptions.highContrastIndicators();
        int color = visual.getArgb(contrast);

        int x = 8;
        int y = mc.getWindow().getGuiScaledHeight() - 48;
        String text;
        if (COIClientOptions.uiDetailLevel() == UiDetailLevel.VERBOSE
                || COIClientOptions.debugOverlayDetail() == DebugOverlayDetail.FULL) {
            text = Component.translatable("hud.create_originium_industry.sickness.verbose", amplifier + 1)
                    .getString();
        } else {
            text = Component.translatable("hud.create_originium_industry.sickness").getString();
        }

        int width = mc.font.width(text) + 8;
        int alpha = contrast ? 0xEE : (COIClientOptions.reduceFlicker() ? 0x88 : pulseAlpha(mc));
        int bg = alpha << 24;
        graphics.fill(x - 2, y - 2, x + width, y + 12, bg);
        graphics.drawString(mc.font, text, x, y, color, !contrast);

        if (COIClientOptions.debugOverlayDetail() == DebugOverlayDetail.FULL) {
            drawDebugLines(graphics, mc, player, 1);
        }
    }

    private static void drawDebugLines(GuiGraphics graphics, Minecraft mc, LocalPlayer player, int startIndex) {
        int x = 8;
        int y = mc.getWindow().getGuiScaledHeight() - 48;
        boolean contrast = COIClientOptions.highContrastIndicators();
        int color = contrast ? 0xFFFFFF00 : 0xFFAAAAAA;
        int index = startIndex;
        if (!VisibleDust.dustSyncEnabled()) {
            graphics.drawString(
                    mc.font,
                    Component.translatable("hud.create_originium_industry.debug.dust_unsynced").getString(),
                    x,
                    y + index * 12,
                    color,
                    false
            );
            index++;
        } else {
            int dust = VisibleDust.chunkDustAt(player);
            DustLevel risk = DustLevel.fromDust(dust);
            graphics.drawString(
                    mc.font,
                    Component.translatable(
                            "hud.create_originium_industry.debug.dust",
                            dust,
                            Component.translatable(risk.getLangKey())
                    ).getString(),
                    x,
                    y + index * 12,
                    color,
                    false
            );
            index++;
        }
        graphics.drawString(
                mc.font,
                Component.translatable(
                        "hud.create_originium_industry.debug.exposure",
                        VisibleDust.exposure(player),
                        VisibleDust.infection(player)
                ).getString(),
                x,
                y + index * 12,
                color,
                false
        );
    }

    private static int dustParticleBase(LocalPlayer player) {
        if (!VisibleDust.dustSyncEnabled()) {
            return 0;
        }
        int dust = VisibleDust.chunkDustAt(player);
        return switch (DustLevel.fromDust(dust)) {
            case SAFE -> 0;
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 4;
            case CRITICAL -> 6;
        };
    }

    private static DustLevel dustVisual(LocalPlayer player, int amplifier) {
        if (VisibleDust.dustSyncEnabled()) {
            DustLevel fromDust = DustLevel.fromDust(VisibleDust.chunkDustAt(player));
            if (fromDust != DustLevel.SAFE) {
                return fromDust;
            }
        }
        return amplifierToVisual(amplifier);
    }

    private static DustLevel amplifierToVisual(int amplifier) {
        if (amplifier <= 0) return DustLevel.LOW;
        if (amplifier == 1) return DustLevel.MEDIUM;
        if (amplifier == 2) return DustLevel.HIGH;
        return DustLevel.CRITICAL;
    }

    private static int pulseAlpha(Minecraft mc) {
        float t = (mc.player != null ? mc.player.tickCount : 0) / 8.0f;
        return 0x44 + (int) (0x22 * (0.5f + 0.5f * Mth.sin(t)));
    }
}
