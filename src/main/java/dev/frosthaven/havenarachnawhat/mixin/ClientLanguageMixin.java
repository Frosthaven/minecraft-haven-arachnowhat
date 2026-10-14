package dev.frosthaven.havenarachnawhat.mixin;

import dev.frosthaven.havenarachnawhat.Raccoons;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** While spiders are replaced, the spider name keys read the raccoon names instead, in every language that has them
 *  (falling back to the spider name). Switches at once with the setting, unlike a language file override. */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
    @Inject(method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void haven$raccoonNames(String key, String fallback, CallbackInfoReturnable<String> cir) {
        String raccoon = Raccoons.RENAMES.get(key);
        if (raccoon != null && Raccoons.enabled()) cir.setReturnValue(((ClientLanguage) (Object) this).getOrDefault(raccoon, fallback));
    }
}
