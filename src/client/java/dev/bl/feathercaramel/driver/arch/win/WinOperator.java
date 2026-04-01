package dev.bl.feathercaramel.driver.arch.win;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;

public final class WinOperator implements IOperator {

    private final WinController controller;
    private final AbstractIMEWrapper wrapper;
    private boolean nowFocused;

    public WinOperator(WinController controller, AbstractIMEWrapper wrapper) {
        this.controller = controller;
        this.wrapper    = wrapper;
    }

    public AbstractIMEWrapper getWrapper() { return wrapper; }

    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[Win] setFocused: {}", focus);
        nowFocused = focus;
        if (focus) {
            WinController.focused = this;
            controller.setFocus(true);
        } else if (WinController.focused == this) {
            WinController.focused = null;
            controller.setFocus(false);
        }
    }

    @Override public boolean isFocused() { return nowFocused; }
}
