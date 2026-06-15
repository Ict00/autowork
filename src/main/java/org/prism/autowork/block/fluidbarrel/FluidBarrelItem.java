package org.prism.autowork.block.fluidbarrel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.prism.autowork.block.placer.ISpecialPlaceable;
import org.prism.autowork.blockhelp.HelpfulBlockItem;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.FluidStackComponent;
import org.prism.autowork.other.datamaps.FluidColorOverride;

import java.util.List;

public class FluidBarrelItem extends HelpfulBlockItem implements ISpecialPlaceable {
    public FluidBarrelItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        var fluid = stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty());

        return !fluid.isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var fluid = stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();
        int fluidAmount = fluid.getAmount();

        return Math.round(13.0F * fluidAmount / 5000.0F);
    }

    @Override
    public void placePostAction(ItemStack stack, Level level, BlockPos pos) {
        FluidStack containing = stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();

        if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity entity) {
            var cap = entity.getCapability(null);
            cap.fill(containing, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        FluidStack containing = context.getItemInHand().getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();
        var res = super.place(context);

        if (res == InteractionResult.sidedSuccess(context.getLevel().isClientSide)) {
            BlockPos pos = context.getClickedPos();

            if (context.getLevel().getBlockEntity(pos) instanceof FluidBarrelBlockEntity entity) {
                var cap = entity.getCapability(null);
                cap.fill(containing, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return res;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var fluid = stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();


        if (fluid.isEmpty()) {
            return 0x404040;
        }

        var col = IClientFluidTypeExtensions.of(fluid.getFluid())
                .getTintColor(fluid)
                & 0xFFFFFF;

        if (col == 0xFFFFFF) {
            var d =  FluidColorOverride.getColor(fluid.getFluid());
            if (d == 0xFFFFFF) {
                try {
                    return fluid.getFluid().defaultFluidState().createLegacyBlock().getMapColor(null, new BlockPos(0, 0, 0)).col;
                }
                catch (Exception ignore) {
                    return d;
                }
            }
            return d;
        }

        return col;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).isEmpty() ? 64 : 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        if (isBarVisible(stack)) {
            var color = getBarColor(stack);
            var fluid = stack.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();

            MutableComponent component = Component.empty().append(Component.empty().append(fluid.getHoverName()).withColor(color));

            component.append(Component.literal("  [").withColor(0xadadad));
            var max = 16;
            var r = (int)(16f * (fluid.getAmount()/5000f));

            for (int i = 0; i < max; i++) {
                var newComp = Component.literal("|");

                if (i <= r) {
                    newComp = newComp.withColor(color);
                }
                else {
                    newComp = newComp.withColor(0xadadad);
                }
                component = component.append(newComp);
            }

            component = component.append(Component.literal("]").withColor(0xadadad));
            components.add(component);


            components.add(Component.literal("------------------").withColor(0xadadad));
        }

        super.appendHoverText(stack, context, components, tooltip);
    }
}
