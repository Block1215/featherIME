package dev.bl.feathercaramel.driver.arch.unknown;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;

public final class UnknownOperator implements IOperator {
    @Override public IController getController() { return UnknownController.INSTANCE; }
    @Override public void setFocused(boolean f) {}
    @Override public boolean isFocused() { return false; }
}
