package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.driver.KeyboardStatus;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feather 対策:
 *   - @Shadow は fabricloader がリマップできるフィールドのみ使用
 *   - commandSuggestions は refMap なしでは見つからないため削除
 *   - IMEインジケーターの位置はシンプルに左下固定にする
 */
@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    protected MixinChatScreen() { super(Component.empty()); }

    @Shadow protected EditBox input;
    // commandSuggestions の @Shadow は Feather 環境で refMap なしだと失敗するため削除

    @Unique private static final int TOOLTIP_TIME = 500;
    @Unique private static final int FADE_TIME    = 250;
    @Unique private static KeyboardStatus.Language featherCaramel$lastLang;
    @Unique private static long                    featherCaramel$changeTime;

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void featherCaramel$onInit(final CallbackInfo ci) {
        featherCaramel$lastLang = null;
        if (this.input == null) return;
        final WrapperEditBox w = EditBoxController.getWrapper(this.input);
        if (w != null) w.setFocused(true);
    }

    @Inject(method = "removed", at = @At("HEAD"), require = 0)
    private void featherCaramel$onRemoved(final CallbackInfo ci) {
        if (this.input == null) return;
        final WrapperEditBox w = EditBoxController.getWrapper(this.input);
        if (w != null) w.setFocused(false);
    }

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void featherCaramel$render(
            final PoseStack poseStack, final int mx, final int my, final float td, final CallbackInfo ci) {
        featherCaramel$renderImeStatus(poseStack);
    }

    @Unique
    private void featherCaramel$renderImeStatus(final PoseStack poseStack) {
        if (this.input == null) return;
        final WrapperEditBox wrapper = EditBoxController.getWrapper(this.input);
        if (wrapper == null) return;

        final KeyboardStatus status = wrapper.getIme().getController().getKeyboardStatus();
        if (status == null) return;

        final long now = System.currentTimeMillis();
        final var curLang = status.language();
        if (featherCaramel$lastLang != curLang) {
            featherCaramel$lastLang   = curLang;
            featherCaramel$changeTime = now;
        }

        final int elapsed = (int)(now - featherCaramel$changeTime);
        if (elapsed > TOOLTIP_TIME) return;

        // Font は Screen が持つ protected フィールド — Mixin が extends Screen なのでアクセス可能
        final Component display = Component.literal(status.display());
        final int textW = this.font.width(display);
        final int bsx = 2;
        final int bex = bsx + Mth.floor(textW - status.offset()) + 4;
        final int bey = this.height - 14 - 2;
        final int bsy = bey - this.font.lineHeight - 2;

        final int backColor = featherCaramel$fade(
            Minecraft.getInstance().options.getBackgroundColor(Integer.MIN_VALUE), elapsed);
        final int textColor = featherCaramel$fade(0xFFFFFFFF, elapsed);
        if (backColor == 0 && textColor == 0) return;

        // 1.19.4: fill と drawString は Screen のメソッドで PoseStack を使用
        fill(poseStack, bsx, bsy, bex, bey, backColor);
        drawString(poseStack, this.font, display, bsx + 2, bsy + 1, textColor);
    }

    @Unique
    private int featherCaramel$fade(final int color, final int elapsed) {
        if (elapsed < FADE_TIME) return color;
        final int   a = (color >> 24) & 0xFF;
        final float p = (float)(TOOLTIP_TIME - elapsed) / FADE_TIME;
        final int   r = (int)(p * a);
        return r < 5 ? 0 : (r << 24) | (color & 0x00FFFFFF);
    }
}
