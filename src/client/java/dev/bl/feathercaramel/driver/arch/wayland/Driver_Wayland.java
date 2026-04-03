package dev.bl.feathercaramel.driver.arch.wayland;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface Driver_Wayland extends Library {

    void initialize(long wlDisplay,
        PreeditCallback preEdit, PreeditNullCallback preEditNull,
        DoneCallback done, RectCallback rect,
        LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);

    void setFocus(boolean flag);

    interface PreeditCallback     extends Callback { void invoke(WString string); }
    interface PreeditNullCallback extends Callback { void invoke(); }
    interface DoneCallback        extends Callback { void invoke(WString string); }
    interface RectCallback        extends Callback { int  invoke(Pointer pointer); }
    interface LogInfoCallback     extends Callback { void invoke(String log); }
    interface LogErrorCallback    extends Callback { void invoke(String log); }
    interface LogDebugCallback    extends Callback { void invoke(String log); }
}
