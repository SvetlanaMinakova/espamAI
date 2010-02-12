/* $Id: acquire_wait.h,v 1.1 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

/* http://www.hpl.hp.com/research/linux/atomic_ops/index.php4 */
#include <atomic_ops.h>

#include <hdpc/stdafx.h>
#include <hdpc/channels/base.h>

/* Locking implementation using (atomic) ACQUIRE-ing on a global variable mechanism */
namespace hdpc { 
	namespace channel { 
		namespace lock {

			#define HDPC_CHANNEL_LOCK_ACQUIRE(NAME)                        \
			template <> class Lock<ACQUIRE_##NAME>: public LockAcq<NAME> { \
			public:                                                        \
				Lock(int size): LockAcq<NAME>(size) {}                       \
			private:                                                       \
				friend class LockAcq<NAME>;                                  \
			};

			template <lock_t T> class LockAcq: public Lock<T> {
			public:
				LockAcq(size_t len);
				inline bool finish();

				inline void wait_read();
				inline void wait_read_ptr(const index_t&);
				inline void release_read(const index_t&);

				inline void wait_write();
				inline void wait_write_ptr(const index_t&);
				inline void release_write(const index_t&);
			private:
				friend class Lock<T>;
				volatile AO_t vbufCount;
			};

			template <lock_t T> LockAcq<T>::LockAcq(size_t len): Lock<T>(len) {
				vbufCount = 0;
			}

			template <lock_t T> bool LockAcq<T>::finish() {
				return Lock<SPIN>::finish();
			}

			template <lock_t T> void LockAcq<T>::wait_read() {
				while (Lock<T>::isEmpty() || vbufCount == 0) boost::this_thread::yield();
				AO_fetch_and_sub1_full(&vbufCount);
			}

			template <lock_t T> void LockAcq<T>::wait_read_ptr(const index_t&) {
				wait_read();
			}

			template <lock_t T> void LockAcq<T>::release_read(const index_t& readIndex) {
				Lock<T>::release_read(readIndex);
				AO_fetch_and_add1_full(&vbufCount);
			}

			template <lock_t T> void LockAcq<T>::wait_write() {
				while (Lock<T>::isFull() || vbufCount == Lock<T>::LockBase::channelLength) boost::this_thread::yield();
			}

			template <lock_t T> void LockAcq<T>::wait_write_ptr(const index_t&) {
				wait_write();
			}

			template <lock_t T> void LockAcq<T>::release_write(const index_t& writeIndex) {
				Lock<T>::release_write(writeIndex);
				AO_fetch_and_sub1_full(&vbufCount);
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */