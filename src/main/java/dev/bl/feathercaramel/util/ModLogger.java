package dev.bl.feathercaramel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("featherIME");

    public static void log(final String msg, final Object... args)   { LOGGER.info(msg, args); }
    public static void error(final String msg, final Object... args) { LOGGER.error(msg, args); }
    public static void debug(final String msg, final Object... args) { LOGGER.debug(msg, args); }
}
