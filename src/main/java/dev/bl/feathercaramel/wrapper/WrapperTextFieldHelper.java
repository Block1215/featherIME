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
package dev.bl.feathercaramel.wrapper;

import dev.bl.feathercaramel.util.Rect;
import net.minecraft.client.gui.font.TextFieldHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TextFieldHelper 用 IME ラッパー。
 *
 * 看板 (AbstractSignEditScreen) や本 (BookEditScreen) など、
 * EditBox ではなく TextFieldHelper を使う画面向けの実装。
 *
 * textGetter / textSetter が渡された場合 (看板)、setPreviewText で
 * その setter にプレビュー込みのテキストを書き込みインライン表示する。
 * 渡されない場合 (本など) は IME フォーカス管理のみで preview 表示はしない。
 */
public final class WrapperTextFieldHelper extends AbstractIMEWrapper {

    private final TextFieldHelper helper;
    private final Supplier<Rect>  rectGetter;
    private final Supplier<String> textGetter;
    private final Consumer<String> textSetter;

    /**
     * Preview 表示なし (IME フォーカスのみ) のコンストラクタ。
     * 本など。
     */
    public WrapperTextFieldHelper(final TextFieldHelper helper, final Supplier<Rect> rectGetter) {
        this(helper, rectGetter, null, null);
    }

    /**
     * Preview インライン表示ありのコンストラクタ。
     * 看板など、textGetter/textSetter 経由で表示中のテキストを取得・更新できる場合。
     */
    public WrapperTextFieldHelper(
            final TextFieldHelper helper,
            final Supplier<Rect> rectGetter,
            final Supplier<String> textGetter,
            final Consumer<String> textSetter) {
        super(textGetter != null ? safeGet(textGetter) : "");
        this.helper     = helper;
        this.rectGetter = rectGetter;
        this.textGetter = textGetter;
        this.textSetter = textSetter;
    }

    private static String safeGet(final Supplier<String> g) {
        try {
            final String s = g.get();
            return s != null ? s : "";
        } catch (Throwable t) { return ""; }
    }

    // ---- AbstractIMEWrapper 実装 ----

    @Override
    protected void insert(final String text) {
        if (!text.isEmpty()) {
            helper.insertText(text);
        }
    }

    @Override
    protected int getCursorPos() { return helper.getCursorPos(); }

    @Override
    protected int getHighlightPos() { return helper.getSelectionPos(); }

    @Override
    public boolean editable() { return true; }

    @Override
    public boolean blockTyping() { return false; }

    @Override
    protected String getTextWithPreview() {
        if (textGetter != null) {
            return safeGet(textGetter);
        }
        return "";
    }

    /**
     * Preview を表示する。
     * textSetter が設定されていれば、preview 込みのテキストを書き込んで
     * インライン表示する。同時にカーソルを preview 末尾に動かす。
     */
    @Override
    protected void setPreviewText(final String text) {
        valueChanged = false;
        if (textSetter == null) {
            // 本など: preview 表示なし
            return;
        }

        try {
            textSetter.accept(text != null ? text : "");
            // カーソルを preview 末尾 (secondStartPos) に移動して、
            // 後続の helper.insertText() が正しい位置で動くようにする。
            final int ssp = getSecondStartPos();
            if (ssp >= 0) {
                helper.setCursorPos(ssp, false);
            }
        } catch (Throwable t) {
            // 失敗しても致命的でないので無視
        }
    }

    @Override
    public Rect getRect() { return rectGetter.get(); }
}
