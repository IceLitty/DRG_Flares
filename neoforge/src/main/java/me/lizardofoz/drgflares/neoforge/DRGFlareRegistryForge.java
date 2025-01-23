package me.lizardofoz.drgflares.neoforge;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.block.FlareLightBlockEntity;
import me.lizardofoz.drgflares.client.FlareEntityRenderer;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.item.FlareDispenserBehavior;
import me.lizardofoz.drgflares.item.FlareItem;
import me.lizardofoz.drgflares.neoforge.packet.PacketStuff;
import me.lizardofoz.drgflares.packet.SpawnFlareEntityS2CPacket;
import me.lizardofoz.drgflares.util.FlareColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import java.util.HashMap;
import java.util.Map;

public class DRGFlareRegistryForge extends DRGFlareRegistry
{
    @Getter private EntityType<FlareEntity> flareEntityType;
    @Getter private Map<FlareColor, Item> flareItemTypes;
    @Getter private Block lightSourceBlockType;
    @Getter private BlockEntityType<FlareLightBlockEntity> lightSourceBlockEntityType;
    @Getter private final ResourceKey<CreativeModeTab> creativeItemGroup = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("drg_flares", "drg_flares"));

    @Getter(lazy = true) private final boolean isClothConfigLoaded = ModList.get().isLoaded("cloth_config"); //Come on, why did it have to change the mod id?
    @Getter(lazy = true) private final boolean isInventorioLoaded = ModList.get().isLoaded("inventorio");

    public static void initialize(ModContainer modContainer, IEventBus bus)
    {
        DRGFlareRegistry.instance = new DRGFlareRegistryForge();
        ModLoadingContext.get().getActiveContainer().getEventBus().register(DRGFlareRegistry.instance);
    }

    private DRGFlareRegistryForge()
    {
    }

    @SubscribeEvent
    public void forgePleaseStopChangingYourAPI(RegisterEvent event)
    {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, creativeItemGroup,
                CreativeModeTab.builder()
                        .icon(() -> new ItemStack(flareItemTypes.get(FlareColor.MAGENTA)))
                        .title(Component.translatable("drg_flares.creative_group"))
                        .build());

        event.register(BuiltInRegistries.ITEM.key(), helper -> {
            Map<FlareColor, Item> flares = new HashMap<>();

            for (FlareColor color : FlareColor.values())
            {
                Item flareItem = new FlareItem(new Item.Properties());
                helper.register(ResourceLocation.fromNamespaceAndPath("drg_flares", "drg_flare_" + color.toString()), flareItem);
                flares.put(color, flareItem);
            }

            flareItemTypes = ImmutableMap.copyOf(flares);
            FlareDispenserBehavior.initialize();
        });

        event.register(BuiltInRegistries.ENTITY_TYPE.key(), helper -> {
            flareEntityType = EntityType.Builder.of(FlareEntity::make, MobCategory.MISC)
                    .sized(0.4f, 0.2f)
                    .setTrackingRange(64)
                    .canSpawnFarFromPlayer()
                    .setUpdateInterval(1)
                    .build("drg_flares:drg_flare");
            helper.register(ResourceLocation.fromNamespaceAndPath("drg_flares", "drg_flare"), flareEntityType);
        });

        event.register(BuiltInRegistries.BLOCK.key(), helper -> {
            lightSourceBlockType = new FlareLightBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .replaceable()
                            .forceSolidOff()
                            .sound(SoundType.WOOD)
                            .strength(3600000.8F)
                            .noLootTable()
                            .noOcclusion()
                            .lightLevel((state) -> state.getValue(FlareLightBlock.LIGHT_LEVEL)));
            helper.register(ResourceLocation.fromNamespaceAndPath("drg_flares", "flare_light_block"), lightSourceBlockType);
        });

        event.register(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), helper -> {
            lightSourceBlockEntityType = BlockEntityType.Builder.of(FlareLightBlockEntity::new, lightSourceBlockType).build(null);
            helper.register(ResourceLocation.fromNamespaceAndPath("drg_flares", "flare_light_block_entity"), lightSourceBlockEntityType);
        });

        event.register(BuiltInRegistries.SOUND_EVENT.key(), helper -> {
            helper.register(FLARE_THROW, FLARE_THROW_EVENT);
            helper.register(FLARE_BOUNCE, FLARE_BOUNCE_EVENT);
            helper.register(FLARE_BOUNCE_FAR, FLARE_BOUNCE_FAR_EVENT);
        });
    }

    @SubscribeEvent
    public void registerCreativeTabItems(BuildCreativeModeTabContentsEvent event)
    {
        if (creativeItemGroup.equals(event.getTabKey()))
            for (Map.Entry<FlareColor, Item> entry : flareItemTypes.entrySet())
                if (entry.getKey() != FlareColor.RANDOM && entry.getKey() != FlareColor.RANDOM_BRIGHT_ONLY)
                    event.accept(new ItemStack(entry.getValue()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DRGFlareRegistryForge.instance.getFlareEntityType(), FlareEntityRenderer::new);
    }

    @Override
    public Packet<ClientGamePacketListener> createSpawnFlareEntityPacket(FlareEntity flareEntity, ServerEntity serverEntity)
    {
        SpawnFlareEntityS2CPacket packet = new SpawnFlareEntityS2CPacket(flareEntity, serverEntity);
        FriendlyByteBuf buf = new FriendlyByteBuf(PooledByteBufAllocator.DEFAULT.buffer());
        packet.write(buf);
        return PacketStuff.sendFlareSpawnS2CPacket(packet);
    }

    @Override
    public void broadcastSettingsChange()
    {
        try
        {
            for (ServerPlayer serverPlayerEntity : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                PacketStuff.sendSettingsSyncS2CPacket(serverPlayerEntity);
        }
        catch (Throwable ignored) { }
    }
}