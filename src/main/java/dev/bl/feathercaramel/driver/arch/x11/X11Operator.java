package dev.bl.feathercaramel.driver.arch.x11;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;

public final class X11Operator implements IOperator {

    private final X11Controller      controller;
    private final AbstractIMEWrapper wrapper;
    private boolean                  nowFocused;

    public X11Operator(X11Controller c, AbstractIMEWrapper w) { controller = c; wrapper = w; }
    public AbstractIMEWrapper getWrapper() { return wrapper; }
    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[X11] setFocused: {}", focus);
        nowFocused = focus;
        if (focus) {
            X11Controller.focused = this;
            controller.setFocus(true);
        } else if (X11Controller.focused == this) {
            wrapper.insertText("");
            X11Controller.focused = null;
            controller.setFocus(false);
            X11Controller.setupKeyboardEvent();
        }
    }

    @Override public boolean isFocused() { return nowFocused; }
}
