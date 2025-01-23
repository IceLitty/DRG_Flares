package me.lizardofoz.drgflares.neoforge.forge;

import net.neoforged.fml.IExtensionPoint;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

@SuppressWarnings("JavadocReference") // reference to NetworkConstants, ForgeHooksClient
public record DisplayTest(Supplier<String> suppliedVersion, BiPredicate<String, Boolean> remoteVersionTest) implements IExtensionPoint {
    public static final String IGNORESERVERONLY = "OHNOES\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31";
}