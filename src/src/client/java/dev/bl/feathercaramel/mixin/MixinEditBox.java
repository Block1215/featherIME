package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
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
 * 1.20.1 対応のポイント:
 *   - moveCursorToEnd() は 1.20.1 では引数なし、1.20.2+ では boolean 引数あり。
 *     INVOKE ターゲットを使うと 1.20.1 でフックが適用されないため TAIL を使用。
 *   - onValueChange(String) は 1.20.1 に存在しない可能性があるため TAIL を使用。
 *   - deleteCharsToPos のフックを追加しないと、バックスペース後に origin が
 *     古い値のまま残り「削除した文字が再入力される」バグが発生する。
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

    // ---- EditBoxController 実装 ----
    @Override
    public WrapperEditBox featherCaramel$wrapper() { return featherCaramel$wrapper; }

    // ---- 初期化 ----

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
    //   HEAD  → カーソル位置を保存 / 通常編集ならstatusをNoneに
    //   Redirect(Predicate.test) → valueChanged=true の間は filter をバイパス
    //   TAIL  → valueChanged=true ならカーソル復元＆フラグリセット
    //            通常編集なら origin を現在の value で更新
    //
    // ※ 旧実装では "INVOKE before moveCursorToEnd(Z)V" でキャンセルしていたが、
    //   1.20.1 では moveCursorToEnd の引数なしバージョンが使われるためフックが
    //   適用されず valueChanged が永遠に true のまま残るバグがあった。
    //   TAIL インジェクションに変更することで全バージョンで確実に動作する。

    @Inject(method = "setValue", at = @At("HEAD"), require = 0)
    private void featherCaramel$setValueHead(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            // プレビュー用 setValue: 現在のカーソル位置を保存しておく
            featherCaramel$cacheCursor    = cursorPos;
            featherCaramel$cacheHighlight = highlightPos;
        } else {
            // 通常の setValue: IME状態をリセット
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
            // プレビュー用 setValue: フィルタをバイパスして無条件に値を通す
            return true;
        }
        return pred.test((String) v);
    }

    @Inject(method = "setValue", at = @At("TAIL"), require = 0)
    private void featherCaramel$setValueTail(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            // プレビュー用 setValue の後処理:
            // moveCursorToEnd() によってカーソルが末尾に動かされるので元の位置に戻す。
            // その後 valueChanged フラグをリセットする。
            cursorPos    = featherCaramel$cacheCursor;
            highlightPos = featherCaramel$cacheHighlight;
            featherCaramel$wrapper.valueChanged = false;
        } else {
            // 通常の setValue の後処理: origin を現在の value で更新
            featherCaramel$forceUpdateOrigin();
        }
    }

    // ---- insertText フック ----
    //
    // ※ 旧実装では "INVOKE before onValueChange(String)" でフックしていたが、
    //   1.20.1 では onValueChange メソッド自体が存在しないためフックが適用されず
    //   insertText 後に origin が更新されないバグがあった。
    //   TAIL に変更することで全バージョンで確実に動作する。

    @Inject(method = "insertText", at = @At("HEAD"), require = 0)
    private void featherCaramel$insertTextHead(final String text, final CallbackInfo ci) {
        featherCaramel$setStatusToNone();
    }

    @Inject(method = "insertText", at = @At("TAIL"), require = 0)
    private void featherCaramel$insertTextTail(final String text, final CallbackInfo ci) {
        // insertText 完了後に origin を最新の value で更新する
        featherCaramel$forceUpdateOrigin();
    }

    // onValueChange が存在するバージョン向けフック（存在しなければ無視される）
    // ※ 1.20.1 では存在しない可能性がある

    // ---- keyPressed フック ----
    //
    // 1.20.1 では deleteCharsToPos が存在しないため、keyPressed でバックスペース/削除キーを
    // 検出して削除後に origin を更新する。これがないと削除後の古い文字列が残り、
    // 次の IME 入力時に「削除したはずの文字が再出現する」バグが発生する。

    @Inject(method = "keyPressed", at = @At("TAIL"), require = 0)
    private void featherCaramel$keyPressed(final int keyCode, final int scanCode, final int modifiers, final CallbackInfoReturnable<Boolean> cir) {
        if (featherCaramel$wrapper != null) {
            // Backspace (259), Delete (261) などの削除キーが押された可能性がある
            // value が変更されたら origin を更新する
            if (!featherCaramel$wrapper.valueChanged) {
                featherCaramel$forceUpdateOrigin();
            }
        }
    }

    // ---- setFocused フック ----

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

    @Unique
    private void featherCaramel$setStatusToNone() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setToNoneStatus();
    }

    @Unique
    private void featherCaramel$forceUpdateOrigin() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setOrigin(value);
    }
}
