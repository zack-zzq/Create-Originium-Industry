package com.mealuet.create_originium_industry.item;

import com.mealuet.create_originium_industry.index.COIBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;

/**
 * Create-flavoured protection gear: right-click to equip, no extra GUI.
 * Counted by {@code originium_protection} and {@code ProtectionHooks}.
 */
public class OriginiumProtectionItem extends Item implements Equipable {

    public static final int DEFAULT_DURABILITY = 240;

    private final EquipmentSlot slot;

    public OriginiumProtectionItem(Properties properties, EquipmentSlot slot) {
        super(properties.stacksTo(1).durability(DEFAULT_DURABILITY));
        this.slot = slot;
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return slot;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(COIBlocks.DUST_SIEVE.asItem());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".hint"));
    }
}
