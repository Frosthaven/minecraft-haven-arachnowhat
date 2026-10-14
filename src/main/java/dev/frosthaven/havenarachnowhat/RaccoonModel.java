package dev.frosthaven.havenarachnowhat;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;

/**
 * The Angry Raccoon: our own quadruped (body; head with snout, jaw and ears; ringed tail; four legs) in its own model
 * layer, so entity model packs never touch it. Texture layout: tools/textures.py. Animated from the mob's state: walk,
 * head look, bite on the attack swing, leap pose while airborne. Climbing is a whole-body rotation in the renderer;
 * hurt and death come from the living renderer. {@code IceRaccoonModel} adds the Ice Spider's states and animations.
 */
public class RaccoonModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Raccoons.id("raccoon"), "main");
    protected static final float DEG = Mth.DEG_TO_RAD;
    protected final ModelPart root, body, head, jaw, tail, rightFront, leftFront, rightHind, leftHind;

    public RaccoonModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = body.getChild("head");
        this.jaw = head.getChild("jaw");
        this.tail = body.getChild("tail");
        this.rightFront = this.root.getChild("right_front_leg");
        this.leftFront = this.root.getChild("left_front_leg");
        this.rightHind = this.root.getChild("right_hind_leg");
        this.leftHind = this.root.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -4F, -6F, 10F, 8F, 12F), PartPose.offset(0F, 16F, 1F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 20).addBox(-3.5F, -3.5F, -5F, 7F, 6F, 5F)
                        .texOffs(24, 20).addBox(-1.5F, 0F, -7F, 3F, 2F, 2F),   // snout
                PartPose.offset(0F, -1F, -6F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(24, 24).addBox(-1.5F, 0F, -2F, 3F, 1F, 2F),
                PartPose.offset(0F, 2F, -5F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(36, 20).addBox(-1F, -2F, 0F, 2F, 2F, 1F),
                PartPose.offset(2.2F, -3.5F, -2F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(36, 24).addBox(-1F, -2F, 0F, 2F, 2F, 1F),
                PartPose.offset(-2.2F, -3.5F, -2F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 9F),
                PartPose.offsetAndRotation(0F, -2F, 6F, -0.35F, 0F, 0F));
        String[] legs = {"right_front_leg", "left_front_leg", "right_hind_leg", "left_hind_leg"};
        float[][] at = {{-3F, -3F}, {3F, -3F}, {-3F, 9F}, {3F, 9F}};
        for (int i = 0; i < 4; i++)
            root.addOrReplaceChild(legs[i], CubeListBuilder.create().texOffs(44, i * 7).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                    PartPose.offset(at[i][0], 20F, at[i][1]));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = Mth.clamp(headPitch, -30F, 30F) * DEG;
        head.yRot = Mth.clamp(netHeadYaw, -40F, 40F) * DEG;
        float swing = limbSwing * 0.6662F, amount = Math.min(limbSwingAmount, 1F) * 1.4F;   // trot: diagonal pairs
        rightFront.xRot = leftHind.xRot = Mth.cos(swing) * amount;
        leftFront.xRot = rightHind.xRot = Mth.cos(swing + Mth.PI) * amount;
        tail.yRot = Mth.cos(swing) * 0.3F * limbSwingAmount + Mth.sin(ageInTicks * 0.08F) * 0.08F;
        body.y += Math.abs(Mth.sin(swing)) * -0.6F * limbSwingAmount;
        if (airborne(e)) {   // leap: legs stretch out, tail up, body tips with the jump
            float up = Mth.clamp((float) e.getDeltaMovement().y * 3F, -1F, 1F);
            rightFront.xRot = leftFront.xRot = -1.1F;
            rightHind.xRot = leftHind.xRot = 1.1F;
            tail.xRot -= 0.5F;
            body.xRot -= up * 0.3F;
        }
        if (attackTime > 0 && bitesOnSwing()) {   // bite: the head lunges and the jaw snaps with the attack swing
            float s = Mth.sin(attackTime * Mth.PI);
            head.z -= s * 2.5F;
            head.xRot += s * 0.5F;
            jaw.xRot += s * 0.9F;
        }
        animateExtra(e, ageInTicks);
    }

    protected boolean airborne(T e) {
        return !e.onGround() && !e.isPassenger() && !(e instanceof Spider s && s.isClimbing());
    }

    protected boolean bitesOnSwing() {
        return true;
    }

    protected void animateExtra(T e, float ageInTicks) {}
}
