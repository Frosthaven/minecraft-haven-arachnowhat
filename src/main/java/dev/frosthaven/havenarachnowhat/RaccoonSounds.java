package dev.frosthaven.havenarachnowhat;

import dev.frosthaven.havenarachnowhat.mixin.AbstractSoundInstanceAccessor;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

/**
 * Swaps spider sound events for Mecha Raccoon events (assets/haven_arachnowhat/sounds.json: vanilla iron golem, crafter,
 * piston and chain sounds at other pitches) as the client plays them, and plays the event's ".grit" layer with it when
 * it has one (a quieter beacon, metal or glass sound that adds the electronic edge). The Spider, Cave Spider and Ice
 * Spider share the {@code entity.spider.*} events,
 * so the nearest spider-like mob at the sound's position picks the variant. Other mobs that reuse spider sound files
 * under their own events are not changed.
 */
final class RaccoonSounds {
    private static final ResourceLocation ICE_SPIDER = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ice_spider");
    private static final Set<String> ICE_EVENTS = Set.of("ambient", "hurt", "death", "bite", "swing", "grapple_latch",
            "grapple_spit", "howl");

    private RaccoonSounds() {}

    static void onPlay(PlaySoundEvent event) {
        SoundInstance s = event.getSound();
        if (!Raccoons.enabled() || !(s instanceof AbstractSoundInstance)) return;
        ResourceLocation id = s.getLocation();
        String replacement = replacementEvent(s);
        if (replacement != null) {
            event.setSound(copy(s, Raccoons.id(replacement)));
            id = Raccoons.id(replacement);
        }
        if (!id.getNamespace().equals(HavenArachnowhat.ID) || id.getPath().endsWith(".grit")) return;
        ResourceLocation grit = Raccoons.id(id.getPath() + ".grit");
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager().getSoundEvent(grit) == null) return;
        SoundInstance layer = copy(s, grit);
        mc.tell(() -> mc.getSoundManager().play(layer));   // queued: not from inside the engine's own play call
    }

    /** The same sound (raw volume and pitch, position, attenuation) under another event. */
    private static SoundInstance copy(SoundInstance s, ResourceLocation event) {
        AbstractSoundInstanceAccessor raw = (AbstractSoundInstanceAccessor) s;
        return new SimpleSoundInstance(event, s.getSource(), raw.haven$volume(), raw.haven$pitch(), RandomSource.create(),
                s.isLooping(), s.getDelay(), s.getAttenuation(), s.getX(), s.getY(), s.getZ(), s.isRelative());
    }

    private static String replacementEvent(SoundInstance s) {
        ResourceLocation id = s.getLocation();
        String path = id.getPath();
        if (id.getNamespace().equals("minecraft")) {
            if (path.equals("entity.parrot.imitate.spider")) return "parrot_imitate";
            if (path.startsWith("entity.spider.")) return nearestKind(s) + "." + path.substring("entity.spider.".length());
        } else if (id.getNamespace().equals("irons_spellbooks") && path.startsWith("entity.ice_spider.")) {
            String e = path.substring("entity.ice_spider.".length());
            return ICE_EVENTS.contains(e) ? "mecha_ice_raccoon." + e : null;
        }
        return null;
    }

    /** The variant of the closest spider-like mob within 3 blocks of the sound; the Mecha Raccoon if none. */
    private static String nearestKind(SoundInstance s) {
        var level = Minecraft.getInstance().level;
        String best = "mecha_raccoon";
        if (level == null) return best;
        double bestD = Double.MAX_VALUE;
        for (Entity e : level.getEntities((Entity) null, new AABB(s.getX(), s.getY(), s.getZ(), s.getX(), s.getY(), s.getZ()).inflate(3))) {
            String kind = e.getType() == EntityType.CAVE_SPIDER ? "mecha_cave_raccoon"
                    : e.getType() == EntityType.SPIDER ? "mecha_raccoon"
                    : BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(ICE_SPIDER) ? "mecha_ice_raccoon" : null;
            double d = e.distanceToSqr(s.getX(), s.getY(), s.getZ());
            if (kind != null && d < bestD) {
                best = kind;
                bestD = d;
            }
        }
        return best;
    }
}
