package me.lizardofoz.drgflares.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class DRGFlaresClientUtil {

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
