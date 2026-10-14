package dev.frosthaven.havenarachnowhat;

import dev.frosthaven.havenarachnowhat.compat.irons.IronsCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Test harness driver (tools/harness.py world): mobs named "<Label>: <action>" act out client-side visuals the server
 * gives no way to force, such as a Spider's attack swing or each Ice Spider animation. Off unless the JVM has
 * {@code -Dhaven_arachnowhat.harness=true}.
 */
final class Harness {
    static final boolean running = Boolean.getBoolean("haven_arachnowhat.harness");

    private Harness() {}

    static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!running || mc.level == null || mc.isPaused()) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            Component name = e.getCustomName();
            if (name == null) continue;
            String s = name.getString();
            int i = s.indexOf(": ");
            if (i < 0) continue;
            String action = s.substring(i + 2);
            if (e instanceof Spider spider) {
                if (action.equals("attack") && spider.tickCount % 30 == 0) spider.swing(InteractionHand.MAIN_HAND);
            } else if (HavenArachnowhat.IRONS && IronsCompat.isIceSpider(e)) {
                IronsCompat.harness(e, action);
            }
        }
    }
}
