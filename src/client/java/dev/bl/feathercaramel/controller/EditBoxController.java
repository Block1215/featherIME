package dev.bl.feathercaramel.controller;

import dev.bl.feathercaramel.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.EditBox;

/**
 * MixinEditBox が実装するインターフェース。
 * EditBox インスタンスから WrapperEditBox を取得するためのブリッジ。
 */
public interface EditBoxController {

    static WrapperEditBox getWrapper(final EditBox box) {
        return ((EditBoxController) box).featherCaramel$wrapper();
    }

    WrapperEditBox featherCaramel$wrapper();
}
