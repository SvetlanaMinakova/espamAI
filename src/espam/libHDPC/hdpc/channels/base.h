/* $Id: base.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/timer.hpp>

namespace hdpc {
	namespace channel {

		typedef size_t index_t;

		/* forward decleration of lock types */
		namespace lock {
			typedef enum lock_t {LOCK_FREE, SEMAPHORE, SPIN, SPIN_ACQUIRE, SYNC_FREE};
			template <lock_t type> class Lock;
		};

		/* type-agonistic base class for channel so we can store these in one place */
		class ChannelBase {
		public:
			ChannelBase(lock::lock_t l);
			virtual ~ChannelBase();
			virtual size_t get_sizeof() const = 0;
			virtual size_t get_length() const = 0;
			virtual void finish() = 0;

			/* idle (wait) and total timers - 2 - */
			ComboTimer<2, HDPC_DEBUG_MODE == 1> t_read, t_write;
			lock::lock_t lock_type;

			inline const char* lock() const;
			inline void update_bufcount(index_t read, index_t write, size_t len);
			inline size_t get_buf_usage() const;
		private:
			size_t _max_buf;
		};

		/* base class for lock types in a channel */
		class LockBase {
		public:
			LockBase(size_t len);

			/* BEGIN: functions lock types HAVE to implement */
			inline void finish();
			inline void wait_read();
			inline void wait_read_ptr(const index_t&);
			inline void release_read(const index_t&);

			inline void wait_write();
			inline void wait_write_ptr(const index_t&);
			inline void release_write(const index_t&);
			/* END:   functions lock types HAVE to implement */

			/* BEGIN: functions lock types CAN override */
			inline void increment_read(index_t  &i);
			inline void increment_write(index_t &i);
			/* END:   functions lock types CAN override */

			inline size_t get_length() const;
		protected:
			const size_t channelLength;
		private:
			inline void increment(index_t &i);
		};

		/* CLASS ChannelBase */
		ChannelBase::ChannelBase(lock::lock_t l): lock_type(l), _max_buf(0) {}

		ChannelBase::~ChannelBase() {}

		const char* ChannelBase::lock() const {
			switch (lock_type) {
				case lock::LOCK_FREE:    return "LOCK_FREE";
				case lock::SEMAPHORE:    return "SEMAPHORE";
				case lock::SPIN:         return "SPIN";
				case lock::SPIN_ACQUIRE: return "SPIN_ACQUIRE";
				case lock::SYNC_FREE:    return "SYNC_FREE";
			}
			return "";
		}

		void ChannelBase::update_bufcount(index_t read, index_t write, size_t len) {
		#if defined(HDPC_DEBUG_MODE)
			size_t count = (write >= read) ? write - read : len - read + write;
			if (count > _max_buf) _max_buf = count;
		#endif /* HDPC_DEBUG_MODE */
		}

		size_t ChannelBase::get_buf_usage() const {
			return _max_buf;
		}

		/* CLASS LockBase */
		LockBase::LockBase(size_t len): channelLength(len) {}

		/* BEGIN: functions lock types CAN override */
		void LockBase::increment_read(index_t  &index) {
			increment(index);
		}
		void LockBase::increment_write(index_t &index) {
			increment(index);
		}

		void LockBase::increment(index_t &index) {
			index++;
			if (index == channelLength) index = 0;
		}

		size_t LockBase::get_length() const {
			return channelLength;
		}

	} /* namespace channel */
} /* namespace hdpc */
