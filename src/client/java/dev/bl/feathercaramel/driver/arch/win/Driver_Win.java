package dev.bl.feathercaramel.driver.arch.win;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import dev.bl.feathercaramel.driver.KeyboardStatus.Language;
import java.util.Map;

public interface Driver_Win extends Library {

    int LAYOUT_CHINESE_TRADITIONAL = 0x0404;
    int LAYOUT_JAPANESE            = 0x0411;
    int LAYOUT_KOREAN              = 0x0412;
    int LAYOUT_CHINESE_SIMPLIFIED  = 0x0804;

    Map<Integer, Language> LAYOUT_MAP = Map.of(
        LAYOUT_KOREAN,              Language.KOREAN,
        LAYOUT_JAPANESE,            Language.JAPANESE,
        LAYOUT_CHINESE_SIMPLIFIED,  Language.CHINESE_SIMPLIFIED,
        LAYOUT_CHINESE_TRADITIONAL, Language.CHINESE_TRADITIONAL
    );

    void initialize(long windowId,
        PreeditCallback preEdit, DoneCallback done, RectCallback rect,
        LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);

    void set_focus(int flag);
    int  getKeyboardLayout();
    int  getStatus();

    interface PreeditCallback  extends Callback { void invoke(WString string, int cursor, int length); }
    interface DoneCallback     extends Callback { void invoke(WString string); }
    interface RectCallback     extends Callback { int  invoke(Pointer pointer); }
    interface LogInfoCallback  extends Callback { void invoke(String log); }
    interface LogErrorCallback extends Callback { void invoke(String log); }
    interface LogDebugCallback extends Callback { void invoke(String log); }
}
