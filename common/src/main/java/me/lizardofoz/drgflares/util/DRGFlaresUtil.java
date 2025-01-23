package me.lizardofoz.drgflares.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.mixin.RecipeManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DRGFlaresUtil
{
    private DRGFlaresUtil() { }

    public static void removeFlareRecipes(RecipeManager recipeManager) {
        RecipeManagerAccessor accessor = (RecipeManagerAccessor) recipeManager;
        Multimap<RecipeType<?>, RecipeHolder<?>> newByType = HashMultimap.create();
        Multimap<RecipeType<?>, RecipeHolder<?>> byType = accessor.getByType();
        for (Map.Entry<RecipeType<?>, RecipeHolder<?>> byTypeEntry : byType.entries()) {
            if (!"drg_flares".equals(byTypeEntry.getValue().id().getNamespace())) {
                newByType.put(byTypeEntry.getKey(), byTypeEntry.getValue());
            }
        }
        accessor.setByType(newByType);
        Map<ResourceLocation, RecipeHolder<?>> newByName = Maps.newHashMap();
        Map<ResourceLocation, RecipeHolder<?>> byName = accessor.getByName();
        for (Map.Entry<ResourceLocation, RecipeHolder<?>> byNameEntry : byName.entrySet()) {
            if (!"drg_flares".equals(byNameEntry.getValue().id().getNamespace())) {
                newByName.put(byNameEntry.getKey(), byNameEntry.getValue());
            }
        }
        accessor.setByName(newByName);
    }

//    public static void setRecipes(RecipeManager recipeManager, Iterable<RecipeHolder<?>> recipes)
//    {
//        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = Maps.newHashMap();
//        recipes.forEach((recipe) -> {
//            Map<ResourceLocation, Recipe<?>> map2 = map.computeIfAbsent(recipe.value().getType(), (recipeType) -> Maps.newHashMap());
//            Recipe<?> recipe2 = map2.put(recipe.id(), recipe.value());
//            if (recipe2 != null)
//                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.id());
//        });
//        ImmutableMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> result = ImmutableMap.copyOf(map);
//        ((RecipeManagerAccessor) recipeManager).setRecipes(result);
//    }

    public static boolean isOnRemoteServer()
    {
        try
        {
            return !Minecraft.getInstance().getConnection().getConnection().isMemoryConnection();
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    public static int getVoidDamageLevel(Level world)
    {
        return world.dimensionType().minY() - 64;
    }

    public static boolean hasUnlimitedRegeneratingFlares(Player player)
    {
        return (player.getAbilities().instabuild && ServerSettings.CURRENT.creativeUnlimitedRegeneratingFlares.value) || ServerSettings.CURRENT.unlimitedSurvivalFlares();
    }

    public static boolean isRegenFlareOnCooldown(Player player)
    {
        return player.getCooldowns().isOnCooldown(DRGFlareRegistry.getInstance().getFlareItemTypes().get(FlareColor.RED));
    }

    public static FlareColor getFlareColorFromItem(ItemStack stack)
    {
        for (Map.Entry<FlareColor, Item> entry : DRGFlareRegistry.getInstance().getFlareItemTypes().entrySet())
        {
            if (stack.getItem().equals(entry.getValue()))
                return entry.getKey();
        }
        return FlareColor.RED;
    }

    public static boolean tryFlare(Player player, List<ItemStack> inventorySection)
    {
        for (ItemStack itemStack : inventorySection)
        {
            Item item = itemStack.getItem();
            if (item instanceof FlareItem)
            {
                if (player.getCooldowns().isOnCooldown(item))
                    return true;
                FlareEntity.throwFlare(player, DRGFlaresUtil.getFlareColorFromItem(itemStack));
                if (!player.getAbilities().instabuild)
                    itemStack.shrink(1);
                player.getCooldowns().addCooldown(item, 5);
                player.awardStat(Stats.ITEM_USED.get(item));
                return true;
            }
        }
        return false;
    }

    //We had to move these 2 methods outside, so that a Dedicated Server won't try to load Client-Only classes
    @Environment(EnvType.CLIENT)
    public static void playSoundFromEntityOnClient(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch)
    {
        Minecraft.getInstance().getSoundManager().play(new EntityBoundSoundInstance(sound, category, volume, pitch, entity, new Random().nextLong()));
    }

    @Environment(EnvType.CLIENT)
    public static void addEntityOnClient(Level world, Entity entity)
    {
        ((ClientLevel) world).addEntity(entity);
    }
}