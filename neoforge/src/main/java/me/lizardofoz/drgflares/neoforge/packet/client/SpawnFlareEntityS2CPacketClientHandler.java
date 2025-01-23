package me.lizardofoz.drgflares.neoforge.packet.client;

import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnFlareEntityS2CPacketClientHandler {

    public static void apply(SpawnFlareEntityS2CPacket message) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
            return;
        FlareEntity entity = new FlareEntity(world, message.getColor());
        entity.syncPacketPositionCodec(message.getX(), message.getY(), message.getZ());
        entity.moveTo(message.getX(), message.getY(), message.getZ());
        entity.setId(message.getId());
        entity.setUUID(message.getUuid());
        entity.lifespan = message.getLifespan();
        entity.color = message.getColor();
        entity.bounceCount = message.getBounceCount();
        world.addEntity(entity);
    }

}
