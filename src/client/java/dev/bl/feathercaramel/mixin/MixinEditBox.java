package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Predicate;

/**
 * EditBox に IME プリエディット表示・確定入力・フォーカス制御を追加する。
 *
 * 1.19.3 と 1.19.4 の差異:
 *   - 1.19.3 の EditBox ではフォーカスメソッドが setFocus(boolean) という名前。
 *     (1.19.4 では setFocused(boolean) に統一された)
 *   - それ以外 (setValue, insertText, keyPressed, charTyped, フィールド名) は同一。
 *
 * 注意: charTyped は 1.19.3 にも存在する。キー入力の横取りは不要。
 * keyPressed では Ctrl+A/C/V/X などを含む複合キーを壊さないよう
 * Backspace/Delete の origin 追跡のみを行う。
 */
@Mixin(value = EditBox.class, priority = 0)
public abstract class MixinEditBox implements EditBoxController {

    @Unique private WrapperEditBox featherCaramel$wrapper;
    @Unique private int            featherCaramel$cacheCursor;
    @Unique private int            featherCaramel$cacheHighlight;

    @Shadow private boolean canLoseFocus;
    @Shadow public  int     highlightPos;
    @Shadow public  int     cursorPos;
    @Shadow public  String  value;
    @Shadow public  int     displayPos;
    @Shadow private boolean bordered;
    @Shadow public  net.minecraft.client.gui.Font font;
    @Shadow public  int     x;
    @Shadow public  int     y;
    @Shadow public  int     height;

    // ---- EditBoxController 実装 ----
    @Override
    public WrapperEditBox featherCaramel$wrapper() { return featherCaramel$wrapper; }

    // ---- 初期化 ----
    //
    // Redirect で初回 setValue の前に wrapper を生成し、
    // Redirect が失敗した場合は TAIL で遅延初期化する。

