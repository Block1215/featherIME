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
package dev.bl.feathercaramel.driver.arch.x11;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface Driver_X11 extends Library {

    void initialize(long windowId, long xWindowId,
        DrawCallback draw, DoneCallback done,
        LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);

    void set_focus(int flag);

    interface DrawCallback extends Callback {
        Pointer invoke(int caret, int chg_first, int chg_length, short length,
            boolean iswstring, String rawstring, WString rawwstring,
            int primary, int secondary, int tertiary);
    }
    interface DoneCallback     extends Callback { void invoke(); }
    interface LogInfoCallback  extends Callback { void invoke(String log); }
    interface LogErrorCallback extends Callback { void invoke(String log); }
    interface LogDebugCallback extends Callback { void invoke(String log); }
}
