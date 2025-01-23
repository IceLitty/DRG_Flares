package me.lizardofoz.drgflares.neoforge.packet;

import me.lizardofoz.drgflares.neoforge.packet.client.SpawnFlareEntityS2CPacketClientHandler;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnFlareEntityS2CPacketWrapper
{

    /** Please make sure you're calling it from the main thread */
    public static void clientHandler(final SpawnFlareEntityS2CPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            SpawnFlareEntityS2CPacketClientHandler.apply(message);
        });
    }

    public static void serverHandler(final SpawnFlareEntityS2CPacket message, final IPayloadContext context) {
    }

}