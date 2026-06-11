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
            return switch (GLFW.glfwGetPlatform()) {
                case GLFW.GLFW_PLATFORM_WIN32   -> new WinController();
                case GLFW.GLFW_PLATFORM_COCOA   -> new DarwinController();
                case GLFW.GLFW_PLATFORM_X11     -> new X11Controller();
                case GLFW.GLFW_PLATFORM_WAYLAND -> new WaylandController();
                default -> throw new UnsupportedOperationException();
            };
        } catch (UnsupportedOperationException ignored) {
            ModLogger.error("This platform is not supported.");
        } catch (Exception e) {
            ModLogger.error("Error while loading IME driver: {}", e.getMessage());
        }
        return UnknownController.INSTANCE;
    }
}
