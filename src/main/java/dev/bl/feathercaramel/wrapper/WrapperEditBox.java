package dev.bl.feathercaramel.wrapper;

import dev.bl.feathercaramel.util.Rect;
import net.minecraft.client.gui.components.EditBox;

/**
 * EditBox 用 IME ラッパー。
 * accesswidener で開放した EditBox のフィールドに直接アクセスする。
 */
public final class WrapperEditBox extends AbstractIMEWrapper {

    private final EditBox wrapped;
    private Runnable insertCallback = () -> {};

    public WrapperEditBox(final EditBox box) {
        super(box.value);
        this.wrapped = box;
    }

    @Override
    protected void insert(final String text) {
        if (editable()) {
            wrapped.insertText(text);
            insertCallback.run();
        }
    }

    @Override protected int getCursorPos()    { return wrapped.cursorPos; }
    @Override protected int getHighlightPos() { return wrapped.highlightPos; }

    @Override
    public boolean editable() { return wrapped.canConsumeInput(); }

    @Override
    public boolean blockTyping() {
        if (!wrapped.canConsumeInput()) return true;
        final int start  = Math.min(wrapped.cursorPos, wrapped.highlightPos);
        final int end    = Math.max(wrapped.cursorPos, wrapped.highlightPos);
        final int remain = (wrapped.maxLength - wrapped.value.length()) - (start - end);
        return remain <= 0;
    }

    @Override
    protected String getTextWithPreview() { return wrapped.value; }

    @Override
    protected void setPreviewText(final String text) {
        valueChanged = true;
        wrapped.setValue(text);
        if (wrapped.isFocused()) insertCallback.run();
    }

    @Override
    public Rect getRect() {
        final int cursor = Math.min(wrapped.getCursorPosition(), wrapped.value.length());
        final int   xWidth = wrapped.font.width(wrapped.value.substring(0, cursor));
        final float x      = xWidth + wrapped.getX() + (wrapped.bordered ? 4 : 0);
        final float y      = wrapped.font.lineHeight + wrapped.getY()
                             + (wrapped.bordered ? ((wrapped.getHeight() - 8) / 2.0f) : 0);
        return new Rect(x, y, wrapped.getWidth(), wrapped.getHeight());
    }

    public void setInsertCallback(final Runnable cb) { insertCallback = cb; }
}
