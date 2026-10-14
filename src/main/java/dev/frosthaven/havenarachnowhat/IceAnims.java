package dev.frosthaven.havenarachnowhat;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

/** The named animation each client Ice Spider was last told to play (captured by the playAnimation mixin), with its
 *  start time, for the Mecha Ice Raccoon model. Render thread only. */
public final class IceAnims {
    public record Playing(String name, AnimationState state) {}

    private static final Map<Entity, Playing> PLAYING = new WeakHashMap<>();

    private IceAnims() {}

    public static void started(Entity e, String name) {
        if (!e.level().isClientSide) return;
        AnimationState state = new AnimationState();
        state.start(e.tickCount);
        PLAYING.put(e, new Playing(name, state));
    }

    public static Playing get(Entity e) {
        return PLAYING.get(e);
    }

    public static void clear(Entity e) {
        PLAYING.remove(e);
    }
}
