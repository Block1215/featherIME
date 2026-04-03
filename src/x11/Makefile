CC      = gcc
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

CFLAGS  = -Wall -fPIC $(JNI_INCLUDES)
FFLAGS  = -lX11
OBJS    = libx11cocoainput.o logger.o jni_bridge_x11.o
LIBS    =
TARGET  = libfeathercaramel_x11.so
DISTDIR = ../../build/
RM      = rm
CP      = cp

all: $(TARGET)

install: $(TARGET)
	$(CP) -f $(TARGET) $(DISTDIR)

$(TARGET): $(OBJS)
	$(CC) $(OBJS) $(CFLAGS) $(FFLAGS) $(LIBS) -shared -o $@

.c.o:
	$(CC) $(CFLAGS) $(LIBS) -c $<

clean:
	$(RM) -f $(TARGET) $(OBJS)
