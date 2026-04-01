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
    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        // GLFW初期化前なので、ここでは初期化せず、最初のsetScreenで初期化
        ModLogger.log("[FeatherIME] Client initialized. Waiting for GLFW...");
    }

    public static synchronized IController getController() {
        if (!initialized) {
            try {
                controller = IController.getController();
                initialized = true;
                ModLogger.log("[FeatherIME] Controller initialized: {}", controller.getClass().getSimpleName());
            } catch (Exception e) {
                ModLogger.error("[FeatherIME] Failed to initialize IME controller: {}", e.getMessage());
                controller = UnknownController.INSTANCE;
                initialized = true;
            }
        }
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
