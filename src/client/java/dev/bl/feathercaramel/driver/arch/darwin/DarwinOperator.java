package dev.bl.feathercaramel.driver.arch.darwin;

import dev.bl.feathercaramel.driver.IController;
import dev.bl.feathercaramel.driver.IOperator;
import dev.bl.feathercaramel.util.ModLogger;
import dev.bl.feathercaramel.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import java.util.UUID;

public final class DarwinOperator implements IOperator {

    private final DarwinController  controller;
    private final AbstractIMEWrapper wrapper;
    private final String            uuid;
    private boolean                 nowFocused;

    public DarwinOperator(DarwinController controller, AbstractIMEWrapper wrapper) {
        this.controller = controller;
        this.wrapper    = wrapper;
        this.uuid       = UUID.randomUUID().toString();

        ModLogger.debug("[Darwin] addInstance: {}", uuid);
        controller.getDriver().addInstance(
            uuid,
            // insertText
            (str, pos, len) -> {
                ModLogger.debug("[Darwin] insertText ({}): {}", uuid, str);
                wrapper.insertText(str);
            },
            // setMarkedText
            (str, p1, l1, p2, l2) -> {
                ModLogger.debug("[Darwin] setMarkedText ({}): {}", uuid, str);
                wrapper.appendPreviewText(str);
            },
            // firstRectForCharacterRange
            (pointer) -> {
                final float[] buff  = wrapper.getRect().copy();
                final var     win   = Minecraft.getInstance().getWindow();
                final float   scale = (float) win.getGuiScale();
                buff[0] = buff[0] * scale + win.getX();
                buff[1] = buff[1] * scale + win.getY();
                buff[2] *= scale;
                buff[3] *= scale;
                pointer.write(0, buff, 0, 4);
            }
        );
    }

    @Override public IController getController() { return controller; }

    @Override
    public void setFocused(boolean focus) {
        if (focus == nowFocused) return;
        ModLogger.debug("[Darwin] setFocused ({}): {}", uuid, focus);
        controller.getDriver().setIfReceiveEvent(uuid, focus ? 1 : 0);
        nowFocused = focus;
    }

    @Override public boolean isFocused() { return nowFocused; }
}
