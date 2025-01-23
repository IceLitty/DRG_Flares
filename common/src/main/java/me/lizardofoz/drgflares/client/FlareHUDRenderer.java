package me.lizardofoz.drgflares.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FlareHUDRenderer
{
    private static final ResourceLocation HUD_TEXTURE = ResourceLocation.fromNamespaceAndPath("drg_flares", "textures/gui/hud.png");
    private static final Minecraft client = Minecraft.getInstance();

    public static void render(GuiGraphics drawContext, float tickDelta)
    {
        if (!ServerSettings.CURRENT.regeneratingFlaresEnabled.value || client.player == null || client.player.isSpectator())
            return;

        int widgetX = (int) (client.getWindow().getGuiScaledWidth() * PlayerSettings.INSTANCE.flareUISlotX.value);
        int widgetY = (int) (client.getWindow().getGuiScaledHeight() * PlayerSettings.INSTANCE.flareUISlotY.value) - 19;
        Component keyHintLabel = PlayerSettings.INSTANCE.throwFlareKey.getTranslatedKeyMessage();
        boolean shouldRenderKeybindHint = PlayerSettings.INSTANCE.flareButtonHint.value && keyHintLabel.getString().length() == 1;
        FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, false);
        ItemStack flareDisplayStack = new ItemStack(DRGFlareRegistry.getInstance().getFlareItemTypes().get(flareColor));
        Tesselator bufferBuilder = Tesselator.getInstance();

        //Frame
        RenderSystem.enableBlend();
        drawContext.blit(HUD_TEXTURE, widgetX - 3, widgetY - 3, -200, 0, 0, 22, 22, 32, 32);  //Frame
        if (shouldRenderKeybindHint)
            drawContext.blit(HUD_TEXTURE, widgetX + 12, widgetY - 6, -200, 22, 0, 10, 10, 32, 32); //Keybind hint bcg

        drawContext.renderItem(flareDisplayStack, widgetX, widgetY, 0, -170);

        if (!DRGFlaresUtil.hasUnlimitedRegeneratingFlares(client.player))
        {
            //Progress Bar
            int count = DRGFlarePlayerAspect.clientLocal.getFlaresLeft();
            int currentRegenStatus = DRGFlarePlayerAspect.clientLocal.getFlareRegenStatus();
            int regenBarMaxValue = ServerSettings.CURRENT.regeneratingFlaresRechargeTime.value * 20;
            if (count < ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value)
            {
                float h = Math.max(0.0F, currentRegenStatus / (float) regenBarMaxValue);
                int i = Math.round(currentRegenStatus * 12.0F / regenBarMaxValue);
                int j = Mth.hsvToRgb(h / 3, 1, 1);
                renderGuiQuad(bufferBuilder, widgetX + 1, widgetY + 2, 2, 13, 0, 0, 0, 0);
                renderGuiQuad(bufferBuilder, widgetX + 1, widgetY + 14 - i, 1, i, 111, j >> 16 & 255, j >> 8 & 255, j & 255);
            }

            //Amount Text
            String countText = String.valueOf(count);
            client.font.drawInBatch(countText, (float) (widgetX + 19 - 2 - client.font.width(countText)), (float) (widgetY + 6 + 3), 16777215, true, drawContext.pose().last().pose(), drawContext.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        }

        //Keybind Hint Text
        if (shouldRenderKeybindHint)
        {
            drawContext.pose().pushPose();
            drawContext.pose().scale(0.7f, 0.7f, 0.7f);
            client.font.drawInBatch(keyHintLabel, (float) (widgetX + 15) / 0.7f, (float) (widgetY - 4) / 0.7f, 16777215, true, drawContext.pose().last().pose(), drawContext.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            drawContext.pose().popPose();
        }
    }


    private static void renderGuiQuad(Tesselator tesselator, int x, int y, int width, int height, int z, int red, int green, int blue)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(x, y, z).setColor(red, green, blue, 255);
        buffer.addVertex(x, y + height, z).setColor(red, green, blue, 255);
        buffer.addVertex(x + width, y + height, z).setColor(red, green, blue, 255);
        buffer.addVertex(x + width, y, z).setColor(red, green, blue, 255);
        BufferUploader.draw(buffer.build());
    }
}