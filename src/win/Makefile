CC      = gcc.exe
# JDK のパスを環境変数 JAVA_HOME から取得する
# 例: set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.x.x-hotspot
JNI_INCLUDES = -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32"

CFLAGS  = -Wall -fPIC $(JNI_INCLUDES)
FFLAGS  =
OBJS    = libwincocoainput.o logger.o jni_bridge_win.o
LIBS    =
TARGET  = feathercaramel_win.dll
DISTDIR = ../../build/
RM      = rm
CP      = cp

all: $(TARGET)

install: $(TARGET)
	$(CP) -f $(TARGET) $(DISTDIR)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(FFLAGS) $(LIBS) -shared -o $@ $(OBJS) -limm32

.c.o:
	$(CC) $(CFLAGS) $(LIBS) -c $<

clean:
	$(RM) -f $(TARGET) $(OBJS)
