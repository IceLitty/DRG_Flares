package me.lizardofoz.drgflares;

import me.lizardofoz.drgflares.block.FlareLightBlock;
import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collection;

public abstract class CommonEvents
{
    protected abstract void sendSettingsSyncS2CPacket(ServerPlayer player);

    protected void onServerStart(MinecraftServer server)
    {
        DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.SYNC_WITH_SERVER;
        DRGFlareLimiter.initOrReset();
        DRGFlarePlayerAspect.initOrReset();
        ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
        FlareLightBlock.refreshBlockStates();
        if (!ServerSettings.CURRENT.flareRecipesInSurvival.value)
        {
            RecipeManager recipeManager = server.getRecipeManager();
            DRGFlaresUtil.removeFlareRecipes(recipeManager);
//            Collection<RecipeHolder<?>> values = server.getRecipeManager().getRecipes();
//            values.removeIf(it -> it.id().getNamespace().equals("drg_flares"));
//            DRGFlaresUtil.setRecipes(server.getRecipeManager(), values);
        }
    }

    protected void onServerTick()
    {
        DRGFlareLimiter.tick();
        DRGFlarePlayerAspect.tickAll();
    }

    protected void onPlayerJoinServer(ServerPlayer player)
    {
        DRGFlareLimiter.onPlayerJoin(player);
        DRGFlarePlayerAspect.onPlayerJoin(player);
        sendSettingsSyncS2CPacket(player);
    }

    protected void onPlayerLeaveServer(Player player)
    {
        DRGFlareLimiter.onPlayerLeave(player);
        DRGFlarePlayerAspect.onPlayerLeave(player);
    }

    @Environment(EnvType.CLIENT)
    protected abstract static class Client
    {
        protected abstract void sendFlareThrowC2SPacket(FlareColor color);

        protected void onClientTick(Minecraft client)
        {
            LocalPlayer player = client.player;
            if (player == null || client.isPaused())
                return;
            DRGFlareLimiter.tick();
            DRGFlarePlayerAspect.clientLocal.tick();

            if (DRGFlareRegistry.getInstance().isClothConfigLoaded() && PlayerSettings.INSTANCE.flareModSettingsKey.consumeClick())
                client.setScreen(SettingsScreen.create(client.screen));

            if (PlayerSettings.INSTANCE.throwFlareKey.consumeClick() && !DRGFlaresUtil.isRegenFlareOnCooldown(player))
            {
                if (DRGFlarePlayerAspect.clientLocal.checkFlareToss(player))
                {
                    FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, true);
                    if (DRGFlareRegistry.getInstance().serverSyncMode != ServerSyncMode.SYNC_WITH_SERVER && ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                        DRGFlarePlayerAspect.clientLocal.tryThrowRegeneratingFlare(Minecraft.getInstance().player, flareColor);
                    else
                        sendFlareThrowC2SPacket(flareColor);
                }
                else if (ServerSettings.CURRENT.regeneratingFlaresEnabled.value)
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), PlayerSettings.INSTANCE.flareSoundVolume.value / 1234f, 1.7f);
            }
        }

        protected void onClientConnect()
        {
            if (DRGFlareRegistry.getInstance().serverSyncMode == ServerSyncMode.UNDEFINED)
            {
                DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.CLIENT_ONLY;
                FlareLightBlock.refreshBlockStates();
                ServerSettings.CURRENT.loadFromJson(ServerSettings.LOCAL.asJson());
            }
        }

        protected void onClientDisconnect()
        {
            DRGFlareRegistry.getInstance().serverSyncMode = ServerSyncMode.UNDEFINED;
        }
    }
}