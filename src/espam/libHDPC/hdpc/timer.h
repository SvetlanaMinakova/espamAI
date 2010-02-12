/* $Id: timer.h,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>

namespace hdpc {

	template <bool enabled = true> class Timer;
	template <int timer_count, bool enabled = true> class ComboTimer;
	typedef Timer<HDPC_DEBUG_MODE == 1> timer_t;

	template <> class Timer<false> {
	public:
		inline void start();
		inline void stop();
		inline double busy() const;

		inline void start_timer();
		inline void end_timer();
		inline double elapsed_time();
		inline double stop_timer();

		static inline void start_timer(uint64_t& time);
		static inline void end_timer(uint64_t& time);
		static inline double elapsed_time(const uint64_t &start, const uint64_t &end);

		static inline uint64_t get_overhead();
	};

	template <int timer_count> class ComboTimer<timer_count, false> {
	public:
		inline void start();
		inline void stop(int index);
		inline double elapsed(int index) const;
		inline size_t count() const;
	};

	template <> class Timer<true> {
	public:
		Timer();
		inline void start();
		inline void stop();
		inline double busy() const;

		inline void start_timer();
		inline void end_timer();
		inline double elapsed_time();
		double stop_timer();

		static void start_timer(uint64_t& time);
		static void end_timer(uint64_t& time);
		static double elapsed_time(const uint64_t &start, const uint64_t &end);

		static uint64_t get_overhead();
	private:
		uint64_t _start, _end;
		double _busy;

		static uint64_t overhead;
		static uint64_t frequency;
	};

	template <int timer_count> class ComboTimer<timer_count, true> {
	public:
		ComboTimer();
		inline void start();
		inline void stop(int index);
		inline double elapsed(int index) const;
		inline size_t count() const;

	private:
		Timer<true> t;
		size_t _count;
		uint64_t _start;
		boost::array<double, timer_count> _end;
	};

} /* namespace hdpc */
