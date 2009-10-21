/* $Id: spin_acquire_wait.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <windows.h>
#include <hdpc/stdafx.h>
#include <hdpc/channels/base.h>
#include <hdpc/channels/spin_wait.h>

/* Locking implementation using (atomic) SPIN_ACQUIRE-ing on a global variable mechanism */
namespace hdpc { 
	namespace channel { 
		namespace lock {

			template <> class Lock<SPIN_ACQUIRE>: public Lock<SPIN> {
			public:
				Lock(size_t len);
				inline bool finish();

				inline void wait_read();
				inline void wait_read_ptr(const index_t&);
				inline void release_read(const index_t&);

				inline void wait_write();
				inline void wait_write_ptr(const index_t&);
				inline void release_write(const index_t&);
			private:
				friend class Lock<SPIN>;
				volatile LONG vbufCount;
			};

			Lock<SPIN_ACQUIRE>::Lock(size_t len): Lock<SPIN>(len) {
				vbufCount = 0;
			}

			bool Lock<SPIN_ACQUIRE>::finish() {
				return Lock<SPIN>::finish();
			}

			void Lock<SPIN_ACQUIRE>::wait_read() {
				while (Lock<SPIN>::isEmpty() || vbufCount == 0) SwitchToThread();
				InterlockedDecrement(&vbufCount);
			}

			void Lock<SPIN_ACQUIRE>::wait_read_ptr(const index_t&) {
				wait_read();
			}

			void Lock<SPIN_ACQUIRE>::release_read(const index_t& readIndex) {
				Lock<SPIN>::release_read(readIndex);
				InterlockedIncrement(&vbufCount);
			}

			void Lock<SPIN_ACQUIRE>::wait_write() {
				while (Lock<SPIN>::isFull() || vbufCount == LockBase::channelLength) SwitchToThread();
			}

			void Lock<SPIN_ACQUIRE>::wait_write_ptr(const index_t&) {
				wait_write();
			}

			void Lock<SPIN_ACQUIRE>::release_write(const index_t& writeIndex) {
				Lock<SPIN>::release_write(writeIndex);
				InterlockedDecrement(&vbufCount);
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
