package me.lizardofoz.drgflares.neoforge.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncServerSettingsS2CPacket implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncServerSettingsS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("drg_flares", "s2c_sync_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerSettingsS2CPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncServerSettingsS2CPacket decode(RegistryFriendlyByteBuf buf) {
            JsonObject json = new Gson().fromJson(buf.readUtf(), JsonObject.class);
            return new SyncServerSettingsS2CPacket(json);
        }
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncServerSettingsS2CPacket message) {
            buf.writeUtf(message.settings.toString());
        }
    };

    public final JsonObject settings;

    public SyncServerSettingsS2CPacket(JsonObject settings)
    {
        this.settings = settings;
    }

    public static void clientHandler(final SyncServerSettingsS2CPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
            ServerSettings.CURRENT.loadFromJson(message.settings);
            FlareLightBlock.refreshBlockStates();
        });
    }

    public static void serverHandler(final SyncServerSettingsS2CPacket message, final IPayloadContext context) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}