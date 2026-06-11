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
package dev.bl.feathercaramel;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.arch.unknown.UnknownController;
import dev.bl.feathercaramel.util.ModLogger;
import net.fabricmc.api.ClientModInitializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public final class FeatherCaramelChatClient implements ClientModInitializer {

    private static IController controller = null;

    @Override
    public void onInitializeClient() {
        try {
            controller = IController.getController();
        } catch (Exception e) {
            ModLogger.error("[FeatherIME] Failed to initialize IME controller: {}", e.getMessage());
            controller = UnknownController.INSTANCE;
        }
        ModLogger.log("[FeatherIME] Initialized. controller={}", controller.getClass().getSimpleName());
    }

    public static IController getController() {
        return controller != null ? controller : UnknownController.INSTANCE;
    }

    /**
     * jar 内の /native/{name} を一時ファイルに展開してパスを返す。
     */
    public static String copyLibrary(final String name) {
        try {
            final URL url = FeatherCaramelChatClient.class.getClassLoader()
                .getResource("native/" + name);
            if (url == null) {
                throw new RuntimeException("[FeatherIME] Native library not found in jar: native/" + name);
            }
            final String suffix = name.endsWith(".dll") ? ".dll"
                                : name.endsWith(".dylib") ? ".dylib" : ".so";
            final File lib = File.createTempFile("featherIME_", suffix);
            lib.deleteOnExit();
            try (final InputStream is      = url.openStream();
                 final FileOutputStream fos = new FileOutputStream(lib)) {
                fos.write(is.readAllBytes());
            }
            ModLogger.log("[FeatherIME] Loaded native library: {}", name);
            return lib.getAbsolutePath();
        } catch (Exception e) {
            ModLogger.error("[FeatherIME] Failed to extract native library '{}': {}", name, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
