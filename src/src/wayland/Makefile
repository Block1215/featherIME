CC      = gcc
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

CFLAGS  = -Wall -fPIC $(JNI_INCLUDES)
FFLAGS  =
OBJS    = libcaramelchatwl.o logger.o jni_bridge_wl.o \
          text-input-unstable-v3-protocol.o
LIBS    = -lwayland-client
TARGET  = libfeathercaramel_wl.so
DISTDIR = ../../build/
RM      = rm
CP      = cp

# Dependencies
WAYLAND_PROTOCOLS_DIR = $(shell pkg-config wayland-protocols --variable=pkgdatadir)
WAYLAND_SCANNER       = $(shell pkg-config --variable=wayland_scanner wayland-scanner)
TEXT_INPUT_V3_PROTOCOL = $(WAYLAND_PROTOCOLS_DIR)/unstable/text-input/text-input-unstable-v3.xml

HEADERS = text-input-unstable-v3-client-protocol.h \
          text-input-unstable-v3-protocol.c

all: $(TARGET)

install: $(TARGET)
	$(CP) -f $(TARGET) $(DISTDIR)

$(TARGET): $(HEADERS) $(OBJS)
	$(CC) $(OBJS) $(CFLAGS) $(FFLAGS) $(LIBS) -shared -o $@

.c.o:
	$(CC) $(CFLAGS) $(LIBS) -c $<

clean:
	$(RM) -f *.o $(HEADERS) $(TARGET) $(OBJS)

text-input-unstable-v3-client-protocol.h:
	$(WAYLAND_SCANNER) client-header $(TEXT_INPUT_V3_PROTOCOL) $@

text-input-unstable-v3-protocol.c:
	$(WAYLAND_SCANNER) public-code $(TEXT_INPUT_V3_PROTOCOL) $@
