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
package dev.bl.feathercaramel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("featherIME");

    public static void log(final String msg, final Object... args)   { LOGGER.info(msg, args); }
    public static void error(final String msg, final Object... args) { LOGGER.error(msg, args); }
    public static void debug(final String msg, final Object... args) { LOGGER.debug(msg, args); }
}
