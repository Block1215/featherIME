package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.FeatherCaramelChatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 画面遷移のたびに IController.changeFocusedScreen() を呼び、
 * フォーカス中の Operator を適切にクリアする。
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "setScreen", at = @At("HEAD"), require = 0)
    private void featherCaramel$setScreen(final Screen screen, final CallbackInfo ci) {
        FeatherCaramelChatClient.getController().changeFocusedScreen(screen);
    }
}
