package dev.bl.feathercaramel.driver.arch.x11;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface Driver_X11 extends Library {

    void initialize(long windowId, long xWindowId,
        DrawCallback draw, DoneCallback done,
        LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);

    void set_focus(int flag);

    interface DrawCallback extends Callback {
        Pointer invoke(int caret, int chg_first, int chg_length, short length,
            boolean iswstring, String rawstring, WString rawwstring,
            int primary, int secondary, int tertiary);
    }
    interface DoneCallback     extends Callback { void invoke(); }
    interface LogInfoCallback  extends Callback { void invoke(String log); }
    interface LogErrorCallback extends Callback { void invoke(String log); }
    interface LogDebugCallback extends Callback { void invoke(String log); }
}
