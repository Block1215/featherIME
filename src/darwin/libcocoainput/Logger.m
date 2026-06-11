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
//
//  Logger.m
//  libcocoainput
//
//  Created by Axer on 2019/03/23.
//  Copyright © 2019年 Axer. All rights reserved.
//

#import "Logger.h"

struct {
    LogFunction log;
    LogFunction error;
    LogFunction debug;
} LogPointer;

void CILog(NSString* msg) {
    LogPointer.log([msg cStringUsingEncoding:NSUTF8StringEncoding]);
}
void CIError(NSString* msg) {
    LogPointer.error([msg cStringUsingEncoding:NSUTF8StringEncoding]);
}
void CIDebug(NSString* msg) {
    LogPointer.debug([msg cStringUsingEncoding:NSUTF8StringEncoding]);
}

void initLogPointer(LogFunction log,LogFunction error,LogFunction debug) {
    LogPointer.log = log;
    LogPointer.error = error;
    LogPointer.debug = debug;
}
