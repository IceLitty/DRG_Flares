package me.lizardofoz.drgflares.block;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class FlareLightBlockEntity extends BlockEntity
{
    private int lifespan = 0;

    public FlareLightBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(DRGFlareRegistry.getInstance().getLightSourceBlockEntityType(), blockPos, blockState);
    }

    public void refresh(int lifeExtension)
    {
        lifespan = -lifeExtension;
    }

    private void tick()
    {
        if (lifespan++ >= ServerSettings.CURRENT.lightSourceLifespanTicks.value)
        {
            if (level.getBlockState(getBlockPos()).getBlock() instanceof FlareLightBlock)
                level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
            else
                setRemoved();
        }
    }

    public static void staticTick(Level world, BlockPos blockPos, BlockState blockState, FlareLightBlockEntity blockEntity)
    {
        blockEntity.tick();
    }
}