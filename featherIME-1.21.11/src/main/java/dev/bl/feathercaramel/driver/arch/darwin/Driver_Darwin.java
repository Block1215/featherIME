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
package dev.bl.feathercaramel.driver.arch.darwin;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface Driver_Darwin extends Library {

    void initialize(LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);
    void addInstance(String uuid, InsertText insertText, SetMarkedText setMarkedText, FirstRectForCharacterRange range);
    void removeInstance(String uuid);
    void refreshInstance();
    void discardMarkedText(String uuid);
    void setIfReceiveEvent(String uuid, int yn);
    float  invertYCoordinate(float y);
    String getStatus();

    interface InsertText                extends Callback { void invoke(String str, int position, int length); }
    interface SetMarkedText             extends Callback { void invoke(String str, int p1, int l1, int p2, int l2); }
    interface FirstRectForCharacterRange extends Callback { void invoke(Pointer pointer); }
    interface LogInfoCallback           extends Callback { void invoke(String log); }
    interface LogErrorCallback          extends Callback { void invoke(String log); }
    interface LogDebugCallback          extends Callback { void invoke(String log); }
}
