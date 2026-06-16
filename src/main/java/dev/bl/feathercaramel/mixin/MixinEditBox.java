/*
 * featherIME - IME support for Minecraft, compatible with Feather Client.
 * Based on caramelChat by LemonCaramel <https://github.com/LemonCaramel/caramelChat>.
 *
 * Copyright (C) 2023 LemonCaramel
 * Copyright (C) 2024-2026 @Bl (modifications)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.bl.feathercaramel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Predicate;

@Mixin(value = EditBox.class, priority = 0)
public abstract class MixinEditBox extends GuiComponent implements EditBoxController {

    @Unique private WrapperEditBox featherCaramel$wrapper;
    @Unique private int            featherCaramel$cacheCursor;
    @Unique private int            featherCaramel$cacheHighlight;

    @Shadow private boolean canLoseFocus;
    @Shadow private boolean isEditable;
    @Shadow public  int     highlightPos;
    @Shadow public  int     cursorPos;
    @Shadow public  String  value;
    @Shadow public  int     displayPos;
    @Shadow private boolean bordered;
    @Shadow public  net.minecraft.client.gui.Font font;
    // Abstract @Shadow for getX/getY/getHeight removed: Feather loads no refMap, so
    // Mixin looks up the baked intermediary name (method_46426) directly in class_342.
    // If EditBox does not override the method from AbstractWidget, it won't be found.
    // Use @Unique wrappers instead — JVM dispatch resolves the inherited impl at runtime.
    @Unique private int featherCaramel$getX()      { return ((EditBox)(Object)this).getX();      }
    @Unique private int featherCaramel$getY()      { return ((EditBox)(Object)this).getY();      }
    @Unique private int featherCaramel$getHeight() { return ((EditBox)(Object)this).getHeight(); }

    @Override
    public WrapperEditBox featherCaramel$wrapper() { return featherCaramel$wrapper; }

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

    @Inject(method = "setValue", at = @At("HEAD"), require = 0)
    private void featherCaramel$setValueHead(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            featherCaramel$cacheCursor    = cursorPos;
            featherCaramel$cacheHighlight = highlightPos;
        } else {
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
            featherCaramel$forceUpdateOrigin();
        }
    }

    @Inject(method = "insertText", at = @At("HEAD"), require = 0)
    private void featherCaramel$insertTextHead(final String text, final CallbackInfo ci) {
        featherCaramel$setStatusToNone();
    }

    @Inject(method = "insertText", at = @At("TAIL"), require = 0)
    private void featherCaramel$insertTextTail(final String text, final CallbackInfo ci) {
        featherCaramel$forceUpdateOrigin();
    }

    @Inject(method = "onValueChange", at = @At("HEAD"), require = 0, cancellable = true)
    private void featherCaramel$onValueChange(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null
                && featherCaramel$wrapper.getStatus() == AbstractIMEWrapper.InputStatus.PREVIEW) {
            value = featherCaramel$wrapper.getOrigin();
            final int maxPos = value.length();
            if (cursorPos > maxPos) cursorPos = maxPos;
            if (highlightPos > maxPos) highlightPos = maxPos;
            // 変換中 (PREVIEW) は responder (検索窓の setResponder など) を発火させない。
            // ここで preview 文字列を検索してしまうと、確定後の最終値が直前の preview と
            // 一致して「変更なし」と見なされ検索がスキップされる (確定後に検索が走らない) バグになる。
            // 確定時 (status=NONE) は通常どおり onValueChange が走り responder が発火する。
            ci.cancel();
        }
    }

    @Inject(method = "deleteChars", at = @At("TAIL"), require = 0)
    private void featherCaramel$deleteCharsToPos(final int pos, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setOrigin(value);
        }
    }

    // IME の有効化は「編集可能 (isEditable)」な欄に限る。
    // 金床の名前欄は setCanLoseFocus(false) でフォーカス固定だが、アイテム未設置時は
    // setEditable(false) になっている。editable を見ずに IME を ON にすると、
    // 打てないのに OS 側で変換が始まり、その変換中文字列が IME コンテキストに残って
    // アイテム設置後に出現するバグになる。

    // 1.19.3 では EditBox のフォーカス設定メソッドは setFocused ではなく setFocus(boolean)。
    @Inject(method = "setFocus", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocused(final boolean focused, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setFocused((focused || !canLoseFocus) && isEditable);
        }
    }

    @Inject(method = "setCanLoseFocus", at = @At("HEAD"), require = 0)
    private void featherCaramel$setCanLoseFocus(final boolean canLoseFocus, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && !canLoseFocus && isEditable) {
            featherCaramel$wrapper.setFocused(true);
        }
    }

    @Inject(method = "setEditable", at = @At("TAIL"), require = 0)
    private void featherCaramel$setEditable(final boolean editable, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            final EditBox self = (EditBox) (Object) this;
            featherCaramel$wrapper.setFocused(editable && (self.isFocused() || !canLoseFocus));
        }
    }


    // 1.19.3 では AbstractWidget の描画メソッドは renderWidget ではなく renderButton。
    @Inject(method = "renderButton", at = @At("TAIL"), require = 0)
    private void featherCaramel$renderUnderline(
            final PoseStack g,
            final int mx, final int my, final float td, final CallbackInfo ci) {
        if (featherCaramel$wrapper == null) return;
        // 汎用フォーカス検出: setFocus/setFocused/changeFocus などメソッド単位の hook では
        // 一部画面 (ModMenu 検索窓のように開いた瞬間 changeFocus で自動フォーカスされる欄など) を
        // 取りこぼす。描画は可視 EditBox 毎フレーム呼ばれ実際の focused 状態を読めるので、
        // ここで毎フレーム IME フォーカスを同期する。setFocused は冪等＆非フォーカス operator への
        // false は no-op なので、全 EditBox がポーリングしても安全。
        featherCaramel$syncImeFocus();
        if (featherCaramel$wrapper.getStatus() != AbstractIMEWrapper.InputStatus.PREVIEW) return;
        final int fep = featherCaramel$wrapper.getFirstEndPos();
        final int ssp = featherCaramel$wrapper.getSecondStartPos();
        if (fep < 0 || ssp <= fep) return;
        final int padX = bordered ? 4 : 0;
        final int padY = bordered ? (featherCaramel$getHeight() - 8) / 2 : 0;
        final int baseX = featherCaramel$getX() + padX;
        final int uY    = featherCaramel$getY() + padY + font.lineHeight;
        final int dp    = displayPos;
        final int c1    = Math.min(Math.max(fep, dp), value.length());
        final int c2    = Math.min(Math.max(ssp, dp), value.length());
        if (c2 <= c1) return;
        final int x1 = baseX + font.width(value.substring(dp, c1));
        final int x2 = baseX + font.width(value.substring(dp, c2));
        fill(g, x1, uY, x2, uY + 1, 0xFFFFFFFF);
    }
    @Unique
    private void featherCaramel$syncImeFocus() {
        if (featherCaramel$wrapper == null) return;
        final EditBox self = (EditBox) (Object) this;
        // 実際にこの欄が入力を受け付けられる状態か (フォーカス + 編集可)。
        // canLoseFocus=false の常時フォーカス欄 (金床名など) も拾う。
        final boolean active = (self.isFocused() || !canLoseFocus) && isEditable;
        featherCaramel$wrapper.setFocused(active);
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
