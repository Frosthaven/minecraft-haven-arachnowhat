package dev.frosthaven.havenarachnawhat;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client config (haven_arachnawhat-client.toml), also on the in-game config screen and set by {@code /spiders on|off}. */
public final class RaccoonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue REPLACE_SPIDERS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        REPLACE_SPIDERS = b.comment("Replace spiders with angry raccoons: models, names and sounds. /spiders on and /spiders off set it in game.")
                .define("replaceSpiders", true);
        SPEC = b.build();
    }

    private RaccoonConfig() {}
}
