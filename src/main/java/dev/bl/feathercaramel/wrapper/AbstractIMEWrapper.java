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

import dev.bl.feathercaramel.FeatherCaramelChatClient;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.util.Rect;

/**
 * IME ラッパーの抽象基底クラス。
 * caramelChat の AbstractIMEWrapper を feathercaramel パッケージに移植。
 *
 * 動作フロー:
 *   C (JNA) → appendPreviewText() : IME変換中テキストを EditBox.value に埋め込み表示
 *   C (JNA) → insertText()        : IME確定テキストを EditBox.insertText() で挿入
 */
public abstract class AbstractIMEWrapper {

    private final IOperator ime;

    public enum InputStatus { NONE, PREVIEW }

    private InputStatus status     = InputStatus.NONE;
    private int firstEndPos        = -1;
    private int secondStartPos     = -1;
    /** PREVIEW 開始時の元のカーソル位置。複数回 appendPreviewText が呼ばれても
     *  preview cursor ではなく「最初の位置」を基準に使う。 */
    private int originalCursor     = -1;
    private int originalHighlight  = -1;
    protected String origin;

    /** 確定済みテキストが変更されたかどうか (setValue のリダイレクト判定用) */
    public boolean valueChanged = false;

    protected AbstractIMEWrapper(final String defValue) {
        this.origin = defValue;
        this.ime    = FeatherCaramelChatClient.getController().createOperator(this);
    }

    public IOperator getIme()          { return ime; }
    public InputStatus getStatus()     { return status; }
    public int getFirstEndPos()        { return firstEndPos; }
    public int getSecondStartPos()     { return secondStartPos; }

    public final void setToNoneStatus() {
        status = InputStatus.NONE;
        originalCursor = -1;
        originalHighlight = -1;
        setPreviewText(origin);
    }

    public final void setFocused(boolean focused) {
        ime.setFocused(focused);
    }

    public final String getOrigin() { return origin; }

    public final void setOrigin() {
        setOrigin(getTextWithPreview());
    }

    public final void setOrigin(final String value) {
        origin = value;
    }

    // ---- IME プリエディット ----

    /**
     * IME 変換中テキスト (preedit) を EditBox.value に「仮挿入」する。
     * カーソル位置に typing を埋め込み、下線表示で変換中であることを示す。
     */
    public final void appendPreviewText(final String typing) {
        if (!editable()) return;

        ModLogger.debug("[IME] appendPreview current=({}) preview=({})", origin, typing);

        // 前回が PREVIEW でなければ、現在の値・カーソル位置を保存する。
        // ※ 二回目以降の appendPreviewText では cursor が preview text 内に
        //   ずれているため、毎回 getCursorPos() を使うと挿入位置が右にずれる。
        if (status != InputStatus.PREVIEW) {
            origin = getTextWithPreview();
            originalCursor    = getCursorPos();
            originalHighlight = getHighlightPos();
        }

        status = InputStatus.PREVIEW;

        final int start   = Math.min(Math.min(originalCursor, originalHighlight), origin.length());
        final int end     = Math.min(Math.max(originalCursor, originalHighlight), origin.length());
        final boolean same = (start == end);
        final int lastPos  = origin.length();

        if (lastPos != end && same) {
            // カーソルが末尾以外
            final String first  = origin.substring(0, end);
            final String second = origin.substring(end);
            firstEndPos     = first.length();
            secondStartPos  = firstEndPos + typing.length();
            setPreviewText(first + typing + second);
        } else if (same) {
            // カーソルが末尾
            final String result = origin + typing;
            firstEndPos    = origin.length();
            secondStartPos = result.length();
            setPreviewText(result);
        } else {
            // 範囲選択中: 選択を削除してからプレビュー挿入
            final String first  = origin.substring(0, start);
            final String second = origin.substring(end);
            insert("");
            origin = getTextWithPreview();
            // insert("") は setStatusToNone を呼んで originalCursor を -1 にリセットするので、
            // 削除後のカーソル位置で再保存する。
            originalCursor    = first.length();
            originalHighlight = first.length();
            firstEndPos    = first.length();
            secondStartPos = firstEndPos + typing.length();
            setPreviewText(first + typing + second);
            // 同じく status も NONE に戻されているので PREVIEW に戻す
            status = InputStatus.PREVIEW;
        }
    }

    /**
     * IME 確定テキストを挿入する。
     * origin をプレビュー無しに戻してから editbox.insertText() に渡す。
     */
    public final void insertText(final String input) {
        if (blockTyping() || !editable()) return;

        ModLogger.debug("[IME] insertText current=({}) input=({})", origin, input);

        // 元のカーソル位置を保存（プレビュー開始位置）。
        // setPreviewText 後にカーソルをここへ復元してから insert する。
        final int restoreCursor = originalCursor;

        status         = InputStatus.NONE;
        firstEndPos    = -1;
        originalCursor    = -1;
        originalHighlight = -1;

        // secondStartPos を一時的に restoreCursor にセットすると、
        // MixinEditBox.setValueTail が cursor を min(restoreCursor, len) に復元してくれる。
        // これにより MC の insertText body が「元の場所」に挿入する。
        secondStartPos = (restoreCursor >= 0) ? restoreCursor : -1;
        setPreviewText(origin);   // プレビューを消し、カーソルを元の位置に復元
        secondStartPos = -1;      // すぐにクリア（後続呼び出しの妨げにならないように）

        insert(input);            // 確定テキストを挿入
        origin = getTextWithPreview();
    }

    // ---- サブクラスが実装する抽象メソッド ----

    protected abstract void insert(String text);
    protected abstract int getCursorPos();
    protected abstract int getHighlightPos();
    public abstract boolean editable();
    public abstract boolean blockTyping();
    protected abstract String getTextWithPreview();
    protected abstract void setPreviewText(String text);
    public abstract Rect getRect();
}
