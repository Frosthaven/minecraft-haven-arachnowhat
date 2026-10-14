package dev.frosthaven.havenarachnawhat.mixin;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The raw volume and pitch: the getters need the sound resolved, which happens after PlaySoundEvent. */
@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {
    @Accessor("volume")
    float haven$volume();

    @Accessor("pitch")
    float haven$pitch();
}
