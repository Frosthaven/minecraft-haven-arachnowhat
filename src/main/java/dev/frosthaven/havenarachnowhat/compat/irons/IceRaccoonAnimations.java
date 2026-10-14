package dev.frosthaven.havenarachnowhat.compat.irons;

import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Mecha Ice Raccoon versions of the Ice Spider's GeckoLib animations, on the raccoon's bones (root, body, head, jaw,
 * ears, tail, four legs). Same names and lengths as Iron's ice_spider.animation.json, so each one lasts as long as the
 * attack it stands for. Positions in model pixels (+y up), rotations in degrees (+x tips forward and down).
 */
final class IceRaccoonAnimations {
    static final AnimationDefinition BITE = AnimationDefinition.Builder.withLength(1.2083F)
            .addAnimation("head", pos(k(0, 0, 0, 0), k(0.3F, 0, 0, -3), k(0.7F, 0, 0, 0), k(1.2083F, 0, 0, 0)))
            .addAnimation("head", rot(k(0, 0, 0, 0), k(0.3F, 15, 0, 0), k(0.7F, 0, 0, 0)))
            .addAnimation("jaw", rot(k(0, 0, 0, 0), k(0.2F, 45, 0, 0), k(0.35F, 0, 0, 0), k(1.2083F, 0, 0, 0)))
            .addAnimation("left_front_leg", rot(k(0, 0, 0, 0), k(0.3F, -20, 0, 0), k(0.7F, 0, 0, 0)))
            .addAnimation("right_front_leg", rot(k(0, 0, 0, 0), k(0.3F, -20, 0, 0), k(0.7F, 0, 0, 0)))
            .build();

