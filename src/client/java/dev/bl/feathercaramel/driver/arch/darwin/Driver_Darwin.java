package dev.bl.feathercaramel.driver.arch.darwin;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface Driver_Darwin extends Library {

    void initialize(LogInfoCallback log, LogErrorCallback error, LogDebugCallback debug);
    void addInstance(String uuid, InsertText insertText, SetMarkedText setMarkedText, FirstRectForCharacterRange range);
    void removeInstance(String uuid);
    void refreshInstance();
    void discardMarkedText(String uuid);
    void setIfReceiveEvent(String uuid, int yn);
    float  invertYCoordinate(float y);
    String getStatus();

    interface InsertText                extends Callback { void invoke(String str, int position, int length); }
    interface SetMarkedText             extends Callback { void invoke(String str, int p1, int l1, int p2, int l2); }
    interface FirstRectForCharacterRange extends Callback { void invoke(Pointer pointer); }
    interface LogInfoCallback           extends Callback { void invoke(String log); }
    interface LogErrorCallback          extends Callback { void invoke(String log); }
    interface LogDebugCallback          extends Callback { void invoke(String log); }
}
