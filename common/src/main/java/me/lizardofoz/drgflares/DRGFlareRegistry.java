package me.lizardofoz.drgflares;

import lombok.Getter;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

public abstract class DRGFlareRegistry
{
    @Getter protected static DRGFlareRegistry instance;

    protected final ResourceLocation FLARE_THROW = ResourceLocation.parse("drg_flares:flare_throw");
    protected final ResourceLocation FLARE_BOUNCE = ResourceLocation.parse("drg_flares:flare_bounce");
    protected final ResourceLocation FLARE_BOUNCE_FAR = ResourceLocation.parse("drg_flares:flare_bounce_far");

    public final SoundEvent FLARE_THROW_EVENT = SoundEvent.createVariableRangeEvent(FLARE_THROW);
    public final SoundEvent FLARE_BOUNCE_EVENT = SoundEvent.createVariableRangeEvent(FLARE_BOUNCE);
    public final SoundEvent FLARE_BOUNCE_FAR_EVENT = SoundEvent.createFixedRangeEvent(FLARE_BOUNCE_FAR, 64);

    public ServerSyncMode serverSyncMode = ServerSyncMode.UNDEFINED;

    public abstract EntityType<FlareEntity> getFlareEntityType();
    public abstract Map<FlareColor, Item> getFlareItemTypes();
    public abstract Block getLightSourceBlockType();
    public abstract BlockEntityType<FlareLightBlockEntity> getLightSourceBlockEntityType();
    public abstract Packet<ClientGamePacketListener> createSpawnFlareEntityPacket(FlareEntity flareEntity, ServerEntity serverEntity);
    public abstract boolean isClothConfigLoaded();
    public abstract boolean isInventorioLoaded();
    public abstract void broadcastSettingsChange();
    public abstract ResourceKey<CreativeModeTab> getCreativeItemGroup();
}
