package dev.bl.feathercaramel.driver.arch.win;

import com.sun.jna.Native;
import dev.bl.feathercaramel.FeatherCaramelChatClient;
import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.driver.KeyboardStatus;
import dev.bl.feathercaramel.driver.KeyboardStatus.Language;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFWNativeWin32;

public final class WinController implements IController {

    static WinOperator focused;

    private final Driver_Win driver;
    private boolean          nativeInitialized = false;

    // コールバックを先にフィールド確保（GC対策 + initialize 遅延のため）
    private final Driver_Win.PreeditCallback  preeditCb;
    private final Driver_Win.DoneCallback     doneCb;
    private final Driver_Win.RectCallback     rectCb;
    private final Driver_Win.LogInfoCallback  logCb;
    private final Driver_Win.LogErrorCallback errCb;
    private final Driver_Win.LogDebugCallback dbgCb;

    public WinController() {
        ModLogger.log("[Driver] Loading Windows driver.");
        this.driver = Native.load(
            FeatherCaramelChatClient.copyLibrary("libwincocoainput.dll"),
            Driver_Win.class
        );

        // コールバックだけ先に生成。initialize() は setFocus 初回時まで遅延。
        this.preeditCb = (str, cursor, length) -> {
            if (focused != null) {
                ModLogger.debug("[Win] Preedit: ({}) cursor={} len={}", str, cursor, length);
                focused.getWrapper().appendPreviewText(str.toString());
            }
        };
        this.doneCb = (str) -> {
            if (focused != null) {
                ModLogger.debug("[Win] Commit: ({})", str);
                focused.getWrapper().insertText(str.toString());
            }
        };
        this.rectCb = (rect) -> {
            if (focused != null) {
                final float[] buff  = focused.getWrapper().getRect().copy();
                final float   scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
                buff[0] *= scale; buff[1] *= scale;
                buff[2] *= scale; buff[3] *= scale;
                rect.write(0, buff, 0, 4);
                return 0;
            }
            return 1;
        };
        this.logCb = (log) -> ModLogger.log("[Win/C] {}",   log);
        this.errCb = (log) -> ModLogger.error("[Win/C] {}", log);
        this.dbgCb = (log) -> ModLogger.debug("[Win/C] {}", log);

        ModLogger.log("[Driver] Windows driver loaded. Deferring native init until first focus.");
    }

    /**
     * 最初の setFocus 呼び出し時に HWND を取得して initialize() を実行する。
     * Feather では onInitializeClient() 時点でウィンドウが null のことがあるため遅延が必要。
     */
    private void ensureNativeInitialized() {
        if (nativeInitialized) return;
        try {
            final long glfwWin = Minecraft.getInstance().getWindow().getWindow();
            final long hwnd    = GLFWNativeWin32.glfwGetWin32Window(glfwWin);
            if (hwnd == 0) {
                ModLogger.error("[Win] HWND is 0, cannot initialize IME.");
                return;
            }
            this.driver.initialize(hwnd, preeditCb, doneCb, rectCb, logCb, errCb, dbgCb);
            nativeInitialized = true;
            ModLogger.log("[Win] Native IME initialized. hwnd=0x{}", Long.toHexString(hwnd));
        } catch (Exception e) {
            ModLogger.error("[Win] Native init failed: {}", e.getMessage());
        }
    }

    @Override
    public IOperator createOperator(AbstractIMEWrapper wrapper) {
        return new WinOperator(this, wrapper);
    }

    @Override
    public void changeFocusedScreen(Screen screen) {
        if (WinController.focused != null) {
            WinController.focused.setFocused(false);
            WinController.focused = null;
        }
    }

    @Override
    public void setFocus(boolean focus) {
        ensureNativeInitialized();
        if (!nativeInitialized) return;
        this.driver.set_focus(focus ? 1 : 0);
    }

    @Override
    public KeyboardStatus getKeyboardStatus() {
        if (!nativeInitialized) return null;
        final Language lang = Driver_Win.LAYOUT_MAP.getOrDefault(
            driver.getKeyboardLayout(), Language.OTHER
        );
        return new KeyboardStatus(lang, driver.getStatus() != 0);
    }
}
