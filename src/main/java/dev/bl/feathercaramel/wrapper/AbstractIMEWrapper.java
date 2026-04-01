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
        status = InputStatus.PREVIEW;

        final int start   = Math.min(getCursorPos(), getHighlightPos());
        final int end     = Math.max(getCursorPos(), getHighlightPos());
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
            firstEndPos    = first.length();
            secondStartPos = firstEndPos + typing.length();
            setPreviewText(first + typing + second);
        }
    }

    /**
     * IME 確定テキストを挿入する。
     * origin をプレビュー無しに戻してから editbox.insertText() に渡す。
     */
    public final void insertText(final String input) {
        if (blockTyping() || !editable()) return;

        ModLogger.debug("[IME] insertText current=({}) input=({})", origin, input);
        status         = InputStatus.NONE;
        firstEndPos    = -1;
        secondStartPos = -1;

        setPreviewText(origin);   // プレビューを消す
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
