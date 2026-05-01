package com.mealuet.create_originium_industry.item;

import com.mealuet.create_originium_industry.core.oridust.DustLevel;
import com.mealuet.create_originium_industry.core.oridust.OriginiumDustManager;
import com.mealuet.create_originium_industry.core.oridust.PlayerExposureData;
import com.mealuet.create_originium_industry.index.COIAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Debug tool for originium dust and player exposure inspection.
 * <ul>
 *   <li>Right-click air: shows player exposure info and current chunk dust</li>
 *   <li>Shift + Right-click block: shows that block's chunk dust info</li>
 * </ul>
 * Only performs logic on the server side.
 */
public class OriginiumDebugWandItem extends Item {

    public OriginiumDebugWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        // Right-click air: show player exposure info
        ChunkPos chunkPos = serverPlayer.chunkPosition();
        int dust = OriginiumDustManager.getDust(chunkPos);
        DustLevel dustLevel = DustLevel.fromDust(dust);

        PlayerExposureData data = COIAttachments.getPlayerExposure(serverPlayer);
        int exposure = data.getExposure();
        int infection = data.getInfection();

        serverPlayer.sendSystemMessage(Component.translatable(
                "item.create_originium_industry.originium_debug_wand.player_info",
                exposure, infection, dust
        ).append(" ").append(Component.translatable(dustLevel.getLangKey())));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide || player == null || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            // Shift + Right-click block: show block's chunk dust info
            ChunkPos chunkPos = new ChunkPos(context.getClickedPos());
            int dust = OriginiumDustManager.getDust(chunkPos);
            DustLevel dustLevel = DustLevel.fromDust(dust);

            serverPlayer.sendSystemMessage(Component.translatable(
                    "item.create_originium_industry.originium_debug_wand.chunk_info",
                    String.valueOf(chunkPos.x), String.valueOf(chunkPos.z), dust
            ).append(" ").append(Component.translatable(dustLevel.getLangKey())));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.create_originium_industry.originium_debug_wand.tooltip"));
        tooltipComponents.add(Component.translatable("item.create_originium_industry.originium_debug_wand.tooltip2"));
    }
}
