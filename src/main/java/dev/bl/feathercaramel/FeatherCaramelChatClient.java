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
