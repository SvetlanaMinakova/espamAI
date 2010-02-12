/* $Id: timer.hpp,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/timer.h>
# if defined(WIN32)
# include <windows.h>
#else
# include <stdio.h>
# include <sys/time.h>
#endif /* WIN32 */

namespace hdpc {

	void Timer<false>::start() {

	}

	void Timer<false>::stop() {

	}

	double Timer<false>::busy() const {
		return 0;
	}

	void Timer<false>::start_timer() {

	}

	void Timer<false>::end_timer() {

	}

	double Timer<false>::elapsed_time() {
		return 0;
	}

	double Timer<false>::stop_timer() {
		return 0;
	}

	void Timer<false>::start_timer(uint64_t& time) {

	}

	void Timer<false>::end_timer(uint64_t& time) {

	}

	double Timer<false>::elapsed_time(const uint64_t &start, const uint64_t &end) {
		return 0;
	}

	uint64_t Timer<false>::get_overhead() {
		return 0;
	}

	Timer<true>::Timer(): _busy(0), _start(0), _end(0) {

	}

	void Timer<true>::start() {
		start_timer(_start);
	}

	void Timer<true>::stop() {
		_busy += stop_timer();
	}

	double Timer<true>::busy() const {
		return _busy;
	}

	void Timer<true>::start_timer() {
		start_timer(_start);
	}

	void Timer<true>::end_timer() {
		end_timer(_end);
	}

	double Timer<true>::elapsed_time() {
		return elapsed_time(_start, _end);
	}

	double Timer<true>::stop_timer() {
		end_timer();
		return elapsed_time(_start, _end);
	}

	uint64_t Timer<true>::get_overhead() {
		/* first run, initialise value */
		if (overhead == 0) {
			uint64_t t;
			start_timer(t);
		}
		return overhead;
	}

#if defined(_WIN32)
	void Timer<true>::start_timer(uint64_t &time) {
		static bool first_run = true;
		LARGE_INTEGER ts;
		/* get the overhead of the call itself */
		if (first_run) {
			LARGE_INTEGER te, freq;
			QueryPerformanceCounter(&ts);
			QueryPerformanceCounter(&te);
			overhead = te.QuadPart - ts.QuadPart;
			QueryPerformanceFrequency(&freq);
			frequency = freq.QuadPart;
			first_run = false;
		}
		QueryPerformanceCounter(&ts);
		time = ts.QuadPart;
	}

	void Timer<true>::end_timer(uint64_t &time) {
		LARGE_INTEGER t;
		QueryPerformanceCounter(&t);
		time = t.QuadPart;
	}

	double Timer<true>::elapsed_time(const uint64_t &start, const uint64_t &end) {
		uint64_t diff = end - start;
		if (diff < overhead) return 0;
		return (diff - overhead) / (double)frequency;
	}

#else /* _WIN32 */
	void Timer<true>::start_timer(uint64_t &time) {
		static bool first_run = true;
		struct timeval ts;
		if (first_run) {
			struct timeval te;
			gettimeofday(&ts, NULL);
			gettimeofday(&te, NULL);
			overhead = te.tv_usec - ts.tv_usec;
			first_run = false;
		}
		gettimeofday(&ts, NULL);
		time = ts.tv_sec*(uint64_t)1.0e6 + ts.tv_usec;
	}

	void Timer<true>::end_timer(uint64_t &time) {
		struct timeval t;
		gettimeofday(&t, NULL);
		time = t.tv_sec*(uint64_t)1.0e6 + t.tv_usec;
	}

	double Timer<true>::elapsed_time(const uint64_t &start, const uint64_t &end) {
		uint64_t diff = end - start;
		if (diff < overhead) return 0;
		return (double)(diff - overhead) / 1.0e6;
	}
#endif /* _WIN32 */

	uint64_t Timer<true>::overhead  = 0;
	uint64_t Timer<true>::frequency = 0;

	template <int timer_count>
	void ComboTimer<timer_count, false>::start() {

	}

	template <int timer_count>
	void ComboTimer<timer_count, false>::stop(int index) {

	}

	template <int timer_count>
	double ComboTimer<timer_count, false>::elapsed(int index) const {
		return 0;
	}

	template <int timer_count>
	size_t ComboTimer<timer_count, false>::count() const {
		return 0;
	}

	template <int timer_count>
	ComboTimer<timer_count, true>::ComboTimer(): _start(0), _count(0) {
		for (int i = 0; i != _end.size(); ++i) _end[i] = 0;
	}

	template <int timer_count>
	void ComboTimer<timer_count, true>::start() {
		t.start_timer(_start);
		++_count;
	}

	template <int timer_count>
	void ComboTimer<timer_count, true>::stop(int index) {
		uint64_t elapsed;
		t.end_timer(elapsed);
		_end[index] += t.elapsed_time(_start, elapsed);
	}

	template <int timer_count>
	double ComboTimer<timer_count, true>::elapsed(int index) const {
		return _end[index];
	}

	template <int timer_count>
	size_t ComboTimer<timer_count, true>::count() const {
		return _count;
	}

} /* namespace hdpc */
