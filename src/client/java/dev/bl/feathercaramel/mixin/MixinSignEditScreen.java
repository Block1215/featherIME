package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.util.Rect;
import dev.bl.feathercaramel.wrapper.WrapperTextFieldHelper;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * SignEditScreen (看板) の IME サポート (1.19.2 向け)。
 * 1.19.2 では AbstractSignEditScreen が存在しないため直接 SignEditScreen をターゲットとする。
 */
@Mixin(SignEditScreen.class)
public abstract class MixinSignEditScreen extends Screen {

    @Unique private WrapperTextFieldHelper featherCaramel$signWrapper;

    protected MixinSignEditScreen() { super(new net.minecraft.network.chat.TextComponent("")); }

    @Redirect(
        method = "init",
        at = @At(value = "NEW", target = "net/minecraft/client/gui/font/TextFieldHelper"),
        require = 0
    )
    private TextFieldHelper featherCaramel$captureSignField(
            final Supplier<String>    getMsg,
            final Consumer<String>    setMsg,
            final Supplier<String>    getClip,
            final Consumer<String>    setClip,
            final Predicate<String>   validator) {
        final TextFieldHelper helper = new TextFieldHelper(getMsg, setMsg, getClip, setClip, validator);
        featherCaramel$signWrapper = new WrapperTextFieldHelper(
            helper,
            () -> new Rect(this.width / 2.0f, this.height * 0.35f, 10, 10)
        );
        return helper;
    }

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$onInit(final CallbackInfo ci) {
        if (featherCaramel$signWrapper != null) {
            featherCaramel$signWrapper.setFocused(true);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"), require = 0)
    private void featherCaramel$onRemoved(final CallbackInfo ci) {
        if (featherCaramel$signWrapper != null) {
            featherCaramel$signWrapper.setFocused(false);
            featherCaramel$signWrapper = null;
        }
    }
}
