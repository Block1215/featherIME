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

import dev.bl.feathercaramel.util.Rect;
import dev.bl.feathercaramel.wrapper.WrapperTextFieldHelper;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
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
 * AbstractSignEditScreen (看板・吊り看板) の IME サポート。
 *
 * 看板は EditBox ではなく TextFieldHelper を使うため、
 * EditBox 向けミックスインでは対応できない。
 *
 * init() で TextFieldHelper が生成されるタイミングを @Redirect で捕捉し、
 * WrapperTextFieldHelper を作成して IME コントローラに登録する。
 * これにより @Shadow で private フィールドに触れることなく
 * refMap なし環境 (Feather Client 等) でも安全に動作する。
 */
@Mixin(AbstractSignEditScreen.class)
public abstract class MixinAbstractSignEditScreen extends Screen {

    @Unique private WrapperTextFieldHelper featherCaramel$signWrapper;

    protected MixinAbstractSignEditScreen() { super(Component.empty()); }

    /**
     * init() 内で new TextFieldHelper(...) が呼ばれる瞬間を横取りし、
     * 生成された TextFieldHelper を WrapperTextFieldHelper でラップして保存する。
     *
     * require = 0: この Redirect が失敗しても起動クラッシュしない。
     * その場合、看板では IME が機能しないが他の機能に影響しない。
     */
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
        // 看板のテキスト入力欄はおおよそ画面中央やや上に位置する。
        // getMsg / setMsg を WrapperTextFieldHelper に渡してインライン preview を有効化する。
        featherCaramel$signWrapper = new WrapperTextFieldHelper(
            helper,
            () -> new Rect(this.width / 2.0f, this.height * 0.35f, 10, 10),
            getMsg,
            setMsg
        );
        return helper;
    }

    /**
     * init() 完了後に IME フォーカスを設定する。
     * Redirect が成功した場合のみラッパーが存在する。
     */
    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$onInit(final CallbackInfo ci) {
        if (featherCaramel$signWrapper != null) {
            featherCaramel$signWrapper.setFocused(true);
        }
    }

    /**
     * 画面が閉じられるとき IME フォーカスを解除する。
     */
    @Inject(method = "removed", at = @At("HEAD"), require = 0)
    private void featherCaramel$onRemoved(final CallbackInfo ci) {
        if (featherCaramel$signWrapper != null) {
            featherCaramel$signWrapper.setFocused(false);
            featherCaramel$signWrapper = null;
        }
    }
}
