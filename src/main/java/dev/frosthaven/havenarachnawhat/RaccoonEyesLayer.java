package dev.frosthaven.havenarachnawhat;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** Glowing eyes, like the spider's: a full-bright pass of the model with a texture that is only the eye pixels. */
public final class RaccoonEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType type;

    public RaccoonEyesLayer(RenderLayerParent<T, M> parent, ResourceLocation eyes) {
        super(parent);
        this.type = RenderType.eyes(eyes);
    }

    @Override
    public RenderType renderType() {
        return type;
    }
}
