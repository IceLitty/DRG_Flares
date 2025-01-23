package me.lizardofoz.drgflares.item;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.item.Item;

public class FlareDispenserBehavior extends ProjectileDispenseBehavior
{
    public FlareDispenserBehavior(Item projectile) {
        super(projectile);
    }

    public static void initialize()
    {
        for (Item item : DRGFlareRegistry.getInstance().getFlareItemTypes().values()) {
            FlareDispenserBehavior behavior = new FlareDispenserBehavior(item);
            DispenserBlock.registerBehavior(item, behavior);
        }
    }

}