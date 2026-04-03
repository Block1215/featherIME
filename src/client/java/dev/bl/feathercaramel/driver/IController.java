package dev.bl.feathercaramel.driver;

import dev.bl.feathercaramel.driver.arch.darwin.DarwinController;
import dev.bl.feathercaramel.driver.arch.unknown.UnknownController;
import dev.bl.feathercaramel.driver.arch.wayland.WaylandController;
import dev.bl.feathercaramel.driver.arch.win.WinController;
import dev.bl.feathercaramel.driver.arch.x11.X11Controller;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
public interface IController {

    IOperator createOperator(AbstractIMEWrapper wrapper);
    void changeFocusedScreen(Screen screen);
    void setFocus(boolean focus);

    @Nullable
    default KeyboardStatus getKeyboardStatus() { return null; }

    static IController getController() {
        try {
            // GLFW 3.3 (MC 1.18.2以前) では glfwGetPlatform() が存在しないため
            // OS名での検出に統一する
            final String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win"))   return new WinController();
            if (os.contains("mac"))   return new DarwinController();
            if (os.contains("linux")) return new X11Controller();
        } catch (Exception e) {
            ModLogger.error("[FeatherIME] Error loading driver: {}", e.getMessage());
        }
        return UnknownController.INSTANCE;
    }
}
