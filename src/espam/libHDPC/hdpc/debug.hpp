/* $Id: debug.hpp,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <stdarg.h>
#include <stdio.h>
#if defined(_WIN32)
#include <windows.h>
#endif /* _WIN32 */

#include <hdpc/stdafx.h>

namespace hdpc {
	namespace debug {

		void debug(const char *msg, ...) {
			if (!HDPC_DEBUG_MODE) return;

			va_list arg;
			va_start(arg, msg);

			fprintf(stdout, msg, arg);
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
		#if defined(_WIN32)
			MessageBoxA(NULL, buf, "HDPC error", MB_OK|MB_ICONERROR|MB_TOPMOST);
		#endif /* _WIN32 */

			va_end(arg);
		}

	} /*namespace debug */
} /* namespace hdpc */
