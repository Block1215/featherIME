package dev.bl.feathercaramel.driver.arch.wayland;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;

public final class WaylandOperator implements IOperator {

    private final WaylandController  controller;
    private final AbstractIMEWrapper wrapper;
    private boolean                  nowFocused;

    public WaylandOperator(WaylandController c, AbstractIMEWrapper w) { controller = c; wrapper = w; }
    public AbstractIMEWrapper getWrapper() { return wrapper; }
    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[Wayland] setFocused: {}", focus);
        nowFocused = focus;
        if (focus) {
            WaylandController.focused = this;
            controller.setFocus(true);
        } else if (WaylandController.focused == this) {
            WaylandController.focused = null;
            controller.setFocus(false);
        }
    }

    @Override public boolean isFocused() { return nowFocused; }
}
