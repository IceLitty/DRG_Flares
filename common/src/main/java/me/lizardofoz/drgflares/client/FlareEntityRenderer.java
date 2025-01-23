package me.lizardofoz.drgflares.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.lizardofoz.drgflares.entity.FlareEntity;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FlareEntityRenderer extends EntityRenderer<FlareEntity>
{
    private final Map<FlareColor, ResourceLocation> TEXTURES = new HashMap<>();
    private final int MAX_LIGHT = LightTexture.pack(15, 15);

    private final ModelPart rodModel;
    private final ModelPart metalModel;

    public FlareEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context);

        //There reason to have 2 models is because the rod itself remains glowing in dark
        rodModel = new MeshDefinition().getRoot()
                .addOrReplaceChild("rod", CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-1, -9, -1, 2, 13, 2), PartPose.ZERO)
                .bake(32, 32);

        PartDefinition metalModelBuilder = new MeshDefinition().getRoot();
        metalModelBuilder.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 6)
                .addBox(-2, -8, -2, 4, 2, 4), PartPose.ZERO);
        metalModelBuilder.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2, 1, -2, 4, 2, 4), PartPose.ZERO);
        metalModel = metalModelBuilder.bake(32, 32);

        for (FlareColor color : FlareColor.colors)
            TEXTURES.put(color, ResourceLocation.fromNamespaceAndPath("drg_flares", "textures/block/drg_flare_" + color.toString() + ".png"));
    }

    @Override
    public void render(FlareEntity entity, float yaw, float subTickTime, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
        super.render(entity, yaw, subTickTime, matrices, vertexConsumers, light);
        Vec3 velocity = entity.getDeltaMovement().scale(10);

        entity.frame(subTickTime);

        matrices.pushPose();
        matrices.translate(0, 0.1f, 0);
        //Here's a trick - we want each flare to end up with a different rotation when laying on the floor.
        //We could use random.setSeed(entId), but this works as good as that, but much faster
        matrices.mulPose(Axis.YP.rotationDegrees(entity.getId() * 119));
        matrices.mulPose(Axis.XP.rotationDegrees(entity.rotation));
        matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin((float) (velocity.x + 90) / 15) * 360));
        matrices.mulPose(Axis.YP.rotationDegrees(Mth.cos((float) (velocity.y + velocity.x * 200) / 15) * 360));
        matrices.scale(0.6f, 0.6f, 0.6f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(TEXTURES.get(entity.color)));
        rodModel.render(matrices, vertexConsumer, MAX_LIGHT, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1, 1, 1, 1));
        metalModel.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1, 1, 1, 1));
        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FlareEntity entity)
    {
        return TEXTURES.get(entity.color);
    }
}