    @Redirect(
        method = "<init>(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/client/gui/components/EditBox;Lnet/minecraft/network/chat/Component;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V"),
        require = 0
    )
    private void featherCaramel$initRedirect(final EditBox self, final String value) {
        featherCaramel$wrapper = new WrapperEditBox((EditBox) (Object) this);
        self.setValue(value);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/client/gui/components/EditBox;Lnet/minecraft/network/chat/Component;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void featherCaramel$lazyInit(final CallbackInfo ci) {
        if (featherCaramel$wrapper == null) {
            featherCaramel$wrapper = new WrapperEditBox((EditBox) (Object) this);
        }
    }

    // ---- setValue フック ----
    //
    // 設計:
    //   HEAD  → カーソル位置を保存 / 通常編集なら status を NONE に
    //   Redirect(Predicate.test) → valueChanged=true の間はフィルタをバイパス
    //   TAIL  → valueChanged=true ならカーソル復元＆フラグリセット
    //            通常編集なら origin を現在の value で更新

    @Inject(method = "setValue", at = @At("HEAD"), require = 0)
    private void featherCaramel$setValueHead(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            // プレビュー用 setValue: 現在のカーソル位置を保存
            featherCaramel$cacheCursor    = cursorPos;
            featherCaramel$cacheHighlight = highlightPos;
        } else {
            // 通常の setValue: IME 状態をリセット
            featherCaramel$setStatusToNone();
        }
    }

    @Redirect(
        method = "setValue",
        at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"),
        require = 0
    )
    private boolean featherCaramel$setValueTest(final Predicate<String> pred, final Object v) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            // プレビュー用 setValue: フィルタをバイパスして無条件に通す
            return true;
        }
        return pred.test((String) v);
    }

    @Inject(method = "setValue", at = @At("TAIL"), require = 0)
    private void featherCaramel$setValueTail(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            value = text;
            final int sp = featherCaramel$wrapper.getSecondStartPos();
            final int maxPos = value.length();
            if (sp >= 0) {
                cursorPos    = Math.min(sp, maxPos);
                highlightPos = Math.min(sp, maxPos);
            } else {
                cursorPos    = Math.min(featherCaramel$cacheCursor, maxPos);
                highlightPos = Math.min(featherCaramel$cacheHighlight, maxPos);
            }
            featherCaramel$wrapper.valueChanged = false;
        } else {
            // 通常の setValue の後処理: origin を現在の value で更新
            featherCaramel$forceUpdateOrigin();
        }
    }

    // ---- insertText フック ----

    @Inject(method = "insertText", at = @At("HEAD"), require = 0)
    private void featherCaramel$insertTextHead(final String text, final CallbackInfo ci) {
        featherCaramel$setStatusToNone();
    }

    @Inject(method = "insertText", at = @At("TAIL"), require = 0)
    private void featherCaramel$insertTextTail(final String text, final CallbackInfo ci) {
        featherCaramel$forceUpdateOrigin();
    }

    // onValueChange が存在するバージョン向けフック（存在しなければ無視される）
    @Inject(method = "onValueChange", at = @At("HEAD"), require = 0)
    private void featherCaramel$onValueChange(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null
                && featherCaramel$wrapper.getStatus() == AbstractIMEWrapper.InputStatus.PREVIEW) {
            value = featherCaramel$wrapper.getOrigin();
            final int maxPos = value.length();
            if (cursorPos > maxPos) cursorPos = maxPos;
            if (highlightPos > maxPos) highlightPos = maxPos;
        }
    }

    // ---- keyPressed フック ----
    //
    // charTyped は 1.19.3 にも存在するため、文字入力の横取りは不要。
    // ここでは Backspace (259) と Delete (261) 後に origin を更新するのみ。
    // ※ Ctrl+A (65+Ctrl), Ctrl+C/V/X など複合キーには一切触れない。

    @Inject(method = "keyPressed", at = @At("TAIL"), require = 0)
    private void featherCaramel$keyPressed(
            final int keyCode, final int scanCode, final int modifiers,
            final CallbackInfoReturnable<Boolean> cir) {
        if (featherCaramel$wrapper == null) return;
        if (keyCode == 259 || keyCode == 261) {
            // Backspace / Delete: 削除後に origin を更新しないと
            // 次回 IME 入力時に削除前の文字が復元するバグが発生する
            ModLogger.debug("[FeatherIME] Delete key, updating origin");
            featherCaramel$forceUpdateOrigin();
        } else if (!featherCaramel$wrapper.valueChanged) {
            // その他のキー操作 (Ctrl+A で全選択 → 削除 など) でも
            // valueChanged=false のときは origin を最新値に同期しておく
            featherCaramel$forceUpdateOrigin();
        }
    }

    // ---- フォーカスフック ----
    //
    // 1.19.3 の EditBox にはフォーカス関連のメソッドが 2 つある:
    //   - setFocus(boolean)   : EditBox 固有。画面の init() 等で明示的に呼ばれる。
    //   - setFocused(boolean) : AbstractWidget 継承。ユーザーがクリックしたとき
    //                          Screen の mouseClicked から呼ばれる。
    // 両方をフックしないと、クリックによるフォーカス変更 (ModMenu 検索窓、
    // コマンドブロック等) を検知できず IME が有効にならない。

    @Inject(method = "setFocus", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocus(final boolean focused, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setFocused(focused || !canLoseFocus);
        }
    }

    // AbstractWidget.setFocused を EditBox インスタンス向けにオーバーライドしてフックする。
    // クリックによるフォーカス変更はこのパスを通る。
    @Inject(method = "setFocused", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocused(final boolean focused, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setFocused(focused || !canLoseFocus);
        }
    }

    @Inject(method = "setCanLoseFocus", at = @At("HEAD"), require = 0)
    private void featherCaramel$setCanLoseFocus(final boolean canLoseFocus, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && !canLoseFocus) {
            featherCaramel$wrapper.setFocused(true);
        }
    }

    // ---- ユーティリティ ----


    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void featherCaramel$renderUnderline(
            final com.mojang.blaze3d.vertex.PoseStack poseStack,
            final int mx, final int my, final float td, final CallbackInfo ci) {
        if (featherCaramel$wrapper == null) return;
        if (featherCaramel$wrapper.getStatus() != AbstractIMEWrapper.InputStatus.PREVIEW) return;
        final int fep = featherCaramel$wrapper.getFirstEndPos();
        final int ssp = featherCaramel$wrapper.getSecondStartPos();
        if (fep < 0 || ssp <= fep) return;
        final int padX = bordered ? 4 : 0;
        final int padY = bordered ? (height - 8) / 2 : 0;
        final int baseX = x + padX;
        final int uY    = y + padY + font.lineHeight;
        final int dp    = displayPos;
        final int c1    = Math.min(Math.max(fep, dp), value.length());
        final int c2    = Math.min(Math.max(ssp, dp), value.length());
        if (c2 <= c1) return;
        final int x1 = baseX + font.width(value.substring(dp, c1));
        final int x2 = baseX + font.width(value.substring(dp, c2));
        net.minecraft.client.gui.GuiComponent.fill(poseStack, x1, uY, x2, uY + 1, 0xFFFFFFFF);
    }
    @Unique
    private void featherCaramel$setStatusToNone() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setToNoneStatus();
    }

    @Unique
    private void featherCaramel$forceUpdateOrigin() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setOrigin(value);
    }
}
