package org.prism.autowork.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CartridgeComponent;

import java.util.List;

public class DyeCartridge extends Item {
    public DyeCartridge(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack cartridge, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        DyeItem dye;
        if (other.getItem() instanceof DyeItem d) {
            dye = d;
        }
        else {
            return false;
        }

        var component = cartridge.get(ModData.CARTRIDGE);

        int amount = action == ClickAction.SECONDARY ? 1 : other.getCount();


        if (component == null) {
            cartridge.set(ModData.CARTRIDGE, new CartridgeComponent(dye, amount*8, dye.getDyeColor().getTextureDiffuseColor()));
            other.shrink(amount);

            return true;
        }

        if (!other.is(component.dye())) {
            return false;
        }

        var amountInUses = amount*8;
        var newUses = amountInUses + component.currentUses();

        if (newUses > 64 * 8) {
            other.setCount((newUses - 64 * 8)/8);
            cartridge.set(ModData.CARTRIDGE, new CartridgeComponent(component.dye(), 64 * 8, component.color()));
        }
        else {
            other.shrink(amount);
            cartridge.set(ModData.CARTRIDGE, new CartridgeComponent(component.dye(), newUses, component.color()));
        }

        return true;
    }

    @Override
    public void appendHoverText(ItemStack cartridge, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var component = cartridge.get(ModData.CARTRIDGE);
        if (component != null) {
            tooltipComponents.add(Component.empty().append(component.dye().getName(ItemStack.EMPTY)).withColor(component.color()));
            tooltipComponents.add(Component.translatable("item.autowork.dye_cartridge.uses", component.currentUses(), 64*8).withColor(0xb3b3b3));
        }
        else {
            tooltipComponents.add(Component.translatable("item.autowork.dye_cartridge.insert_dye").withColor(0xb3b3b3).withStyle(ChatFormatting.ITALIC));
        }

        super.appendHoverText(cartridge, context, tooltipComponents, tooltipFlag);
    }

    public static ItemStack getDye(ItemStack cartridge) {
        var component = cartridge.get(ModData.CARTRIDGE);

        if (component == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(component.dye());
    }
    public static void useDye(ItemStack cartridge) {
        useDye(cartridge, 1);
    }

    public static void useDye(ItemStack cartridge, int i) {
        var component = cartridge.get(ModData.CARTRIDGE);

        if (component == null) {
            return;
        }

        int uses = component.currentUses() - i;

        if (uses == 0) {
            cartridge.remove(ModData.CARTRIDGE);
            return;
        }

        cartridge.set(ModData.CARTRIDGE, new CartridgeComponent(component.dye(), uses, component.color()));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModData.CARTRIDGE);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!isBarVisible(stack)) {
            return 0;
        }

        var component = stack.get(ModData.CARTRIDGE);

        assert component != null;

        var total = 64*8;
        var newWidth = component.currentUses();


        return Math.round(13.0F * ((float)newWidth/(float)total));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (!isBarVisible(stack)) {
            return 0;
        }

        var component = stack.get(ModData.CARTRIDGE);

        assert component != null;
        if (component.dye() instanceof DyeItem dye) {
            return dye.getDyeColor().getTextColor();
        }

        return super.getBarColor(stack);
    }
}
