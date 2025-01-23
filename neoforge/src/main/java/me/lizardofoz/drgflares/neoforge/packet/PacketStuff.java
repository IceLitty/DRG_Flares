package me.lizardofoz.drgflares.neoforge.packet;

import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = "drg_flares", bus = EventBusSubscriber.Bus.MOD)
public final class PacketStuff
{
    private static final String PROTOCOL_VERSION = "1.0";

    private PacketStuff() { }

    /**
     * 注册网络包处理器事件
     */
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // 协议版本"1"
        final PayloadRegistrar registrar = event.registrar("1.0").executesOn(HandlerThread.MAIN);
        // 以下处理器在主线程执行
        registrar.playBidirectional(
                ThrowFlareC2SPacketWrapper.TYPE, ThrowFlareC2SPacketWrapper.STREAM_CODEC,
                new DirectionalPayloadHandler<>(ThrowFlareC2SPacketWrapper::clientHandler, ThrowFlareC2SPacketWrapper::serverHandler)
        );
        registrar.playBidirectional(
                SyncServerSettingsS2CPacket.TYPE, SyncServerSettingsS2CPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(SyncServerSettingsS2CPacket::clientHandler, SyncServerSettingsS2CPacket::serverHandler)
        );
        registrar.playBidirectional(
                SpawnFlareEntityS2CPacket.TYPE, SpawnFlareEntityS2CPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(SpawnFlareEntityS2CPacketWrapper::clientHandler, SpawnFlareEntityS2CPacketWrapper::serverHandler)
        );
        registrar.executesOn(HandlerThread.NETWORK);
        // 以下处理器在网络线程执行
    }

    public static void sendSettingsSyncS2CPacket(ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new SyncServerSettingsS2CPacket(ServerSettings.LOCAL.asJson()));
    }

    public static Packet<ClientGamePacketListener> sendFlareSpawnS2CPacket(SpawnFlareEntityS2CPacket packet)
    {
        try {
            Method method = PacketDistributor.class.getDeclaredMethod("makeClientboundPacket", CustomPacketPayload.class, CustomPacketPayload[].class);
            method.setAccessible(true);
            return (Packet<ClientGamePacketListener>) method.invoke(null, packet, null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            DRGFlares.LOGGER.error("Reflect to get vanilla packet error: " + e.getLocalizedMessage());
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendFlareThrowC2SPacket(FlareColor color)
    {
        PacketDistributor.sendToServer(new ThrowFlareC2SPacketWrapper(color));
        DRGFlarePlayerAspect.clientLocal.reduceFlareCount(Minecraft.getInstance().player);
    }
}