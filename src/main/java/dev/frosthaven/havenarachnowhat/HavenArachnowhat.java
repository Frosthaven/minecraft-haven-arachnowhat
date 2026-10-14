package dev.frosthaven.havenarachnowhat;

import dev.frosthaven.havenarachnowhat.compat.irons.IronsCompat;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/** Client only: spiders are drawn, named and heard as Angry Raccoons. Spider and Cave Spider always; Iron's Ice Spider
 *  when that mod is loaded ({@link IronsCompat}). The server and other players see no change. */
@Mod(value = HavenArachnowhat.ID, dist = Dist.CLIENT)
public final class HavenArachnowhat {
    public static final String ID = "haven_arachnowhat";
    static final boolean IRONS = ModList.get().isLoaded(IronsCompat.MOD);

    public HavenArachnowhat(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, RaccoonConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(ModConfigEvent.Reloading.class, e -> Raccoons.changed());   // edited on the config screen or on disk
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(RaccoonModel.LAYER, RaccoonModel::createBodyLayer);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerEntityRenderer(EntityType.SPIDER, ctx -> new RaccoonRenderer<>(ctx, new SpiderRenderer<>(ctx), RaccoonRenderer.Kind.SPIDER));
            e.registerEntityRenderer(EntityType.CAVE_SPIDER, ctx -> new RaccoonRenderer<>(ctx, new CaveSpiderRenderer(ctx), RaccoonRenderer.Kind.CAVE));
            if (IRONS) IronsCompat.registerRenderers(e);
        });
        NeoForge.EVENT_BUS.addListener(PlaySoundEvent.class, RaccoonSounds::onPlay);
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, SpiderCommands::register);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, Harness::tick);
        Raccoons.LOG.once("start", "Angry Raccoons on (Iron's Ice Spider {})", IRONS ? "covered" : "not installed");
    }
}
