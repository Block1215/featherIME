package dev.bl.feathercaramel.driver.arch.darwin;

import com.sun.jna.Native;
import dev.bl.feathercaramel.FeatherCaramelChatClient;
import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.driver.KeyboardStatus;
import dev.bl.feathercaramel.driver.KeyboardStatus.Language;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;
import java.util.Locale;

public final class DarwinController implements IController {

    private final Driver_Darwin driver;
    private boolean             nativeInitialized = false;

    public DarwinController() {
        ModLogger.log("[Driver] Loading Darwin driver.");
        this.driver = Native.load(
            FeatherCaramelChatClient.copyLibrary("libdarwincocoainput.dylib"),
            Driver_Darwin.class
        );
        // initialize() は最初の Operator 生成時に実行
        ensureNativeInitialized();
    }

    private void ensureNativeInitialized() {
        if (nativeInitialized) return;
        try {
            this.driver.initialize(
                (log) -> ModLogger.log("[Darwin/C] {}",   log),
                (log) -> ModLogger.error("[Darwin/C] {}", log),
                (log) -> ModLogger.debug("[Darwin/C] {}", log)
            );
            nativeInitialized = true;
            ModLogger.log("[Darwin] Native IME initialized.");
        } catch (Exception e) {
            ModLogger.error("[Darwin] Native init failed: {}", e.getMessage());
        }
    }

    public Driver_Darwin getDriver() { return driver; }

    @Override
    public IOperator createOperator(AbstractIMEWrapper wrapper) {
        return new DarwinOperator(this, wrapper);
    }

    @Override
    public void changeFocusedScreen(Screen screen) {
        if (nativeInitialized) driver.refreshInstance();
    }

    @Override
    public void setFocus(boolean focus) { /* Darwin は Operator 側で管理 */ }

    @Override
    public KeyboardStatus getKeyboardStatus() {
        if (!nativeInitialized) return null;
        final String src = driver.getStatus();
        if (src == null) return null;
        return new KeyboardStatus(parseSource(src.toLowerCase(Locale.ENGLISH)), true);
    }

    private static Language parseSource(String s) {
        if (s.contains("abc"))     return Language.ENGLISH;
        if (s.contains("korean"))  return Language.KOREAN;
        if (s.contains("kotoeri") || s.contains("japanese")) return Language.JAPANESE;
        if (s.contains("scim"))    return Language.CHINESE_SIMPLIFIED;
        if (s.contains("tcim"))    return Language.CHINESE_TRADITIONAL;
        return Language.OTHER;
    }
}
