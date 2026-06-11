/*
 * featherIME native source - derived from CocoaInput-lib (CocoaInput Native).
 *
 * Original work: CocoaInput-lib by Korea-Minecraft-Forum
 *                <https://github.com/Korea-Minecraft-Forum/CocoaInput-lib>
 *                (original CocoaInput by Axeryok; Wayland part "caramelChat
 *                Library" by LemonCaramel)
 * License: Minecraft Mod Public License Japanese Translation (MMPL_J) 1.0.1
 *          (see LICENSES/MMPL_J-1.0.1.txt in the repository root)
 *
 * Modifications Copyright (C) 2024-2026 @Bl
 * NOTE: The native binaries bundled in the released jar are unmodified copies
 * of those distributed with caramelChat (built from CocoaInput-lib) and were
 * NOT built from this tree. See CREDITS.md.
 */
#define _GNU_SOURCE

#include "logger.h"
#include <stdio.h>
#include <stdlib.h>

struct {
    LogFunction log;
    LogFunction error;
    LogFunction debug;
} LogPointer;

void CILog(const char *format, ...) {
    char *msg;
    va_list args;
    va_start(args, format);
    vasprintf(&msg, format, args);
    LogPointer.log(msg);
    free(msg);
    va_end(args);
}

void CIError(const char *format, ...) {
    char *msg;
    va_list args;
    va_start(args, format);
    vasprintf(&msg, format, args);
    LogPointer.error(msg);
    free(msg);
    va_end(args);
}

void CIDebug(const char *format, ...) {
    char *msg;
    va_list args;
    va_start(args, format);
    vasprintf(&msg, format, args);
    LogPointer.debug(msg);
    free(msg);
    va_end(args);
}

void initLogPointer(LogFunction log, LogFunction error, LogFunction debug) {
    LogPointer.log = log;
    LogPointer.error = error;
    LogPointer.debug = debug;
}
