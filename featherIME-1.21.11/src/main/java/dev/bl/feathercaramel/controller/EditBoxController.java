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
package dev.bl.feathercaramel.controller;

import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.EditBox;

/**
 * MixinEditBox が実装するインターフェース。
 * EditBox インスタンスから WrapperEditBox を取得するためのブリッジ。
 */
public interface EditBoxController {

    static WrapperEditBox getWrapper(final EditBox box) {
        return ((EditBoxController) box).featherCaramel$wrapper();
    }

    WrapperEditBox featherCaramel$wrapper();
}
