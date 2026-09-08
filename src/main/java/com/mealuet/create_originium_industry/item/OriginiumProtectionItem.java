package com.mealuet.create_originium_industry.item;

import com.mealuet.create_originium_industry.index.COIBlocks;
import com.mealuet.create_originium_industry.index.COIItems;
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
import java.util.function.Predicate;

/**
 * Create-flavoured protection gear: right-click to equip, no extra GUI.
 * Counted by {@code originium_protection} and {@code ProtectionHooks}.
 */
public class OriginiumProtectionItem extends Item implements Equipable {

    public static final int DEFAULT_DURABILITY = 240;
    public static final int SEALED_DURABILITY = 480;

    private final EquipmentSlot slot;
    private final Predicate<ItemStack> repair;

    public OriginiumProtectionItem(Properties properties, EquipmentSlot slot) {
        this(properties, slot, DEFAULT_DURABILITY, stack -> stack.is(COIBlocks.DUST_SIEVE.asItem()));
    }

    public OriginiumProtectionItem(Properties properties, EquipmentSlot slot, int durability,
                                   Predicate<ItemStack> repair) {
        super(properties.stacksTo(1).durability(durability));
        this.slot = slot;
        this.repair = repair;
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    /**
     * Alloy sealed canister: chest slot, longer durability, repairs with alloy.
     */
    public static OriginiumProtectionItem sealedCanister(Properties properties) {
        return new OriginiumProtectionItem(properties, EquipmentSlot.CHEST, SEALED_DURABILITY, stack ->
                stack.is(COIItems.ORIGINIUM_ALLOY_INGOT.get())
                        || stack.is(COIBlocks.ALLOY_SIEVE.asItem())
                        || stack.is(COIBlocks.ALLOY_CASING.asItem()));
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
        return repair != null && repair.test(repairCandidate);
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
