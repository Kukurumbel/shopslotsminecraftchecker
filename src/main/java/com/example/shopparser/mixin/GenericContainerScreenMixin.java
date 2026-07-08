package com.example.shopparser.mixin;

import com.example.shopparser.ShopParser;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public class GenericContainerScreenMixin {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onScreenInit(CallbackInfo ci) {
        GenericContainerScreen screen = (GenericContainerScreen) (Object) this;
        ShopParser.handleScreen(screen);
    }
}
