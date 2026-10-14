package dev.frosthaven.havenarachnawhat.compat.irons;

import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderRenderer;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import dev.frosthaven.havenarachnawhat.Raccoons;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Iron's Spells 'n Spellbooks support. Only called when that mod is loaded, so its classes are never touched otherwise. */
public final class IronsCompat {
    public static final String MOD = "irons_spellbooks";

    private IronsCompat() {}

    @SuppressWarnings("unchecked")
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        var type = (EntityType<IceSpiderEntity>) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(MOD, "ice_spider"));
        e.registerEntityRenderer(type, ctx -> new IceRaccoonRenderer(ctx, new IceSpiderRenderer(ctx)));
    }

    /**
     * Iron's queues each Ice Spider animation until its own model next draws. While raccoons are shown it never does,
     * so the last attack would replay when spiders come back: drop what is queued in the loaded Ice Spiders.
     */
    public static void clearQueuedAnimations() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        try {
            Field queued = IceSpiderEntity.class.getDeclaredField("animationToPlay");
            queued.setAccessible(true);
            for (Entity e : level.entitiesForRendering())
                if (e instanceof IceSpiderEntity s) queued.set(s, null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Raccoons.LOG.error("ice-queue", "could not clear queued Ice Spider animations; one may replay after switching", e);
        }
    }

    public static boolean isIceSpider(Entity e) {
        return e instanceof IceSpiderEntity;
    }

    /** Harness actions (see Harness): hold a state, or replay a named animation with a pause between plays. */
    public static void harness(Entity e, String action) {
        IceSpiderEntity s = (IceSpiderEntity) e;
        switch (action) {
            case "crouching" -> s.setIsCrouching(true);
            case "grappling" -> s.setGrappleTargetUUID(Minecraft.getInstance().player.getUUID());
            case "climbing" -> {   // a wall in front: the surface normal points back at the spider
                Vec3 n = Vec3.directionFromRotation(0, s.yBodyRot).scale(-1);
                s.normal = n;
                s.lastNormal = n;
            }
            default -> {
                Float seconds = IceRaccoonAnimations.LENGTH.get(action);
                if (seconds != null && s.tickCount % (Math.round(seconds * 20) + 30) == 0) s.playAnimation(action);
            }
        }
    }
}
