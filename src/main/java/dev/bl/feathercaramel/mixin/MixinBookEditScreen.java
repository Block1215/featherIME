package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.util.Rect;
import dev.bl.feathercaramel.wrapper.WrapperTextFieldHelper;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BookEditScreen (本と羽根ペン) の IME サポート - 1.21.7+ 版。
 *
 * 1.21.7 以降 BookEditScreen は TextFieldHelper ではなく
 * MultiLineEditBox を使用するため、コンストラクタで TextFieldHelper を
 * キャプチャする旧方式が使えない。
 *
 * 代わりに setInitialFocus() / init() のタイミングでダミーの
 * WrapperTextFieldHelper を生成して IME フォーカスのみを有効化する。
 * 確定テキストは OS が GLFW charTyped 経由で MultiLineEditBox に直接渡すため、
 * IME フォーカスの有効化だけで入力が機能する。
 */
@Mixin(BookEditScreen.class)
public abstract class MixinBookEditScreen extends Screen {

    @Unique private WrapperTextFieldHelper featherCaramel$bookWrapper;

    protected MixinBookEditScreen() { super(Component.empty()); }

    /**
     * setInitialFocus() は 1.21.7+ で MultiLineEditBox にフォーカスを設定する。
     * このタイミングで IME を有効化する。
     */
    @Inject(method = "setInitialFocus", at = @At("TAIL"), require = 0)
    private void featherCaramel$onSetInitialFocus(final CallbackInfo ci) {
        featherCaramel$ensureWrapperAndFocus();
    }

    /**
     * init() でも IME フォーカスを確保する。
     */
    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$onInit(final CallbackInfo ci) {
        featherCaramel$ensureWrapperAndFocus();
    }

    @Unique
    private void featherCaramel$ensureWrapperAndFocus() {
        if (featherCaramel$bookWrapper == null) {
            // IME フォーカス管理専用のダミー TextFieldHelper
            // 確定テキストは OS → GLFW charTyped → MultiLineEditBox へ渡るため
            // ここでの insert() は呼ばれない
            final TextFieldHelper dummy = new TextFieldHelper(
                () -> "", s -> {}, () -> "", s -> {}, s -> true
            );
            featherCaramel$bookWrapper = new WrapperTextFieldHelper(
                dummy,
                () -> new Rect(this.width / 2.0f, this.height / 2.0f, 10, 10)
            );
        }
        featherCaramel$bookWrapper.setFocused(true);
    }
}
