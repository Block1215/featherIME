package dev.bl.feathercaramel.driver.arch.wayland;

import com.sun.jna.Native;
import dev.bl.feathercaramel.FeatherCaramelChatClient;
import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFWNativeWayland;

public final class WaylandController implements IController {

    static WaylandOperator focused;
    private final Driver_Wayland driver;
    private boolean              nativeInitialized = false;

    private final Driver_Wayland.PreeditCallback     preeditCb;
    private final Driver_Wayland.PreeditNullCallback preeditNullCb;
    private final Driver_Wayland.DoneCallback        doneCb;
    private final Driver_Wayland.RectCallback        rectCb;

    public WaylandController() {
        ModLogger.log("[Driver] Loading Wayland driver.");
        this.driver = Native.load(
            FeatherCaramelChatClient.copyLibrary("libcaramelchatwl.so"),
            Driver_Wayland.class
        );

        this.preeditCb = (str) -> {
            if (focused != null) focused.getWrapper().appendPreviewText(str.toString());
        };
        this.preeditNullCb = () -> {
            if (focused != null) focused.getWrapper().appendPreviewText("");
        };
        this.doneCb = (str) -> {
            if (focused != null) focused.getWrapper().insertText(str.toString());
        };
        this.rectCb = (rect) -> {
            if (focused != null) {
                final var   win    = Minecraft.getInstance().getWindow();
                final int   osScale = win.getHeight() / win.getScreenHeight();
                final float scale  = (float) win.getGuiScale();
                final float[] buff = focused.getWrapper().getRect().copy();
                buff[0] *= scale; buff[1] = (buff[1] * scale) / osScale;
                buff[2] *= scale; buff[3] *= scale;
                rect.write(0, buff, 0, 4);
                return 0;
            }
            return 1;
        };

        ModLogger.log("[Driver] Wayland driver loaded. Deferring native init until first focus.");
    }

    private void ensureNativeInitialized() {
        if (nativeInitialized) return;
        try {
            this.driver.initialize(
                GLFWNativeWayland.glfwGetWaylandDisplay(),
                preeditCb, preeditNullCb, doneCb, rectCb,
                (log) -> ModLogger.log("[Wayland/C] {}",   log),
                (log) -> ModLogger.error("[Wayland/C] {}", log),
                (log) -> ModLogger.debug("[Wayland/C] {}", log)
            );
            this.driver.setFocus(false);
            nativeInitialized = true;
            ModLogger.log("[Wayland] Native IME initialized.");
        } catch (Exception e) {
            ModLogger.error("[Wayland] Native init failed: {}", e.getMessage());
        }
    }

    @Override public IOperator createOperator(AbstractIMEWrapper wrapper) { return new WaylandOperator(this, wrapper); }

    @Override
    public void setFocus(boolean focus) {
        ensureNativeInitialized();
        if (nativeInitialized) driver.setFocus(focus);
    }

    @Override
    public void changeFocusedScreen(Screen screen) {
        if (WaylandController.focused != null) {
            WaylandController.focused.setFocused(false);
            WaylandController.focused = null;
        }
    }
}
