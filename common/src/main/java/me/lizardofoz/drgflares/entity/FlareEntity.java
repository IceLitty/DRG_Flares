package me.lizardofoz.drgflares.entity;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlareLimiter;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import me.lizardofoz.drgflares.util.ServerSyncMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FlareEntity extends ThrowableProjectile
{
    //Persistent Entity Data
    public FlareColor color;
    public int lifespan = -1;

    //Temporary/Service Entity Data
    public int bounceCount = 1;
    public float rotation = 0;
    private int idleTicks = 0;
    private float prevPartialTick = 0;

    private BlockPos lightBlockPos = null;
    private BlockPos lastHitBlockPos = null;
    private BlockState lastHitBlockState = null;

    public FlareEntity(Level world, FlareColor color)
    {
        super(DRGFlareRegistry.getInstance().getFlareEntityType(), world);
        this.color = FlareColor.RandomColorPicker.unwrapRandom(color, false);
        //Technically, we shouldn't receive "Random" as a Flare Color here, but just in case...
    }

    private FlareEntity(LivingEntity owner, FlareColor color)
    {
        super(DRGFlareRegistry.getInstance().getFlareEntityType(), owner, owner.level());
        this.color = FlareColor.RandomColorPicker.unwrapRandom(color, false);
        //Technically, we shouldn't receive "Random" as a Flare Color here, but just in case...
    }

    //Note: we call this only on the server's side
    //UPD: if this called on the client's side, we know it has to be in the client-side-only mode
    public static FlareEntity throwFlare(@NotNull LivingEntity owner, FlareColor color)
    {
        float throwAngle = ServerSettings.CURRENT.flareThrowAngle.value;
        float pitchModifier = Math.min(throwAngle, Math.max(0, owner.getXRot() + (95 - throwAngle)));

        FlareEntity flareEntity = new FlareEntity(owner, color);
        flareEntity.setPos(flareEntity.getX(), flareEntity.getY() - 0.5, flareEntity.getZ());
        flareEntity.shootFromRotation(owner, owner.getXRot() - pitchModifier, owner.getYRot(), 0, 0.75f * ServerSettings.CURRENT.flareThrowSpeed.value, 1);
        if (!owner.level().isClientSide())
            owner.level().addFreshEntity(flareEntity);
        else
        {
            //EntityId magic to avoid clashing with entityId-s of real entities
            int randomNegativeId = flareEntity.getId() - 100000;
            while (owner.level().getEntity(randomNegativeId) != null)
                randomNegativeId = owner.level().getRandom().nextInt(1000000) - 2000000;
            flareEntity.setId(randomNegativeId);
            DRGFlaresUtil.addEntityOnClient(owner.level(), flareEntity);
        }
        return flareEntity;
    }

    public static FlareEntity make(EntityType<FlareEntity> entityType, Level world)
    {
        return new FlareEntity(world, FlareColor.RED);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag tag)
    {
        super.saveWithoutId(tag);
        tag.putInt("lifespan", lifespan);
        tag.putShort("color", (short) color.id);
        return tag;
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        lifespan = tag.getInt("lifespan");
        color = FlareColor.byId(tag.getShort("color"));
    }

    @Override
    public boolean isAttackable()
    {
        return false;
    }

    /**
     * getGravity() is final
     */
    @Override
    protected void applyGravity()
    {
        float d = 0.024f * ServerSettings.CURRENT.flareGravity.value;
        if (d != (double)0.0F) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)0.0F, -d, (double)0.0F));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity)
    {
        return DRGFlareRegistry.getInstance().createSpawnFlareEntityPacket(this, entity);
    }

    @Environment(EnvType.CLIENT)
    public void frame(float partialTick)
    {
        if (Minecraft.getInstance().isPaused())
            return;
        while (partialTick <= prevPartialTick)
            prevPartialTick -=1;
        float delta = partialTick - prevPartialTick;
        prevPartialTick = partialTick;

        if (getDeltaMovement().lengthSqr() < 0.1 && bounceCount > 2)
            rotation = 0;
        else
            rotation += 10.0f * delta / bounceCount;
    }

    public boolean isLit()
    {
        return lifespan < (ServerSettings.CURRENT.secondsUntilDimmingOut.value + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value) * 20;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);
        BlockPos hitBlockPos = blockHitResult.getBlockPos();

        bounceCount++;
        if (level().isClientSide() && !isInWater())
        {
            float pitch = 1.1f + level().random.nextFloat() * 0.3f;
            float volume = PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f;
            float farVolume = volume < 0.25f ? volume * 3 : volume < 0.5f ? volume * 2 : volume;
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_BOUNCE_EVENT, SoundSource.MASTER, volume, pitch);
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_BOUNCE_FAR_EVENT, SoundSource.MASTER, farVolume * 3, pitch);
        }

        //If we don't disable gravity, a flare will just bob up and down when laying on the ground
        if (hitBlockPos.equals(lastHitBlockPos) && getDeltaMovement().lengthSqr() < 0.01)
        {
            setDeltaMovement(Vec3.ZERO);
            setNoGravity(true);
            return;
        }
        else
        {
            lastHitBlockPos = hitBlockPos;
            lastHitBlockState = level().getBlockState(hitBlockPos);
        }

        double speedDivider = ServerSettings.CURRENT.flareSpeedBounceDivider.value;
        Vec3 velocity = getDeltaMovement();
        if (blockHitResult.getDirection() == Direction.EAST || blockHitResult.getDirection() == Direction.WEST)
            setDeltaMovement(-velocity.x / speedDivider, velocity.y / speedDivider, velocity.z / speedDivider);
        else if (blockHitResult.getDirection() == Direction.UP || blockHitResult.getDirection() == Direction.DOWN)
            setDeltaMovement(velocity.x / speedDivider, -velocity.y / speedDivider, velocity.z / speedDivider);
        else
            setDeltaMovement(velocity.x / speedDivider, velocity.y / speedDivider, -velocity.z / speedDivider);
    }

    @Override
    public void tick()
    {
        int ticksUntilDespawn = ServerSettings.CURRENT.secondsUntilDimmingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilFizzlingOut.value
                + ServerSettings.CURRENT.andThenSecondsUntilDespawn.value;

        if (++lifespan == 0 && level().isClientSide())
            DRGFlaresUtil.playSoundFromEntityOnClient(this, DRGFlareRegistry.getInstance().FLARE_THROW_EVENT, SoundSource.MASTER, PlayerSettings.INSTANCE.flareSoundVolume.value / 100.0f, 1);

        if (lifespan > ticksUntilDespawn * 20 || isInLava() || getY() <= DRGFlaresUtil.getVoidDamageLevel(level()))
            kill();

        if (!level().isClientSide() || DRGFlareRegistry.getInstance().serverSyncMode == ServerSyncMode.CLIENT_ONLY)
            DRGFlareLimiter.reportFlare(this);
        else
            frame(0);

        int idleOpt = ServerSettings.CURRENT.secondsUntilIdlingFlareGetsOptimized.value * 20;
        if (getDeltaMovement().lengthSqr() < 0.01 && bounceCount > 2)
            idleTicks++;
        else
            idleTicks = 0;
        if (idleOpt <= 0 || idleTicks < idleOpt)
            super.tick();

        if (isInWater())
        {
            idleTicks = 0;
            bounceCount = 10;              //Stop the flare from spinning in water
            addDeltaMovement(new Vec3(0, 0.04, 0));       //Make it float in water
        }

        if (idleTicks == 20)
            lightBlockPos = null;

        //If the block bellow the flare has changed or it's in water, then re-enable gravity
        boolean isInsideWaterBlock = level().isWaterAt(blockPosition());
        if (isNoGravity() && (isInsideWaterBlock || !getBlockStateOnLegacy().equals(lastHitBlockState)))
        {
            setNoGravity(false);
            idleTicks = 0;
        }

        //If the Flare has fizzled out, we skip the bottom part which is responsible for lighting things up
        //
        //Here's a trick - we don't need the light source from the flare to exist on the server side.
        //While it COULD be useful for temporal mob-proofing, the upsides,
        //  such as server performance, not firing observers and not interfering with the flow of liquids, are far more important.
        //However, there's an option to enable server-side light sources anyway
        if (isLit() && (level().isClientSide() || ServerSettings.CURRENT.serverSideLightSources.value))
            spawnLightSource(isInsideWaterBlock);
    }

    private void spawnLightSource(boolean isInWaterBlock)
    {
        try
        {
            //This bit is kinda ugly, but there's a set of edge cases when light sources get replaces with actual blocks
            //And a flare losing any place it can place a light source at
            if (lightBlockPos == null)
            {
                lightBlockPos = findFreeSpace(level(), blockPosition(), ServerSettings.CURRENT.lightSourceSearchDistance.value);
                if (lightBlockPos == null)
                    return;

                BlockEntity blockEntity = level().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                else if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                else
                    level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
            }
            else if (checkDistance(lightBlockPos, blockPosition(), ServerSettings.CURRENT.lightSourceRefreshDistance.value))
            {
                //Because a flare moves slightly in water, it used to create an edge case when a light source would rapidly flash on and off.
                //By having an old light source survive for longer than it takes to spawn a new one, we make sure the flicker doesn't happen
                BlockEntity blockEntity = level().getBlockEntity(lightBlockPos);
                if (blockEntity instanceof FlareLightBlockEntity)
                {
                    int lightLevel = FlareLightBlock.getLightLevel(level(), lightBlockPos);
                    if (lifespan < ServerSettings.CURRENT.secondsUntilDimmingOut.value * 20)
                    {
                        if (lightLevel != ServerSettings.CURRENT.fullBrightnessLightLevel.value)
                            level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getFullBrightnessBlockState());
                    }
                    else if (lightLevel != ServerSettings.CURRENT.dimmedLightLevel.value)
                        level().setBlockAndUpdate(lightBlockPos, FlareLightBlock.getDimmedOutBlockState());
                    ((FlareLightBlockEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
                }
                else
                    lightBlockPos = null;
            }
            else
                lightBlockPos = null;
        }
        catch (Throwable e)
        {
            DRGFlares.LOGGER.error("Failed to process a light source block for " + this+" -> "+lightBlockPos, e);
        }
    }

    private boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance)
    {
        return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance
                && Math.abs(blockPosA.getY() - blockPosB.getY()) <= distance
                && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= distance;
    }

    private BlockPos findFreeSpace(Level world, BlockPos blockPos, int maxDistance)
    {
        if (blockPos == null)
            return null;

        //We want to find a valid position for a light source as close as possible to the flare
        //Which means we want to iterate from 0;0;0 offset and move outside, rather than x=-2..2; y=-2..2; z=-2..2
        int[] offsets = new int[maxDistance * 2 + 1];
        offsets[0] = 0;
        for (int i = 2; i <= maxDistance * 2; i += 2)
        {
            offsets[i - 1] = i / 2;
            offsets[i] = -i / 2;
        }
        for (int x : offsets)
            for (int y : offsets)
                for (int z : offsets)
                {
                    BlockPos offsetPos = blockPos.offset(x, y, z);
                    BlockState state = world.getBlockState(offsetPos);
                    if (state.isAir() || state.getBlock().equals(DRGFlareRegistry.getInstance().getLightSourceBlockType()))
                        return offsetPos;
                }

        return null;
    }
}