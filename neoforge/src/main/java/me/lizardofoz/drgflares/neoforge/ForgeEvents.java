package me.lizardofoz.drgflares.neoforge;

import me.lizardofoz.drgflares.CommonEvents;
import me.lizardofoz.drgflares.client.FlareHUDRenderer;
import me.lizardofoz.drgflares.neoforge.packet.PacketStuff;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "drg_flares")
public class ForgeEvents extends CommonEvents
{
    private static ForgeEvents instance;

    public static void initialize(IEventBus bus)
    {
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();

        instance = new ForgeEvents();
//        bus.register(new ForgeEvents());
        if (FMLEnvironment.dist == Dist.CLIENT)
            new Client();
//            bus.register(new Client());
    }

    private ForgeEvents() { }

    @SubscribeEvent
    public static void onEvent(ServerStartedEvent event)
    {
        instance.onServerStart(event.getServer());
    }

    @SubscribeEvent
    public static void onEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        instance.onPlayerJoinServer((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onEvent(PlayerEvent.PlayerLoggedOutEvent event)
    {
        instance.onPlayerLeaveServer(event.getEntity());
    }

    @SubscribeEvent
    public static void onEvent(ServerTickEvent.Pre event)
    {
        instance.onServerTick();
    }

    @Override
    protected void sendSettingsSyncS2CPacket(ServerPlayer player)
    {
        PacketStuff.sendSettingsSyncS2CPacket(player);
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = "drg_flares", value = Dist.CLIENT)
    private static class Client extends CommonEvents.Client
    {
        private static Client instance;

        private Client()
        {
            instance = this;
        }

        @SubscribeEvent
        public static void onEvent(CustomizeGuiOverlayEvent event)
        {
            FlareHUDRenderer.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }

        @SubscribeEvent
        public static void onEvent(ClientPlayerNetworkEvent.LoggingIn event)
        {
            instance.onClientConnect();
        }

        @SubscribeEvent
        public static void onEvent(ClientPlayerNetworkEvent.LoggingOut event)
        {
            instance.onClientDisconnect();
        }

        @SubscribeEvent
        public static void onEvent(ClientTickEvent.Pre event)
        {
            instance.onClientTick(Minecraft.getInstance());
        }

        @Override
        protected void sendFlareThrowC2SPacket(FlareColor color)
        {
            PacketStuff.sendFlareThrowC2SPacket(color);
        }
    }
}