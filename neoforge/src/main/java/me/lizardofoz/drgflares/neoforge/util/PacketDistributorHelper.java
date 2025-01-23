package me.lizardofoz.drgflares.neoforge.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavadocReference")
public class PacketDistributorHelper {

    /**
     * {@link PacketDistributor#makeClientboundPacket(CustomPacketPayload, CustomPacketPayload...)}
     */
    public static Packet<?> makeClientboundPacket(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (payloads.length > 0) {
            final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(new ClientboundCustomPayloadPacket(payload));
            for (CustomPacketPayload otherPayload : payloads) {
                packets.add(new ClientboundCustomPayloadPacket(otherPayload));
            }
            return new ClientboundBundlePacket(packets);
        } else {
            return new ClientboundCustomPayloadPacket(payload);
        }
    }

}
