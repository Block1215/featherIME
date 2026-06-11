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
//  cocoainput.h
//  libcocoainput
//
//  Created by Axer on 2019/03/23.
//  Copyright © 2019年 Axer. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Logger.h"


void initialize(LogFunction log,LogFunction error,LogFunction debug);
void replaceInstanceMethod(Class cls, SEL sel, SEL renamedSel, Class dataCls);

void addInstance(
    const char* uuid,
    void (*insertText_p)(const char*, const int, const int),
    void (*setMarkedText_p)(const char*, const int, const int, const int, const int),
    void (*firstRectForCharacterRange)(const float*)
);
void removeInstance(const char* uuid);
void refreshInstance(void);

void discardMarkedText(const char* uuid);
void setIfReceiveEvent(const char* uuid, int yn);

const char* getStatus(void);
