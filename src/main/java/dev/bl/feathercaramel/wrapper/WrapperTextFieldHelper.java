package dev.bl.feathercaramel.wrapper;

import dev.bl.feathercaramel.util.Rect;
import net.minecraft.client.gui.font.TextFieldHelper;

import java.util.function.Supplier;

/**
 * TextFieldHelper 用 IME ラッパー。
 *
 * 看板 (AbstractSignEditScreen) や本 (BookEditScreen) など、
 * EditBox ではなく TextFieldHelper を使う画面向けの実装。
 *
 * プリエディット (変換中テキスト) のインライン表示は行わない。
 * OS 側の IME ウィンドウで変換を行い、確定した文字のみを挿入する。
 * これにより TextFieldHelper のカーソル位置管理との競合を避ける。
 */
public final class WrapperTextFieldHelper extends AbstractIMEWrapper {

    private final TextFieldHelper helper;
    private final Supplier<Rect>  rectGetter;

    /**
     * @param helper     ラップする TextFieldHelper
     * @param rectGetter IME ウィンドウ配置用の矩形を返すサプライヤ
     */
    public WrapperTextFieldHelper(final TextFieldHelper helper, final Supplier<Rect> rectGetter) {
        super("");
        this.helper    = helper;
        this.rectGetter = rectGetter;
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

    /**
     * TextFieldHelper が保持するテキストは内部 Supplier 経由でしか取得できないため、
     * ここでは空文字を返す。
     * この wrapper はプリエディット表示を行わないため、
     * getTextWithPreview() の返値は origin の更新チェックにしか使われない。
     * その用途では空文字でも問題ない。
     */
    @Override
    protected String getTextWithPreview() { return ""; }

    /**
     * プリエディット表示を行わない。
     * OS 側の IME ウィンドウが変換を担当し、確定文字のみ insert() に来る。
     * valueChanged フラグを確実にリセットしてリエントランスを防ぐ。
     */
    @Override
    protected void setPreviewText(final String text) {
        valueChanged = false;
    }

    @Override
    public Rect getRect() { return rectGetter.get(); }
}
