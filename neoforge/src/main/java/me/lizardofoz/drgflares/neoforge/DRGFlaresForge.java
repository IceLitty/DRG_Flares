package me.lizardofoz.drgflares.neoforge;

import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.DRGFlares;
import me.lizardofoz.drgflares.client.SettingsScreen;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.neoforge.forge.ConfigScreenHandler;
import me.lizardofoz.drgflares.neoforge.forge.DisplayTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import java.util.Arrays;

@Mod("drg_flares")
public final class DRGFlaresForge extends DRGFlares
{
    public DRGFlaresForge(ModContainer modContainer, IEventBus bus)
    {
        DRGFlareRegistryForge.initialize(modContainer, bus);
        ForgeEvents.initialize(bus);

        if (FMLEnvironment.dist == Dist.CLIENT)
            Client.initialize();
    }

    //Has to be a separate class or else JVM will try to load client-only classes on a dedicated server
    @OnlyIn(Dist.CLIENT)
    private static class Client
    {
        private static void initialize()
        {
            ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> DisplayTest.IGNORESERVERONLY, (a, b) -> true));
            Options options = Minecraft.getInstance().options;
            //Keybinds and integration with Forge's Built-in Mod Menu.
            KeyMapping[] keys = Arrays.copyOf(
                    options.keyMappings,
                    options.keyMappings.length + (DRGFlareRegistry.getInstance().isClothConfigLoaded() ? 2 : 1));
            keys[keys.length - 1] = PlayerSettings.INSTANCE.throwFlareKey;
            if (DRGFlareRegistryForge.getInstance().isClothConfigLoaded())
            {
                ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> SettingsScreen.create(parent)));
                keys[keys.length - 2] = PlayerSettings.INSTANCE.flareModSettingsKey;
            }
            options.keyMappings = keys;
        }
    }
}