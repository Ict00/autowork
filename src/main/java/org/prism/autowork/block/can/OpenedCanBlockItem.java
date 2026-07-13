package org.prism.autowork.block.can;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.blockhelp.HelpfulBlockItem;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CanComponent;

import java.util.ArrayList;
import java.util.List;

public class OpenedCanBlockItem extends CanBlockItem {
    public OpenedCanBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var can = stack.getOrDefault(ModData.CAN, new CanComponent(new ArrayList<>(), Items.AIR));

        return Math.round(13.0F * can.food().size() / 9);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xf5ce42;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModData.CAN);
    }
}
