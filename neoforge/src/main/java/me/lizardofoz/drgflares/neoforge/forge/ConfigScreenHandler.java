package me.lizardofoz.drgflares.neoforge.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.Optional;
import java.util.function.BiFunction;

public class ConfigScreenHandler {
    public ConfigScreenHandler() {
    }

    public static Optional<BiFunction<Minecraft, Screen, Screen>> getScreenFactoryFor(IModInfo selectedMod) {
        return ModList.get().getModContainerById(selectedMod.getModId()).flatMap((mc) -> mc.getCustomExtension(ConfigScreenFactory.class).map(ConfigScreenFactory::screenFunction));
    }

    public static record ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) implements IExtensionPoint {
        public ConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) {
            this.screenFunction = screenFunction;
        }

        public BiFunction<Minecraft, Screen, Screen> screenFunction() {
            return this.screenFunction;
        }
    }
}
