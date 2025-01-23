package me.lizardofoz.drgflares.packet;

import lombok.Getter;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

@Getter
public class SpawnFlareEntityS2CPacket implements CustomPacketPayload
{
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath("drg_flares", "spawn_flare");
    public static final CustomPacketPayload.Type<SpawnFlareEntityS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(IDENTIFIER);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnFlareEntityS2CPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SpawnFlareEntityS2CPacket decode(RegistryFriendlyByteBuf buf) {
            int id = buf.readVarInt();
            UUID uuid = buf.readUUID();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            int vX = buf.readVarInt();
            int vY = buf.readVarInt();
            int vZ = buf.readVarInt();
            int lifespan = buf.readVarInt();
            FlareColor flareColor = buf.readEnum(FlareColor.class);
            int bounceCount = buf.readVarInt();
            return new SpawnFlareEntityS2CPacket(id, uuid, x, y, z, new Vec3(vX, vY, vZ), lifespan, flareColor, bounceCount);
        }
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SpawnFlareEntityS2CPacket message) {
            buf.writeVarInt(message.id);
            buf.writeUUID(message.uuid);
            buf.writeDouble(message.x);
            buf.writeDouble(message.y);
            buf.writeDouble(message.z);
            buf.writeVarInt(message.velocityX);
            buf.writeVarInt(message.velocityY);
            buf.writeVarInt(message.velocityZ);
            buf.writeVarInt(message.lifespan);
            buf.writeEnum(message.color);
            buf.writeVarInt(message.bounceCount);
        }
    };

    private int id;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private int lifespan;
    private FlareColor color;
    private int bounceCount;

    public SpawnFlareEntityS2CPacket()
    {
    }

    public SpawnFlareEntityS2CPacket(int id, UUID uuid, double x, double y, double z, Vec3 velocity, int lifespan, FlareColor color, int bounceCount)
    {
        this.id = id;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = (int) (Mth.clamp(velocity.x, -3.9, 3.9) * 8000);
        this.velocityY = (int) (Mth.clamp(velocity.y, -3.9, 3.9) * 8000);
        this.velocityZ = (int) (Mth.clamp(velocity.z, -3.9, 3.9) * 8000);
        this.lifespan = lifespan;
        this.color = color;
        this.bounceCount = bounceCount;
    }

    public SpawnFlareEntityS2CPacket(FlareEntity entity, ServerEntity serverEntity)
    {
        this(entity.getId(), entity.getUUID(), serverEntity.getPositionBase().x(), serverEntity.getPositionBase().y(), serverEntity.getPositionBase().z(), serverEntity.getLastSentMovement(), entity.lifespan, entity.color, entity.bounceCount);
    }

    public void write(FriendlyByteBuf buf)
    {
        try
        {
            buf.writeVarInt(this.id);
            buf.writeUUID(this.uuid);
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeShort(this.velocityX);
            buf.writeShort(this.velocityY);
            buf.writeShort(this.velocityZ);
            buf.writeShort(this.lifespan);
            buf.writeByte(this.color.id);
            buf.writeByte(this.bounceCount);
        }
        catch (Exception e)
        {
            DRGFlares.LOGGER.error("Failed to write SpawnFlareEntityS2CPacket", e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}