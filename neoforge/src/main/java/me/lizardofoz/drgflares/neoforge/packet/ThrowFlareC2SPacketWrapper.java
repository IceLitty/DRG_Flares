package me.lizardofoz.drgflares.neoforge.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
//import me.lizardofoz.inventorio.api.InventorioAPI;
//import me.lizardofoz.inventorio.player.PlayerInventoryAddon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ThrowFlareC2SPacketWrapper implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ThrowFlareC2SPacketWrapper> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("drg_flares", "c2s_throw_flare"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowFlareC2SPacketWrapper> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ThrowFlareC2SPacketWrapper decode(RegistryFriendlyByteBuf buf) {
            FlareColor flareColor = buf.readEnum(FlareColor.class);
            return new ThrowFlareC2SPacketWrapper(flareColor);
        }
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ThrowFlareC2SPacketWrapper message) {
            buf.writeEnum(message.color);
        }
    };

    @Getter
    public final FlareColor color;

    public ThrowFlareC2SPacketWrapper(FlareColor color)
    {
        this.color = color;
    }

    //Receiver's constructor
    public ThrowFlareC2SPacketWrapper(FriendlyByteBuf buf)
    {
        color = FlareColor.byId(buf.readByte());
    }

    public static void clientHandler(final ThrowFlareC2SPacketWrapper message, final IPayloadContext context) {
    }

    public static void serverHandler(final ThrowFlareC2SPacketWrapper message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null)
                return;

            if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
            {
                DRGFlarePlayerAspect playerAspect = DRGFlarePlayerAspect.get(player);
                if (playerAspect != null)
                    playerAspect.tryThrowRegeneratingFlare(player, message.getColor());
            }
            else
            {
                //Here we exploit the fact, that when any tryFlare returns true, the subsequent tryFlare~s never get called
                if (DRGFlaresUtil.tryFlare(player, player.getInventory().offhand) || DRGFlaresUtil.tryFlare(player, player.getInventory().items))
                    return;
//                if (DRGFlareRegistry.getInstance().isInventorioLoaded())
//                {
//                    PlayerInventoryAddon playerInventoryAddon = InventorioAPI.getInventoryAddon();
//                    if (playerInventoryAddon != null)
//                    {
//                        if (DRGFlaresUtil.tryFlare(player, playerInventoryAddon.utilityBelt)
//                                || DRGFlaresUtil.tryFlare(player, playerInventoryAddon.toolBelt)
//                                || DRGFlaresUtil.tryFlare(player, playerInventoryAddon.deepPockets))
//                            return; //Don't remove this, we abuse Java, lol
//                    }
//                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}