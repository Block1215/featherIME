package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AbstractWidget フック。
 *
 * 1.19.3 では EditBox が render()/setFocused() をオーバーライドしていないため、
 * このクラスで処理する。
 * x, y, height は AbstractWidget のフィールドなのでここで @Shadow する。
 */
@Mixin(value = AbstractWidget.class, priority = 0)
public abstract class MixinAbstractWidget {

    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int height;

    @Inject(method = "setFocused", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocused(final boolean focused, final CallbackInfo ci) {
        if (focused && this instanceof EditBoxController ctrl) {
            final WrapperEditBox wrapper = ctrl.featherCaramel$wrapper();
            if (wrapper != null) {
                wrapper.setFocused(true);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void featherCaramel$renderUnderline(
            final com.mojang.blaze3d.vertex.PoseStack poseStack,
            final int mx, final int my, final float td, final CallbackInfo ci) {
        if (!(this instanceof EditBoxController ctrl)) return;
        final WrapperEditBox wrapper = ctrl.featherCaramel$wrapper();
        if (wrapper == null) return;
        if (wrapper.getStatus() != AbstractIMEWrapper.InputStatus.PREVIEW) return;
        final int fep = wrapper.getFirstEndPos();
        final int ssp = wrapper.getSecondStartPos();
        if (fep < 0 || ssp <= fep) return;
        final EditBox box = (EditBox)(Object)this;
        final int padX = box.bordered ? 4 : 0;
        final int padY = box.bordered ? (height - 8) / 2 : 0;
        final int baseX = x + padX;
        final int uY    = y + padY + box.font.lineHeight;
        final int dp    = box.displayPos;
        final String val = box.value;
        final int c1    = Math.min(Math.max(fep, dp), val.length());
        final int c2    = Math.min(Math.max(ssp, dp), val.length());
        if (c2 <= c1) return;
        final int x1 = baseX + box.font.width(val.substring(dp, c1));
        final int x2 = baseX + box.font.width(val.substring(dp, c2));
        net.minecraft.client.gui.GuiComponent.fill(poseStack, x1, uY, x2, uY + 1, 0xFFFFFFFF);
    }
}
