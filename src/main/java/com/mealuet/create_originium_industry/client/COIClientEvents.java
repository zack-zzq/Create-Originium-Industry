package com.mealuet.create_originium_industry.client;

import com.mealuet.create_originium_industry.config.COIClientOptions;
import com.mealuet.create_originium_industry.config.COIConfig;
import com.mealuet.create_originium_industry.config.DebugOverlayDetail;
import com.mealuet.create_originium_industry.config.UiDetailLevel;
import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.index.COIEffects;
import com.mealuet.create_originium_industry.index.COITags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client presentation gated by {@link COIClientOptions}.
 * Dust numbers are not synced yet ({@code multiplayer.syncDustToClients} is
 * reserved); HUD uses the already-synced sickness effect.
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
        if (sickness == null) {
            if (COIClientOptions.debugOverlayDetail() == DebugOverlayDetail.FULL) {
                drawDebugLine(event.getGuiGraphics(), mc, 0,
                        Component.translatable("hud.create_originium_industry.debug.no_sickness"));
            }
            return;
        }

        if (COIClientOptions.showSicknessHud()) {
            drawSicknessHud(event.getGuiGraphics(), mc, sickness);
        } else if (COIClientOptions.debugOverlayDetail() != DebugOverlayDetail.OFF) {
            drawSicknessHud(event.getGuiGraphics(), mc, sickness);
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
        if (sickness == null) {
            return;
        }
        int base = 1 + sickness.getAmplifier();
        int count = COIClientOptions.particleCount(base);
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
    }

    private static void drawSicknessHud(GuiGraphics graphics, Minecraft mc, MobEffectInstance sickness) {
        int amplifier = sickness.getAmplifier();
        DustLevel visual = amplifierToVisual(amplifier);
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
            graphics.drawString(
                    mc.font,
                    Component.translatable("hud.create_originium_industry.debug.dust_unsynced").getString(),
                    x,
                    y + 12,
                    contrast ? 0xFFFFFF00 : 0xFFAAAAAA,
                    false
            );
        }
    }

    private static void drawDebugLine(GuiGraphics graphics, Minecraft mc, int index, Component line) {
        int x = 8;
        int y = mc.getWindow().getGuiScaledHeight() - 48 - index * 10;
        graphics.drawString(mc.font, line.getString(), x, y, 0xFFAAAAAA, false);
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
