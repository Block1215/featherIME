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

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;

public final class X11Operator implements IOperator {

    private final X11Controller      controller;
    private final AbstractIMEWrapper wrapper;
    private boolean                  nowFocused;

    public X11Operator(X11Controller c, AbstractIMEWrapper w) { controller = c; wrapper = w; }
    public AbstractIMEWrapper getWrapper() { return wrapper; }
    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[X11] setFocused: {}", focus);
        nowFocused = focus;
        if (focus) {
            X11Controller.focused = this;
            controller.setFocus(true);
        } else if (X11Controller.focused == this) {
            wrapper.insertText("");
            X11Controller.focused = null;
            controller.setFocus(false);
            X11Controller.setupKeyboardEvent();
        }
    }

    @Override public boolean isFocused() { return nowFocused; }
}
