CC      = gcc
# JDK パスを JAVA_HOME から取得
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin

CFLAGS  = -Wall -fobjc-arc -mmacosx-version-min=10.14 \
          -arch x86_64 -arch arm64 $(JNI_INCLUDES)
FFLAGS  = -framework Foundation -framework Cocoa
OBJS    = cocoainput.o Logger.o DataManager.o MinecraftView.o jni_bridge_darwin.o
LIBS    =
TARGET  = libfeathercaramel_darwin.dylib
DISTDIR = ../../../build/
RM      = rm
CP      = cp

all: $(TARGET)

install: $(TARGET)
	$(CP) -f $(TARGET) $(DISTDIR)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(FFLAGS) $(LIBS) -shared -o $@ $(OBJS)

.m.o:
	$(CC) $(CFLAGS) $(LIBS) -c $<

.c.o:
	$(CC) $(CFLAGS) $(LIBS) -c $<

clean:
	$(RM) -f $(TARGET) $(OBJS)
