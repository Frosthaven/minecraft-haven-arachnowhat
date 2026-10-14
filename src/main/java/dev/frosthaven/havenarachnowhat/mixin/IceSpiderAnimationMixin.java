package dev.frosthaven.havenarachnowhat.mixin;

import dev.frosthaven.havenarachnowhat.IceAnims;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Iron's tells a client Ice Spider which GeckoLib animation to play by name; record it so the Angry Ice Raccoon plays its
 *  own version. Skipped when Iron's is not installed (@Pseudo). */
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.entity.mobs.ice_spider.IceSpiderEntity", remap = false)
public abstract class IceSpiderAnimationMixin {
    @Inject(method = "playAnimation(Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private void haven$onPlayAnimation(String name, CallbackInfo ci) {
        IceAnims.started((Entity) (Object) this, name);
    }
}
