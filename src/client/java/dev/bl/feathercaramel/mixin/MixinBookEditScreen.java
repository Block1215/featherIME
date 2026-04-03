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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * BookEditScreen (本と羽根ペン) の IME サポート。
 *
 * BookEditScreen は本文用 (pageEdit) とタイトル用 (titleEdit) の
 * 2 つの TextFieldHelper を持つ。
 * コンストラクタで new TextFieldHelper(...) が 2 回呼ばれるため、
 * 1 回目 (pageEdit) のみを IME ラッパーとして登録する。
 *
 * フォーカス解除は MixinMinecraft.changeFocusedScreen() が
 * 画面切り替え時に自動的に行うため、ここでは init() のみ担当する。
 * (BookEditScreen は Screen.removed() をオーバーライドしないため、
 *  removed() フックは効かない。MixinMinecraft に任せる。)
 */
@Mixin(BookEditScreen.class)
public abstract class MixinBookEditScreen extends Screen {

    @Unique private WrapperTextFieldHelper featherCaramel$pageWrapper;
    @Unique private int                    featherCaramel$helperCount = 0;

    protected MixinBookEditScreen() { super(Component.empty()); }

    /**
     * コンストラクタ内の new TextFieldHelper(...) 呼び出しを横取りする。
     * BookEditScreen はコンストラクタで pageEdit → titleEdit の順に 2 つ生成する。
     * 1 つ目 (pageEdit, 本文) のみ IME ラッパーとして保存する。
     */
    @Redirect(
        method = "<init>",
        at = @At(value = "NEW", target = "net/minecraft/client/gui/font/TextFieldHelper"),
        require = 0
    )
    private TextFieldHelper featherCaramel$captureHelper(
            final Supplier<String>    getMsg,
            final Consumer<String>    setMsg,
            final Supplier<String>    getClip,
            final Consumer<String>    setClip,
            final Predicate<String>   validator) {
        final TextFieldHelper helper = new TextFieldHelper(getMsg, setMsg, getClip, setClip, validator);
        featherCaramel$helperCount++;
        if (featherCaramel$helperCount == 1) {
            // 1 つ目 = pageEdit (本文)
            featherCaramel$pageWrapper = new WrapperTextFieldHelper(
                helper,
                () -> new Rect(this.width / 2.0f, this.height / 2.0f, 10, 10)
            );
        }
        return helper;
    }

    /**
     * init() 完了後に本文欄へ IME フォーカスを設定する。
     */
    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$onInit(final CallbackInfo ci) {
        if (featherCaramel$pageWrapper != null) {
            featherCaramel$pageWrapper.setFocused(true);
        }
    }
}
