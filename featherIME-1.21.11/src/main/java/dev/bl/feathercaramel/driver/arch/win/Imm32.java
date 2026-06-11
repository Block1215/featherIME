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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Windows imm32.dll の JNA インターフェース。
 * IME の変換モード (全角/半角・かな/英数) を Java から制御するために使う。
 */
public interface Imm32 extends Library {

    Imm32 INSTANCE = Native.load("imm32", Imm32.class);

    /** IME_CMODE_NATIVE: 1=かな/漢字 (=[あ]), 0=英数 (=[ENG]) */
    int IME_CMODE_NATIVE    = 0x0001;
    /** IME_CMODE_FULLSHAPE: 1=全角, 0=半角 */
    int IME_CMODE_FULLSHAPE = 0x0008;

    Pointer ImmGetContext(Pointer hwnd);
    boolean ImmReleaseContext(Pointer hwnd, Pointer himc);
    boolean ImmGetConversionStatus(Pointer himc, IntByReference conversion, IntByReference sentence);
    boolean ImmSetConversionStatus(Pointer himc, int conversion, int sentence);
    /** IME が開いているか (合成可能状態か)。閉じている = 半角英数モード相当 */
    boolean ImmGetOpenStatus(Pointer himc);
    boolean ImmSetOpenStatus(Pointer himc, boolean open);
}
