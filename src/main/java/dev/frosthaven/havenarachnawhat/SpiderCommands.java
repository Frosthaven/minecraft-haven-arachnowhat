package dev.frosthaven.havenarachnawhat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** Client commands {@code /spiders on} (spiders stay spiders) and {@code /spiders off} (angry raccoons), saved to the config. */
final class SpiderCommands {
    private SpiderCommands() {}

    static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("spiders")
                .then(Commands.literal("on").executes(c -> set(c.getSource(), false)))
                .then(Commands.literal("off").executes(c -> set(c.getSource(), true))));
    }

    private static int set(CommandSourceStack source, boolean raccoons) {
        Raccoons.set(raccoons);
        source.sendSuccess(() -> Component.translatable(raccoons ? "haven_arachnawhat.spiders.off" : "haven_arachnawhat.spiders.on"), false);
        return 1;
    }
}
