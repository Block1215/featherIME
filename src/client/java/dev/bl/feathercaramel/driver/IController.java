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
import org.lwjgl.glfw.GLFW;

public interface IController {

    IOperator createOperator(AbstractIMEWrapper wrapper);
    void changeFocusedScreen(Screen screen);
    void setFocus(boolean focus);

    @Nullable
    default KeyboardStatus getKeyboardStatus() { return null; }

    static IController getController() {
        try {
            int platform = GLFW.glfwGetPlatform();
            // GLFW未初期化時は0が返るのでOS検出にフォールバック
            if (platform == 0) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) platform = GLFW.GLFW_PLATFORM_WIN32;
                else if (os.contains("mac")) platform = GLFW.GLFW_PLATFORM_COCOA;
                else if (os.contains("linux")) platform = GLFW.GLFW_PLATFORM_X11;
                ModLogger.log("[FeatherIME] GLFW not initialized, using OS detection: {}", platform);
            }
            return switch (platform) {
                case GLFW.GLFW_PLATFORM_WIN32   -> new WinController();
                case GLFW.GLFW_PLATFORM_COCOA   -> new DarwinController();
                case GLFW.GLFW_PLATFORM_X11     -> new X11Controller();
                case GLFW.GLFW_PLATFORM_WAYLAND -> new WaylandController();
                default -> throw new UnsupportedOperationException("Platform: " + platform);
            };
        } catch (Exception e) {
            ModLogger.error("[FeatherIME] Error loading driver: {}", e.getMessage());
        }
        return UnknownController.INSTANCE;
    }
}
