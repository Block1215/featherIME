/*
 * featherIME - IME support for Minecraft, compatible with Feather Client.
 * Based on caramelChat by LemonCaramel <https://github.com/LemonCaramel/caramelChat>.
 *
 * Copyright (C) 2023 LemonCaramel
 * Copyright (C) 2024-2026 @Bl (modifications)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.bl.feathercaramel.driver.arch.win;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
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
            final long glfwWin = Minecraft.getInstance().getWindow().handle();
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

    /** セッション中の最初の入力欄フォーカスかどうか */
    private boolean firstFocusDone = false;

    /**
     * 現在の IME 状態を表す Java 側フラグ。
     * 表示判定はこの値を使う。OS の getStatus() を毎フレーム鵜呑みにするのではなく、
     * 「変化を検知した時だけ書き換える」ことで、強制 [ENG] の効果を維持する。
     */
    private volatile boolean flagIsJapanese = false;

    /** 前回ポーリングした OS 側の状態（変化検出用） */
    private boolean lastDriverNative = false;

    @Override
    public void setFocus(boolean focus) {
        ensureNativeInitialized();
        if (!nativeInitialized) return;
        this.driver.set_focus(focus ? 1 : 0);
        if (focus && !firstFocusDone) {
            // セッション最初の入力欄フォーカス時のみ [ENG] にデフォルト設定。
            forceImeModeHalfwidthEnglish();
            flagIsJapanese = false;       // 表示用フラグも [ENG] にする
            lastDriverNative = isImeInNativeMode();  // 変化検出のベースライン
            firstFocusDone = true;
        }
    }

    /**
     * Windows IME の変換モードを「半角英数」に設定する。
     * imm32.dll を JNA 経由で直接呼ぶことで、ネイティブ DLL の再ビルドなしに対応する。
     */
    private void forceImeModeHalfwidthEnglish() {
        try {
            final long glfwWin = Minecraft.getInstance().getWindow().handle();
            final long hwndPtr = GLFWNativeWin32.glfwGetWin32Window(glfwWin);
            if (hwndPtr == 0) return;
            final Pointer hwnd = new Pointer(hwndPtr);
            final Pointer himc = Imm32.INSTANCE.ImmGetContext(hwnd);
            if (himc == null || Pointer.nativeValue(himc) == 0) return;
            try {
                final IntByReference conv = new IntByReference();
                final IntByReference sent = new IntByReference();
                if (Imm32.INSTANCE.ImmGetConversionStatus(himc, conv, sent)) {
                    int newConv = conv.getValue();
                    newConv &= ~Imm32.IME_CMODE_NATIVE;     // NATIVE off → 英数
                    newConv &= ~Imm32.IME_CMODE_FULLSHAPE;  // FULLSHAPE off → 半角
                    Imm32.INSTANCE.ImmSetConversionStatus(himc, newConv, sent.getValue());
                }
                // IME を閉じる = 半角英数モード相当
                Imm32.INSTANCE.ImmSetOpenStatus(himc, false);
            } finally {
                Imm32.INSTANCE.ImmReleaseContext(hwnd, himc);
            }
        } catch (Throwable t) {
            ModLogger.debug("[Win] forceImeModeHalfwidthEnglish failed: {}", t.getMessage());
        }
    }

    /**
     * 現在の IME が「全角入力モード」かどうかを Java 側で直接判定する。
     *
     * NATIVE bit だけでなく ImmGetOpenStatus も見る:
     *   - IME が閉じている → 半角英数モード相当 ([ENG])
     *   - IME が開いている + NATIVE bit on → 全角 ([あ])
     *   - IME が開いている + NATIVE bit off → 半角英数 ([ENG])
     *
     * 半角/全角キーで IME を閉じても NATIVE bit が残っているケースがあるため、
     * NATIVE bit だけでは [ENG] と判定できず [あ] が表示され続けるバグになる。
     */
    private boolean isImeInNativeMode() {
        try {
            final long glfwWin = Minecraft.getInstance().getWindow().handle();
            final long hwndPtr = GLFWNativeWin32.glfwGetWin32Window(glfwWin);
            if (hwndPtr == 0) return false;
            final Pointer hwnd = new Pointer(hwndPtr);
            final Pointer himc = Imm32.INSTANCE.ImmGetContext(hwnd);
            if (himc == null || Pointer.nativeValue(himc) == 0) return false;
            try {
                // IME が閉じていれば必ず [ENG]
                if (!Imm32.INSTANCE.ImmGetOpenStatus(himc)) return false;
                // 開いていれば NATIVE bit を見る
                final IntByReference conv = new IntByReference();
                final IntByReference sent = new IntByReference();
                if (Imm32.INSTANCE.ImmGetConversionStatus(himc, conv, sent)) {
                    return (conv.getValue() & Imm32.IME_CMODE_NATIVE) != 0;
                }
            } finally {
                Imm32.INSTANCE.ImmReleaseContext(hwnd, himc);
            }
        } catch (Throwable t) {
            ModLogger.debug("[Win] isImeInNativeMode failed: {}", t.getMessage());
        }
        return false;
    }

    @Override
    public KeyboardStatus getKeyboardStatus() {
        if (!nativeInitialized) return null;

        // 表示は Java 側のフラグ (flagIsJapanese) を使う。
        // OS 側状態が変化した時だけフラグを書き換える。
        // ※ NATIVE bit だけでなく ImmGetOpenStatus も見ることで、
        //   半角/全角キーで IME を閉じた状態を正しく [ENG] と判定する。
        final boolean driverNow = isImeInNativeMode();
        if (driverNow != lastDriverNative) {
            flagIsJapanese = driverNow;
            lastDriverNative = driverNow;
        }

        final Language lang = Driver_Win.LAYOUT_MAP.getOrDefault(
            driver.getKeyboardLayout(), Language.OTHER
        );
        return new KeyboardStatus(lang, flagIsJapanese);
    }
}
