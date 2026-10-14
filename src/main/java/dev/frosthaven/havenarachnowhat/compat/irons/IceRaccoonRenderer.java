package dev.frosthaven.havenarachnowhat.compat.irons;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.frosthaven.havenarachnowhat.RaccoonEyesLayer;
import dev.frosthaven.havenarachnowhat.RaccoonModel;
import dev.frosthaven.havenarachnowhat.Raccoons;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** Draws Iron's Ice Spider as the Angry Ice Raccoon, over about the area its legs cover. Like Iron's renderer, the whole
 *  body turns from straight up to the spider's surface normal, so it lies flat on walls and ceilings it climbs. */
final class IceRaccoonRenderer extends MobRenderer<IceSpiderEntity, IceRaccoonModel> {
    private static final float SCALE = 2.6F;
    private final IceSpiderRenderer original;

    IceRaccoonRenderer(EntityRendererProvider.Context ctx, IceSpiderRenderer original) {
        super(ctx, new IceRaccoonModel(ctx.bakeLayer(RaccoonModel.LAYER)), 1.6F);
        this.original = original;
        addLayer(new RaccoonEyesLayer<>(this, Raccoons.Skin.ICE.eyes));
    }

    @Override
    public void render(IceSpiderEntity e, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!Raccoons.enabled()) {
            original.render(e, yaw, partialTick, pose, buffers, light);
            return;
        }
        pose.pushPose();
        if (e.normal != null && e.lastNormal != null) {
            Vec3 n = e.lastNormal.lerp(e.normal, partialTick);
            pose.mulPose(new Quaternionf().rotationTo(0F, 1F, 0F, (float) n.x, (float) n.y, (float) n.z));
        }
        super.render(e, yaw, partialTick, pose, buffers, light);
        pose.popPose();
    }

    @Override
    public boolean shouldRender(IceSpiderEntity e, Frustum frustum, double x, double y, double z) {
        if (!Raccoons.enabled()) return original.shouldRender(e, frustum, x, y, z);
        return e.shouldRender(x, y, z) && frustum.isVisible(e.getBoundingBoxForCulling().inflate(3.5));   // far wider than the 1x1 hitbox
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpiderEntity e) {
        return Raccoons.Skin.ICE.texture;
    }

    @Override
    protected void scale(IceSpiderEntity e, PoseStack pose, float partialTick) {
        pose.scale(SCALE, SCALE, SCALE);
    }
}
