package dev.bl.feathercaramel.driver;

import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public interface IOperator {
    IController getController();
    void setFocused(boolean focus);
    boolean isFocused();
}
