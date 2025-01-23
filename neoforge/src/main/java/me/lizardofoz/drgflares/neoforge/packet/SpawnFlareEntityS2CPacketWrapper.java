package me.lizardofoz.drgflares.neoforge.packet;

import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnFlareEntityS2CPacketWrapper
{

    public static void clientHandler(final SpawnFlareEntityS2CPacket message, final IPayloadContext context) {
        context.enqueueWork(message::spawnOnClient);
    }

    public static void serverHandler(final SpawnFlareEntityS2CPacket message, final IPayloadContext context) {
    }

}