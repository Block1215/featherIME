package dev.bl.feathercaramel.mixin;

import dev.bl.feathercaramel.controller.EditBoxController;
import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AbstractWidget.setFocused(boolean) フック。
 *
 * 1.19.3 では EditBox が setFocused(boolean) をオーバーライドしていないため、
 * @Mixin(EditBox.class) での @Inject(method="setFocused") は
 * EditBox のバイトコードに存在しないメソッドへのインジェクションとなり無視される。
 *
 * コマンドブロック・ModMenu 検索欄など、クリックでフォーカスが移る EditBox では
 * Screen.mouseClicked → AbstractWidget.setFocused(true) の経路で呼ばれるため、
 * AbstractWidget レベルでフックしてから EditBoxController か否かを判定する。
 */
@Mixin(value = AbstractWidget.class, priority = 0)
public abstract class MixinAbstractWidget {

    @Inject(method = "setFocused", at = @At("TAIL"), require = 0)
    private void featherCaramel$setFocused(final boolean focused, final CallbackInfo ci) {
        if (focused && this instanceof EditBoxController ctrl) {
            final WrapperEditBox wrapper = ctrl.featherCaramel$wrapper();
            if (wrapper != null) {
                wrapper.setFocused(true);
            }
        }
    }
}
