package me.lizardofoz.drgflares.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.ServerSettings;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

@SuppressWarnings("deprecation")
public class FlareLightBlock extends BaseEntityBlock
{
    public static final MapCodec<FlareLightBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockBehaviour.propertiesCodec()
    ).apply(instance, FlareLightBlock::new));

    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;

    @Getter private static BlockState fullBrightnessBlockState;
    @Getter private static BlockState dimmedOutBlockState;

    public static void refreshBlockStates()
    {
        BlockState defaultState = DRGFlareRegistry.getInstance().getLightSourceBlockType().defaultBlockState();
        fullBrightnessBlockState = defaultState.setValue(LIGHT_LEVEL, ServerSettings.CURRENT.fullBrightnessLightLevel.value);
        dimmedOutBlockState = defaultState.setValue(LIGHT_LEVEL, ServerSettings.CURRENT.dimmedLightLevel.value);
    }

    public static int getLightLevel(Level world, BlockPos blockPos)
    {
        try
        {
            return world.getBlockState(blockPos).getValue(FlareLightBlock.LIGHT_LEVEL);
        }
        catch (Throwable e)
        {
            return 0;
        }
    }

    public FlareLightBlock(BlockBehaviour.Properties settings)
    {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FlareLightBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos)
    {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return world.isClientSide() || ServerSettings.CURRENT.serverSideLightSources.value
                ? createTickerHelper(type, DRGFlareRegistry.getInstance().getLightSourceBlockEntityType(), FlareLightBlockEntity::staticTick)
                : null;
    }
}