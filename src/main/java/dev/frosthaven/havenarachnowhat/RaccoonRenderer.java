package dev.frosthaven.havenarachnowhat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;

/** Draws a Spider or Cave Spider as an Angry Raccoon at about the spider's size; with replacing off ({@link Raccoons#enabled}) it
 *  hands the entity to the original renderer. Climbing tips the body nose-up against the wall (front pivot). */
public final class RaccoonRenderer<T extends Spider> extends MobRenderer<T, RaccoonModel<T>> {
    /** Model size 1.0 is about 0.6 wide, 1.0 tall (ears) and 1.6 long. */
    public enum Kind {
        SPIDER(Raccoons.Skin.ANGRY, 1.15F, 0.8F),   // spider hitbox 1.4 x 0.9
        CAVE(Raccoons.Skin.CAVE, 0.6F, 0.45F);      // cave spider hitbox 0.7 x 0.5

        final Raccoons.Skin skin;
        final float scale, shadow;

        Kind(Raccoons.Skin skin, float scale, float shadow) {
            this.skin = skin;
            this.scale = scale;
            this.shadow = shadow;
        }
    }

    private final EntityRenderer<? super T> original;
    private final Kind kind;
    private final Map<T, float[]> climb = new WeakHashMap<>();

    public RaccoonRenderer(EntityRendererProvider.Context ctx, EntityRenderer<? super T> original, Kind kind) {
        super(ctx, new RaccoonModel<>(ctx.bakeLayer(RaccoonModel.LAYER)), kind.shadow);
        this.original = original;
        this.kind = kind;
        addLayer(new RaccoonEyesLayer<>(this, kind.skin.eyes));
    }

    @Override
    public void render(T e, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (Raccoons.enabled()) super.render(e, yaw, partialTick, pose, buffers, light);
        else original.render(e, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public boolean shouldRender(T e, Frustum frustum, double x, double y, double z) {
        return Raccoons.enabled() ? super.shouldRender(e, frustum, x, y, z) : original.shouldRender(e, frustum, x, y, z);
    }

    @Override
    public ResourceLocation getTextureLocation(T e) {
        return kind.skin.texture;
    }

    @Override
    protected float getFlipDegrees(T e) {
        return 180F;
    }

    @Override
    protected void scale(T e, PoseStack pose, float partialTick) {
        pose.scale(kind.scale, kind.scale, kind.scale);
    }

    @Override
    protected void setupRotations(T e, PoseStack pose, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(e, pose, bob, yBodyRot, partialTick, scale);
        float k = climbAmount(e);
        if (k > 0.001F) {   // nose up the wall, belly against it
            float r = 0.6F * kind.scale;
            pose.translate(0, 0, -r);
            pose.mulPose(Axis.XP.rotationDegrees(90F * k));
            pose.translate(0, 0, r);
        }
    }

    /** 0 on the ground, 1 climbing; eased per frame so the body swings up instead of snapping. */
    private float climbAmount(T e) {
        float[] v = climb.computeIfAbsent(e, k -> new float[1]);
        float dt = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        v[0] += ((e.isClimbing() ? 1F : 0F) - v[0]) * Math.min(1F, dt * 0.35F);
        return v[0];
    }
}
