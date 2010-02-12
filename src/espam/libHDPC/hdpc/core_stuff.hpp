/* $Id: core_stuff.hpp,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#if !defined(_WIN32)
#include <sched.h>
#endif /* _WIN32 */

#include <hdpc/stdafx.h>

namespace hdpc {

	#pragma warning(disable: 4293) // C4293: '<<' : shift count negative or too big, undefined behavior

	#if defined(_WIN32)
	typedef DWORD_PTR cpu_set_t;
	#endif /* _WIN32 */

	template <int core = 0> class CPU_CORES {
	public:
	#if defined(_WIN32)
		CPU_CORES(): mask(0) {
			if (core > 0) mask |= (1 << (core - 1));
		}
	#else
		CPU_CORES() {
			CPU_ZERO(&mask);
			if (core > 0) CPU_SET(core - 1, &mask);
		}
	#endif /* _WIN32 */

		operator cpu_set_t() {return mask;}
	private:
		cpu_set_t mask;
	};

	static void setThreadAffinity(boost::thread::native_handle_type h, cpu_set_t mask) {
	#if defined(_WIN32)
		if (mask != CPU_CORES<>()) SetThreadAffinityMask(h, mask);
	#else
		sched_setaffinity(h, sizeof(mask), &mask);
	#endif /* _WIN32 */
	}

#pragma warning(default: 4293)
} /* namespace hdpc */
