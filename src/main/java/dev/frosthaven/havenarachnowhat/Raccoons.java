package dev.frosthaven.havenarachnowhat;

import dev.frosthaven.havenarachnowhat.compat.irons.IronsCompat;
import dev.frosthaven.havenarachnowhat.mixin.EntityTypeAccessor;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/** Shared state: whether spiders are replaced ({@link RaccoonConfig}), the names to use, and the skins. */
public final class Raccoons {
    public static final Log LOG = new Log();

    /** Spider name keys and the raccoon keys that stand in for them while replacing (ClientLanguageMixin). */
    public static final Map<String, String> RENAMES = Map.of(
            "entity.minecraft.spider", "haven_arachnowhat.name.spider",
            "entity.minecraft.cave_spider", "haven_arachnowhat.name.cave_spider",
            "entity.irons_spellbooks.ice_spider", "haven_arachnowhat.name.ice_spider",
            "item.minecraft.spider_spawn_egg", "haven_arachnowhat.name.spider_spawn_egg",
            "item.minecraft.cave_spider_spawn_egg", "haven_arachnowhat.name.cave_spider_spawn_egg",
            "item.irons_spellbooks.ice_spider_spawn_egg", "haven_arachnowhat.name.ice_spider_spawn_egg");
    private static final ResourceLocation[] RENAMED_TYPES = {
            ResourceLocation.withDefaultNamespace("spider"), ResourceLocation.withDefaultNamespace("cave_spider"),
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ice_spider")};

    private Raccoons() {}

    /** True until the config has loaded, then the config value. */
    public static boolean enabled() {
        return !RaccoonConfig.SPEC.isLoaded() || RaccoonConfig.REPLACE_SPIDERS.get();
    }

    public static void set(boolean on) {
        RaccoonConfig.REPLACE_SPIDERS.set(on);
        RaccoonConfig.SPEC.save();
        changed();
    }

    /** After the setting changes (command, config screen or file): names and queued Ice Spider animations. */
    public static void changed() {
        refreshNames();
        if (HavenArachnowhat.IRONS) IronsCompat.clearQueuedAnimations();
    }

    /** Entity types keep their translated name component, and it keeps its text: drop it so the next lookup uses
     *  the current setting. */
    private static void refreshNames() {
        for (ResourceLocation id : RENAMED_TYPES) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            if (type != null) ((EntityTypeAccessor) type).haven$setDescription(null);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HavenArachnowhat.ID, path);
    }

    /** One raccoon look: its texture and its eyes-only texture (tools/textures.py). */
    public enum Skin {
        MECHA("mecha_raccoon"), CAVE("mecha_cave_raccoon"), ICE("mecha_ice_raccoon");

        public final ResourceLocation texture, eyes;

        Skin(String name) {
            this.texture = id("textures/entity/" + name + ".png");
            this.eyes = id("textures/entity/" + name + "_eyes.png");
        }
    }
}
