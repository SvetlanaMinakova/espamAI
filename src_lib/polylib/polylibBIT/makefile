#
# $Id: makefile,v 1.2 2009/11/25 11:14:03 mohamed Exp $
#

ROOT  = ../..

# Java files in this directory
JSRCS = \
	PolyLibBIT.java \
	PolylibBITImplementation.java

SRCS = \
	polylibBIT.c

OBJS = $(SRCS:.c=.o)

LIBNAME = $(ROOT)/lib/libpandapolylib.$(SHEXT)

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

# =========================== C++ Code =====================

INCL                    = -I$(JAVA_HOME)/include/$(OSEXT)/ \
                          -I$(JAVA_HOME)/include \
	                  -I$(POLYLIB_INC)

# Changed to enable compilation on 64-bit machines
#LIB                     = $(POLYLIB_LIB)/libpolylib$(BITS).a
LIB                     = -L$(POLYLIB_LIB) -lpolylib$(BITS)


# Added to enable compilation on 64-bit machines
EXTRA_FLAGS = -fPIC


# =========================== WINDOWS NT =====================
#
# DLL related variables. These are used when building the DLL. See later.
#

AS = as
DLLTOOL = dlltool
DLLWRAP = dllwrap

#
# Various targets to build.
#
DLL_NAME    = pandapolylib.dll
DLL_EXP_DEF = pandapolylib.def

# Some tools require special CPP macros when building a DLL (eg., _DLL etc).
# Here we don't need anything.

DLL_CFLAGS = -DBUILDING_DLL=1 -D_DLL=1 

# The default entry point defined by dllwrap is __cygwin_dll_entry@12 
# defined in libcygwin.a, but that's only appropriate for Cygwin apps,
# but since Java is a MSVC app, we need to provide a different entry
# point. Note the leading underscore and the trailing @12.
# The -s flag strips the DLL to shrink the size.

DLL_LDFLAGS = -Wl,-e,__cygwin_noncygwin_dll_entry@12 -s

# any extra libraries that your DLL may depend on.
DLL_OBJS =  $(OBJS)

DLLWRAP_FLAGS = --output-def $(DLL_EXP_DEF) \
		--add-stdcall-alias \
		--driver-name $(CC) \
		$(IMAGE_BASE)

# ==========================================================

package : classes espam.utils_polylib_polylibBIT_PolyLibBIT.h $(OBJS) $(LIBTYPE)
all : classes espam.utils_polylib_polylibBIT_PolyLibBIT.h $(OBJS) $(LIBTYPE)

$(OBJS): $(POLYLIB_INC)/polylib/polylib.h

espam.utils_polylib_polylibBIT_PolyLibBIT.h: PolyLibBIT.class
	$(JAVAH) -jni -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(ESPAM_SRC)" espam.utils.polylib.polylibBIT.PolyLibBIT
	touch $@

library: $(OBJS)
	@if [ ! -d $(ROOT)/lib ]; then mkdir -p $(ROOT)/lib; fi;
	echo " -- Rebuilding Library $(LIBNAME) -- " ;\
	$(LD) $(LDFLAGS) $(SHAREDLIB_FLAG) \
	$(OBJS) -o $(LIBNAME) $(LIB)

dll:
	@if [ ! -d $(ROOT)/lib ]; then mkdir -p $(ROOT)/lib; fi;
	@if [ ! -f $(ROOT)/lib/$(DLL_NAME) ]; then \
	echo " -- Rebuilding Library $(DLL_NAME) -- " ;\
	$(DLLWRAP) $(DLLWRAP_FLAGS) -o $(ROOT)/lib/$(DLL_NAME) \
		    $(DLL_OBJS) $(LIB) $(DLL_LDFLAGS) $(DLL_LDLIBS) ;\
	fi;

.c.o: $(SRCS)
	$(CC) $(CFLAGS) $(DEFINES) $(EXTRA_FLAGS) $(INCL) -c $<