    static final AnimationDefinition FANG = AnimationDefinition.Builder.withLength(1F)
            .addAnimation("head", rot(k(0, 0, 0, 0), k(0.2F, 10, 15, 0), k(0.45F, 10, -15, 0), k(0.65F, 10, 10, 0), k(1F, 0, 0, 0)))
            .addAnimation("jaw", rot(k(0, 0, 0, 0), k(0.15F, 45, 0, 0), k(0.3F, 0, 0, 0), k(0.5F, 45, 0, 0), k(0.65F, 0, 0, 0), k(1F, 0, 0, 0)))
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.3F, 6, 0, 0), k(1F, 0, 0, 0)))
            .build();

    static final AnimationDefinition SWIPE = AnimationDefinition.Builder.withLength(0.7083F)
            .addAnimation("right_front_leg", rot(k(0, 0, 0, 0), k(0.2F, -80, 0, -20), k(0.4F, -60, 0, 30), k(0.7083F, 0, 0, 0)))
            .addAnimation("right_front_leg", pos(k(0, 0, 0, 0), k(0.2F, 0, 2, -1), k(0.4F, 0, 2, -1), k(0.7083F, 0, 0, 0)))
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.25F, 0, -10, 5), k(0.45F, 0, 10, -3), k(0.7083F, 0, 0, 0)))
            .build();

    static final AnimationDefinition POUNCE = AnimationDefinition.Builder.withLength(2.125F)
            .addAnimation("body", pos(k(0, 0, 0, 0), k(0.5F, 0, -2, 1), k(0.85F, 0, 2, -5), k(1.3F, 0, 0, -4), k(2.125F, 0, 0, 0)))
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.5F, 8, 0, 0), k(0.85F, -15, 0, 0), k(1.3F, 5, 0, 0), k(2.125F, 0, 0, 0)))
            .addAnimation("jaw", rot(k(0, 0, 0, 0), k(0.6F, 0, 0, 0), k(0.85F, 50, 0, 0), k(1.5F, 50, 0, 0), k(1.8F, 0, 0, 0), k(2.125F, 0, 0, 0)))
            .addAnimation("tail", rot(k(0, 0, 0, 0), k(0.5F, 20, 0, 0), k(0.85F, -30, 0, 0), k(2.125F, 0, 0, 0)))
            .addAnimation("left_front_leg", rot(k(0, 0, 0, 0), k(0.5F, 20, 0, 0), k(0.85F, -70, 0, 0), k(1.3F, -10, 0, 0), k(2.125F, 0, 0, 0)))
            .addAnimation("right_front_leg", rot(k(0, 0, 0, 0), k(0.5F, 20, 0, 0), k(0.85F, -70, 0, 0), k(1.3F, -10, 0, 0), k(2.125F, 0, 0, 0)))
            .addAnimation("left_hind_leg", rot(k(0, 0, 0, 0), k(0.5F, -20, 0, 0), k(0.85F, 50, 0, 0), k(1.3F, 0, 0, 0)))
            .addAnimation("right_hind_leg", rot(k(0, 0, 0, 0), k(0.5F, -20, 0, 0), k(0.85F, 50, 0, 0), k(1.3F, 0, 0, 0)))
            .build();

    static final AnimationDefinition LEAP_BACK = AnimationDefinition.Builder.withLength(0.75F)
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.25F, -25, 0, 0), k(0.5F, -10, 0, 0), k(0.75F, 0, 0, 0)))
            .addAnimation("body", pos(k(0, 0, 0, 0), k(0.25F, 0, 2, 3), k(0.75F, 0, 0, 0)))
            .addAnimation("left_front_leg", rot(k(0, 0, 0, 0), k(0.25F, -60, 0, 0), k(0.5F, -15, 0, 0), k(0.75F, 0, 0, 0)))
            .addAnimation("right_front_leg", rot(k(0, 0, 0, 0), k(0.25F, -60, 0, 0), k(0.5F, -15, 0, 0), k(0.75F, 0, 0, 0)))
            .addAnimation("left_hind_leg", rot(k(0, 0, 0, 0), k(0.25F, 20, 0, 0), k(0.75F, 0, 0, 0)))
            .addAnimation("right_hind_leg", rot(k(0, 0, 0, 0), k(0.25F, 20, 0, 0), k(0.75F, 0, 0, 0)))
            .build();

    static final AnimationDefinition LONG_CAST = AnimationDefinition.Builder.withLength(1.625F)
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.35F, -30, 0, 0), k(1.25F, -30, 0, 0), k(1.625F, 0, 0, 0)))
            .addAnimation("head", rot(k(0, 0, 0, 0), k(0.35F, -25, 0, 0), k(1.25F, -25, 0, 0), k(1.625F, 0, 0, 0)))
            .addAnimation("jaw", rot(k(0, 0, 0, 0), k(0.4F, 35, 0, 0), k(1.2F, 35, 0, 0), k(1.625F, 0, 0, 0)))
            .addAnimation("left_ear", rot(k(0, 0, 0, 0), k(0.4F, 0, 0, -30), k(1.2F, 0, 0, -30), k(1.625F, 0, 0, 0)))
            .addAnimation("right_ear", rot(k(0, 0, 0, 0), k(0.4F, 0, 0, 30), k(1.2F, 0, 0, 30), k(1.625F, 0, 0, 0)))
            .addAnimation("left_front_leg", rot(k(0, 0, 0, 0), k(0.35F, -50, 0, -15), k(1.25F, -50, 0, -15), k(1.625F, 0, 0, 0)))
            .addAnimation("right_front_leg", rot(k(0, 0, 0, 0), k(0.35F, -50, 0, 15), k(1.25F, -50, 0, 15), k(1.625F, 0, 0, 0)))
            .addAnimation("tail", rot(k(0, 0, 0, 0), k(0.35F, -25, 0, 0), k(1.25F, -25, 0, 0), k(1.625F, 0, 0, 0)))
            .build();

    static final AnimationDefinition EMERGE = AnimationDefinition.Builder.withLength(3F)
            .addAnimation("root", pos(k(0, 0, -14, 0), k(0.3F, 0, -14, 0), k(1.4F, 0, -6, 0), k(2.2F, 0, -2, 0), k(3F, 0, 0, 0)))
            .addAnimation("body", rot(k(0, 0, 0, 0), k(0.6F, 0, 0, 6), k(1F, 0, 0, -6), k(1.4F, 0, 0, 5), k(1.8F, 0, 0, -4), k(2.4F, 0, 0, 0)))
            .addAnimation("head", rot(k(0, 30, 0, 0), k(2.4F, -20, 0, 0), k(3F, 0, 0, 0)))
            .build();

    static final Map<String, AnimationDefinition> BY_NAME = Map.of(
            "attack_bite", BITE, "attack_fang_basic", FANG, "attack_right_swipe", SWIPE, "attack_grapple_pounce", POUNCE,
            "leap_back", LEAP_BACK, "long_cast", LONG_CAST, "emerge_from_ground", EMERGE);

    static final Map<String, Float> LENGTH = Map.of(
            "attack_bite", BITE.lengthInSeconds(), "attack_fang_basic", FANG.lengthInSeconds(),
            "attack_right_swipe", SWIPE.lengthInSeconds(), "attack_grapple_pounce", POUNCE.lengthInSeconds(),
            "leap_back", LEAP_BACK.lengthInSeconds(), "long_cast", LONG_CAST.lengthInSeconds(), "emerge_from_ground", EMERGE.lengthInSeconds());

    private IceRaccoonAnimations() {}

    /** A keyframe at t seconds with an (x, y, z) value. */
    private static float[] k(float t, float x, float y, float z) {
        return new float[] {t, x, y, z};
    }

    private static AnimationChannel rot(float[]... frames) {
        return channel(AnimationChannel.Targets.ROTATION, true, frames);
    }

    private static AnimationChannel pos(float[]... frames) {
        return channel(AnimationChannel.Targets.POSITION, false, frames);
    }

    private static AnimationChannel channel(AnimationChannel.Target target, boolean degrees, float[]... frames) {
        Keyframe[] out = new Keyframe[frames.length];
        for (int i = 0; i < frames.length; i++) {
            float[] f = frames[i];
            out[i] = new Keyframe(f[0], degrees ? KeyframeAnimations.degreeVec(f[1], f[2], f[3]) : KeyframeAnimations.posVec(f[1], f[2], f[3]),
                    AnimationChannel.Interpolations.CATMULLROM);
        }
        return new AnimationChannel(target, out);
    }
}
