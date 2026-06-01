package org.prism.autowork.item.custom;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.prism.autowork.entities.signal.SignalEntity;

public class RedstoneChargeItem extends Item {
    public RedstoneChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            player.getCooldowns().addCooldown(this, 10);
            SignalEntity signal = new SignalEntity(player.getX(), player.getEyeY(), player.getZ(), new Vec3(player.getXRot(), player.getYRot(), 0), level);
            signal.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1f, 0.0F);

            level.addFreshEntity(signal);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, player);

        return InteractionResultHolder.success(itemstack);
    }
}
