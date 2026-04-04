package dev.bl.feathercaramel.driver.arch.x11;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import dev.bl.feathercaramel.FeatherCaramelChatClient;
import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeX11;

public final class X11Controller implements IController {

    static X11Operator focused;
    private static long WINDOW_ID = 0;

    private final Driver_X11 driver;
    private boolean           nativeInitialized = false;

    @SuppressWarnings("FieldCanBeLocal")
    private final Driver_X11.DrawCallback drawCb;
    @SuppressWarnings("FieldCanBeLocal")
    private final Driver_X11.DoneCallback doneCb;

    public X11Controller() {
        this.drawCb = (caret, chg_first, chg_length, length, iswstring,
                       rawstring, rawwstring, primary, secondary, tertiary) -> {
            final String text = iswstring ? rawwstring.toString() : rawstring;
            if (X11Controller.focused != null) {
                GLFW.glfwSetKeyCallback(WINDOW_ID, null);
                X11Controller.focused.getWrapper().appendPreviewText(text);
            }
            final int[]  pt  = { 600, 600 };
            final Memory mem = new Memory(8L);
            mem.write(0L, pt, 0, 2);
            return mem;
        };
        this.doneCb = () -> {
            if (X11Controller.focused != null)
                X11Controller.focused.getWrapper().insertText("");
            X11Controller.setupKeyboardEvent();
        };

        ModLogger.log("[Driver] Loading X11 driver.");
        this.driver = Native.load(
            FeatherCaramelChatClient.copyLibrary("libx11cocoainput.so"),
            Driver_X11.class
        );
        ModLogger.log("[Driver] X11 driver loaded. Deferring native init until first focus.");
    }

    private void ensureNativeInitialized() {
        if (nativeInitialized) return;
        try {
            WINDOW_ID = Minecraft.getInstance().getWindow().handle();
            X11Controller.setupKeyboardEvent();
            this.driver.initialize(
                WINDOW_ID,
                GLFWNativeX11.glfwGetX11Window(WINDOW_ID),
                drawCb, doneCb,
                (log) -> ModLogger.log("[X11/C] {}",   log),
                (log) -> ModLogger.error("[X11/C] {}", log),
                (log) -> ModLogger.debug("[X11/C] {}", log)
            );
            this.driver.set_focus(0);
            nativeInitialized = true;
            ModLogger.log("[X11] Native IME initialized.");
        } catch (Exception e) {
            ModLogger.error("[X11] Native init failed: {}", e.getMessage());
        }
    }

    public static void setupKeyboardEvent() {
        if (WINDOW_ID == 0) return;
        final Minecraft mc = Minecraft.getInstance();
        mc.keyboardHandler.setup(mc.getWindow());
        GLFW.glfwSetCharModsCallback(WINDOW_ID, (window, codepoint, mods) ->
            mc.execute(() -> {
                if (X11Controller.focused != null) {
                    X11Controller.focused.getWrapper()
                        .insertText(String.valueOf(Character.toChars(codepoint)));
                } else {
                    mc.keyboardHandler.charTyped(window, new CharacterEvent(codepoint, mods));
                }
            })
        );
    }

    @Override public IOperator createOperator(AbstractIMEWrapper wrapper) { return new X11Operator(this, wrapper); }

    @Override
    public void setFocus(boolean focus) {
        ensureNativeInitialized();
        if (nativeInitialized) driver.set_focus(focus ? 1 : 0);
    }

    @Override
    public void changeFocusedScreen(Screen screen) {
        if (X11Controller.focused != null) {
            X11Controller.focused.setFocused(false);
            X11Controller.focused = null;
        }
    }
}
