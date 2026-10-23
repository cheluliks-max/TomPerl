package com.tompearl.client;

import com.tompearl.AbilityPayload;
import com.tompearl.TomPearlMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TomPearlClient implements ClientModInitializer {
    private static KeyBinding dashKey;
    private static KeyBinding waveKey;
    private static KeyBinding cloudKey;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TomPearlMod.TOM_PEARL, TomPearlRenderer::new);

        dashKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.tom_pearl.dash", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.tom_pearl"));
        waveKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.tom_pearl.wave", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.tom_pearl"));
        cloudKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.tom_pearl.cloud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.tom_pearl"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (dashKey.wasPressed()) ClientPlayNetworking.send(new AbilityPayload(0));
            while (waveKey.wasPressed()) ClientPlayNetworking.send(new AbilityPayload(1));
            while (cloudKey.wasPressed()) ClientPlayNetworking.send(new AbilityPayload(2));
        });
    }
}
