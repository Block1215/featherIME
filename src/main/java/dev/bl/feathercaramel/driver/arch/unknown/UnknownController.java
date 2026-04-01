package dev.bl.feathercaramel.driver.arch.unknown;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;

public final class UnknownController implements IController {
    public static final IController INSTANCE = new UnknownController();

    private UnknownController() {
        ModLogger.log("[Driver] Unknown platform — IME disabled.");
    }

    @Override public IOperator createOperator(AbstractIMEWrapper w) { return new UnknownOperator(); }
    @Override public void changeFocusedScreen(Screen s) {}
    @Override public void setFocus(boolean f) {}
}
