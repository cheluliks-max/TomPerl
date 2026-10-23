package com.tompearl.client;

import com.tompearl.TomPearlEntity;
import com.tompearl.TomPearlMod;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class TomPearlRenderer extends BipedEntityRenderer<TomPearlEntity, BipedEntityModel<TomPearlEntity>> {
    private static final Identifier TEXTURE = TomPearlMod.id("textures/entity/tom_pearl.png");

    public TomPearlRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(TomPearlEntity entity) {
        return TEXTURE;
    }
}
