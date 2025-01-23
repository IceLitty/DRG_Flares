package me.lizardofoz.drgflares.item;

import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class FlareItem extends Item implements ProjectileItem {

    public FlareItem(Item.Properties settings)
    {
        super(settings);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide())
            FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
        if (!player.getAbilities().instabuild)
            itemStack.shrink(1);
        player.getCooldowns().addCooldown(this, 5);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        FlareEntity flare = new FlareEntity(world, DRGFlaresUtil.getFlareColorFromItem(stack));
        flare.setPos(pos.x(), pos.y(), pos.z());
        return flare;
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder()
                .power(ServerSettings.CURRENT.flareThrowSpeed.value)
                .build();
    }

}