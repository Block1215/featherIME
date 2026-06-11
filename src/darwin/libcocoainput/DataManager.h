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
//  DataManager.h
//  libcocoainput
//
//  Created by Axer on 2019/03/23.
//  Copyright © 2019年 Axer. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>

#import "MinecraftView.h"

@interface DataManager : NSObject <NSTextInputClient>

@property BOOL hasPreeditText;
@property BOOL isSentedInsertText;
@property BOOL isBeforeActionSetMarkedText;
//@property void (*insertText)(const char*, const int, const int);
//@property void (*setMarkedText)(const char*, const int, const int, const int, const int);
//@property float* (*firstRectForCharacterRange)(void);




@property NSMutableDictionary* dic;
@property MinecraftView* activeView;
@property NSView* glfwView;
+ (instancetype)sharedManager;
- (void) modifyGLFWView;

//機能上書き
- (void)keyDown:(NSEvent*)theEvent;
- (void)interpretKeyEvents:(NSArray*)eventArray;

//形式上
- (void)org_keyDown:(NSEvent*)theEvent;
- (void)org_unmarkText;
- (void)org_interpretKeyEvents:(NSArray*)eventArray;
- (NSRect)org_firstRectForCharacterRange:(NSRange)aRange
                         actualRange:(NSRangePointer)actualRange;
- (void)org_insertText:(id)aString replacementRange:(NSRange)replacementRange;
- (void)org_setMarkedText:(id)aString
        selectedRange:(NSRange)selectedRange
     replacementRange:(NSRange)replacementRange ;
@end
