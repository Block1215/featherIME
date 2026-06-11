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

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import java.util.UUID;

public final class DarwinOperator implements IOperator {

    private final DarwinController  controller;
    private final AbstractIMEWrapper wrapper;
    private final String            uuid;
    private boolean                 nowFocused;

    public DarwinOperator(DarwinController controller, AbstractIMEWrapper wrapper) {
        this.controller = controller;
        this.wrapper    = wrapper;
        this.uuid       = UUID.randomUUID().toString();

        ModLogger.debug("[Darwin] addInstance: {}", uuid);
        controller.getDriver().addInstance(
            uuid,
            // insertText
            (str, pos, len) -> {
                ModLogger.debug("[Darwin] insertText ({}): {}", uuid, str);
                wrapper.insertText(str);
            },
            // setMarkedText
            (str, p1, l1, p2, l2) -> {
                ModLogger.debug("[Darwin] setMarkedText ({}): {}", uuid, str);
                wrapper.appendPreviewText(str);
            },
            // firstRectForCharacterRange
            (pointer) -> {
                final float[] buff  = wrapper.getRect().copy();
                final var     win   = Minecraft.getInstance().getWindow();
                final float   scale = (float) win.getGuiScale();
                buff[0] = buff[0] * scale + win.getX();
                buff[1] = buff[1] * scale + win.getY();
                buff[2] *= scale;
                buff[3] *= scale;
                pointer.write(0, buff, 0, 4);
            }
        );
    }

    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[Darwin] setFocused ({}): {}", uuid, focus);
        controller.getDriver().setIfReceiveEvent(uuid, focus ? 1 : 0);
        nowFocused = focus;
    }

    @Override public boolean isFocused() { return nowFocused; }
}
