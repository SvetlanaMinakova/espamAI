/* $Id: debug.hpp,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <stdarg.h>
#include <stdio.h>
#include <windows.h>

#include <hdpc/stdafx.h>

namespace hdpc {
	namespace debug {

		void debug(const char *msg, ...) {
			if (!HDPC_DEBUG_MODE) return;

			va_list arg;
			va_start(arg, msg);

			char buf[1024];
			vsnprintf_s(buf, lengthof(buf), sizeof(buf), msg, arg);
			fprintf(stdout, "%s\n", buf);
			fflush(stdout);

			va_end(arg);
		}

		void error(const char *msg, ...) {
			if (!HDPC_DEBUG_MODE) return;
			va_list arg;
			va_start(arg, msg);

			char buf[1024];
			vsnprintf_s(buf, lengthof(buf), sizeof(buf), msg, arg);
			fprintf(stderr, "%s\n", buf);
			fflush(stderr);
			assert(false);
			MessageBoxA(NULL, buf, "HDPC error", MB_OK|MB_ICONERROR|MB_TOPMOST);

			va_end(arg);
		}

	} /*namespace debug */
} /* namespace hdpc */
