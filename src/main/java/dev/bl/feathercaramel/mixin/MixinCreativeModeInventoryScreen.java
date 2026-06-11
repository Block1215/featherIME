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

import java.util.Objects;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * クリエイティブ検索窓を IME 確定で即時検索させるための mixin。
 *
 * バニラの CreativeModeInventoryScreen は searchBox に setResponder を使わず、
 * 画面側の charTyped/keyPressed で「入力前後の値変化」を比較して refreshSearchResults() を
 * 呼ぶ方式になっている。IME 確定は native コールバックから EditBox へ直接挿入されるため、
 * これらのイベントを通らず、確定しても検索が走らない（次のキー入力でようやく走る）。
 *
 * そこで init() 後に searchBox へ responder を付与し、値が変わった時点で
 * refreshSearchResults() を呼ぶようにする。変換中 (PREVIEW) は MixinEditBox 側で
 * onValueChange をキャンセルしているため responder は発火せず、確定時のみ検索が走る。
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen {

    @Shadow private EditBox searchBox;

    @Shadow private void refreshSearchResults() {}

    @Unique private String featherCaramel$lastSearch;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$attachSearchResponder(final CallbackInfo ci) {
        if (searchBox != null) {
            featherCaramel$lastSearch = searchBox.getValue();
            // 値が実際に変わった時だけ検索する。setValue は同値でも responder を
            // 呼ぶため (フォーカス変更時の IME ラッパーの setValue(origin) など)、
            // 無条件に refreshSearchResults() するとスクロール位置が先頭にリセット
            // され、スロットクリックが別アイテムを掴む問題が起きる。
            searchBox.setResponder(s -> {
                if (!Objects.equals(s, featherCaramel$lastSearch)) {
                    featherCaramel$lastSearch = s;
                    this.refreshSearchResults();
                }
            });
        }
    }
}
