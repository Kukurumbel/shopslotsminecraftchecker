package com.example.shopparser;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ShopParserClient implements ClientModInitializer {
    private static KeyBinding startKey;

    @Override
    public void onInitializeClient() {
        startKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.shopparser.start",
            GLFW.GLFW_KEY_P, // Кнопка P (англ) в игре для старта
            "category.shopparser"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && startKey.wasPressed()) {
                if (!ShopParser.isParsing) {
                    ShopParser.start();
                }
            }
        });
    }
}
