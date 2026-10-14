package dev.frosthaven.havenarachnawhat.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The cached name component, cleared when the spider setting changes. */
@Mixin(EntityType.class)
public interface EntityTypeAccessor {
    @Accessor("description")
    void haven$setDescription(Component description);
}
