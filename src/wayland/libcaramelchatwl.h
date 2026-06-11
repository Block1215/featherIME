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
#include <wayland-client-core.h>
#include <stdio.h>
#include <stdbool.h>

#include "text-input-unstable-v3-client-protocol.h"
#include "logger.h"

void initialize(
    struct wl_display *display,
    void (*callPreedit)(wchar_t*),
    void (*callPreeditNull),
    void (*callDone)(wchar_t*),
    bool (*callRect)(float*),
    LogFunction log,
    LogFunction error,
    LogFunction debug
);

void setFocus(bool flag);
