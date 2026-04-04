package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Predicate;

@Mixin(value = EditBox.class, priority = 0)
public abstract class MixinEditBox implements EditBoxController {

    @Unique private WrapperEditBox featherCaramel$wrapper;
    @Unique private int            featherCaramel$cacheCursor;
    @Unique private int            featherCaramel$cacheHighlight;

    @Shadow private boolean canLoseFocus;
    @Shadow public  int     highlightPos;
    @Shadow public  int     cursorPos;
    @Shadow public  String  value;
    @Shadow public  int     displayPos;
    @Shadow private boolean bordered;
    @Shadow public  net.minecraft.client.gui.Font font;
    // Abstract @Shadow for getX/getY/getHeight removed: in 1.21.10 EditBox does not
    // override these methods from AbstractWidget, so Mixin cannot find the shadow in
    // class_342 and hard-fails with "No refMap loaded".  Call through the concrete
    // EditBox reference instead — JVM dispatch finds the inherited implementation fine.
    @Unique private int featherCaramel$getX()      { return ((EditBox)(Object)this).getX();      }
    @Unique private int featherCaramel$getY()      { return ((EditBox)(Object)this).getY();      }
    @Unique private int featherCaramel$getHeight() { return ((EditBox)(Object)this).getHeight(); }

    @Override
    public WrapperEditBox featherCaramel$wrapper() { return featherCaramel$wrapper; }

    @Redirect(
        method = "<init>(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/client/gui/components/EditBox;Lnet/minecraft/network/chat/Component;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V"),
        require = 0
    )
    private void featherCaramel$initRedirect(final EditBox self, final String value) {
        featherCaramel$wrapper = new WrapperEditBox((EditBox) (Object) this);
        self.setValue(value);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/client/gui/components/EditBox;Lnet/minecraft/network/chat/Component;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void featherCaramel$lazyInit(final CallbackInfo ci) {
        if (featherCaramel$wrapper == null) {
            featherCaramel$wrapper = new WrapperEditBox((EditBox) (Object) this);
        }
    }

    @Inject(method = "setValue", at = @At("HEAD"), require = 0)
    private void featherCaramel$setValueHead(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            featherCaramel$cacheCursor    = cursorPos;
            featherCaramel$cacheHighlight = highlightPos;
        } else {
            featherCaramel$setStatusToNone();
        }
    }

    @Redirect(
        method = "setValue",
        at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"),
        require = 0
    )
    private boolean featherCaramel$setValueTest(final Predicate<String> pred, final Object v) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            return true;
        }
        return pred.test((String) v);
    }

    @Inject(method = "setValue", at = @At("TAIL"), require = 0)
    private void featherCaramel$setValueTail(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && featherCaramel$wrapper.valueChanged) {
            value = text;
            final int sp = featherCaramel$wrapper.getSecondStartPos();
            final int maxPos = value.length();
            if (sp >= 0) {
                cursorPos    = Math.min(sp, maxPos);
                highlightPos = Math.min(sp, maxPos);
            } else {
                cursorPos    = Math.min(featherCaramel$cacheCursor, maxPos);
                highlightPos = Math.min(featherCaramel$cacheHighlight, maxPos);
            }
            featherCaramel$wrapper.valueChanged = false;
        } else {
            featherCaramel$forceUpdateOrigin();
        }
    }

    @Inject(method = "insertText", at = @At("HEAD"), require = 0)
    private void featherCaramel$insertTextHead(final String text, final CallbackInfo ci) {
        featherCaramel$setStatusToNone();
    }

    @Inject(method = "insertText", at = @At("TAIL"), require = 0)
    private void featherCaramel$insertTextTail(final String text, final CallbackInfo ci) {
        featherCaramel$forceUpdateOrigin();
    }

    @Inject(method = "onValueChange", at = @At("HEAD"), require = 0)
    private void featherCaramel$onValueChange(final String text, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null
                && featherCaramel$wrapper.getStatus() == AbstractIMEWrapper.InputStatus.PREVIEW) {
            value = featherCaramel$wrapper.getOrigin();
            final int maxPos = value.length();
            if (cursorPos > maxPos) cursorPos = maxPos;
            if (highlightPos > maxPos) highlightPos = maxPos;
        }
    }

    @Inject(method = "deleteCharsToPos", at = @At("TAIL"), require = 0)
    private void featherCaramel$deleteCharsToPos(final int pos, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setOrigin(value);
        }
    }

    @Inject(method = "setFocused", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocused(final boolean focused, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null) {
            featherCaramel$wrapper.setFocused(focused || !canLoseFocus);
        }
    }

    @Inject(method = "setCanLoseFocus", at = @At("HEAD"), require = 0)
    private void featherCaramel$setCanLoseFocus(final boolean canLoseFocus, final CallbackInfo ci) {
        if (featherCaramel$wrapper != null && !canLoseFocus) {
            featherCaramel$wrapper.setFocused(true);
        }
    }


    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void featherCaramel$renderUnderline(
            final net.minecraft.client.gui.GuiGraphics g,
            final int mx, final int my, final float td, final CallbackInfo ci) {
        if (featherCaramel$wrapper == null) return;
        if (featherCaramel$wrapper.getStatus() != AbstractIMEWrapper.InputStatus.PREVIEW) return;
        final int fep = featherCaramel$wrapper.getFirstEndPos();
        final int ssp = featherCaramel$wrapper.getSecondStartPos();
        if (fep < 0 || ssp <= fep) return;
        final int padX = bordered ? 4 : 0;
        final int padY = bordered ? (featherCaramel$getHeight() - 8) / 2 : 0;
        final int baseX = featherCaramel$getX() + padX;
        final int uY    = featherCaramel$getY() + padY + font.lineHeight;
        final int dp    = displayPos;
        final int c1    = Math.min(Math.max(fep, dp), value.length());
        final int c2    = Math.min(Math.max(ssp, dp), value.length());
        if (c2 <= c1) return;
        final int x1 = baseX + font.width(value.substring(dp, c1));
        final int x2 = baseX + font.width(value.substring(dp, c2));
        g.fill(x1, uY, x2, uY + 1, 0xFFFFFFFF);
    }
    @Unique
    private void featherCaramel$setStatusToNone() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setToNoneStatus();
    }

    @Unique
    private void featherCaramel$forceUpdateOrigin() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setOrigin(value);
    }
}
