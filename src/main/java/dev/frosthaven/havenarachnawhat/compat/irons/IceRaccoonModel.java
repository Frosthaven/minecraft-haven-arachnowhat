package dev.frosthaven.havenarachnawhat.compat.irons;

import dev.frosthaven.havenarachnawhat.IceAnims;
import dev.frosthaven.havenarachnawhat.RaccoonModel;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Angry Ice Raccoon: the raccoon model plus the Ice Spider's own states. Crouching lowers the body and splays the
 * legs; grappling holds the head down with the jaw open; each named Iron's animation (bite, fang, swipe, pounce, leap
 * back, cast, emerge) plays its keyframes ({@link IceRaccoonAnimations}) from the moment the mixin saw it start.
 * Climbing is the renderer's surface rotation. Its attacks are named animations, so the plain swing bite is off.
 */
final class IceRaccoonModel extends RaccoonModel<IceSpiderEntity> {
    IceRaccoonModel(ModelPart root) {
        super(root);
    }

    @Override
    protected boolean airborne(IceSpiderEntity e) {
        return !e.onGround() && !e.isPassenger() && !e.isClimbing();
    }

    @Override
    protected boolean bitesOnSwing() {
        return false;
    }

    @Override
    protected void animateExtra(IceSpiderEntity e, float ageInTicks) {
        float crouch = Mth.clamp(e.crouchTweenPercent(ageInTicks - e.tickCount), 0F, 1F);
        if (crouch > 0F) {
            body.y += 3F * crouch;
            leftFront.zRot = leftHind.zRot = -0.6F * crouch;
            rightFront.zRot = rightHind.zRot = 0.6F * crouch;
            tail.xRot += 0.4F * crouch;
        }
        if (e.isGrappling()) {
            head.xRot += 0.4F;
            jaw.xRot += 0.8F;
        }
        IceAnims.Playing p = IceAnims.get(e);
        AnimationDefinition def = p == null ? null : IceRaccoonAnimations.BY_NAME.get(p.name());
        if (def != null) {
            animate(p.state(), def, ageInTicks);
            if (p.state().getAccumulatedTime() > def.lengthInSeconds() * 1000F) IceAnims.clear(e);
        }
    }
}
