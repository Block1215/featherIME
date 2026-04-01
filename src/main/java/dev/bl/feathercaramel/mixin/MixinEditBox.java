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
            cursorPos    = featherCaramel$cacheCursor;
            highlightPos = featherCaramel$cacheHighlight;
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

    @Unique
    private void featherCaramel$setStatusToNone() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setToNoneStatus();
    }

    @Unique
    private void featherCaramel$forceUpdateOrigin() {
        if (featherCaramel$wrapper != null) featherCaramel$wrapper.setOrigin(value);
    }
}
