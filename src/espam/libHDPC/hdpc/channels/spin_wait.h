/* $Id: spin_wait.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

/* http://www.hpl.hp.com/research/linux/atomic_ops/index.php4 */
#include <atomic_ops.h>

#include <hdpc/stdafx.h>
#include <hdpc/debug.hpp>
#include <hdpc/channels/base.h>

/* Locking implementation using (atomic) spinning on a global variable mechanism */
namespace hdpc { 
	namespace channel { 
		namespace lock {

			template <> class Lock<SPIN>: public LockBase {
			public:
				Lock(size_t len);
				inline bool finish();

				inline void wait_read();
				inline void wait_read_ptr(const index_t&);
				inline void release_read(const index_t&);

				inline void wait_write();
				inline void wait_write_ptr(const index_t&);
				inline void release_write(const index_t&);
			protected:
				inline bool isFull() const;
				inline bool isEmpty() const;
			private:
				volatile AO_t bufCount;
			};

			Lock<SPIN>::Lock(size_t len): LockBase(len), bufCount(0) {}

			bool Lock<SPIN>::finish() {
				return isEmpty();
			}

			void Lock<SPIN>::wait_read() {
				while (isEmpty()) boost::this_thread::yield();
			}

			void Lock<SPIN>::wait_read_ptr(const index_t&) {
				wait_read();
			}

			void Lock<SPIN>::release_read(const index_t&) {
				AO_fetch_and_sub1_full(&bufCount);
			}

			void Lock<SPIN>::wait_write() {
				while (isFull()) boost::this_thread::yield();
			}

			void Lock<SPIN>::wait_write_ptr(const index_t&) {
				wait_write();
			}

			void Lock<SPIN>::release_write(const index_t&) {
				AO_fetch_and_add1_full(&bufCount);
			}

			bool Lock<SPIN>::isFull() const {
				return bufCount == LockBase::channelLength;
			}

			bool Lock<SPIN>::isEmpty() const {
				return bufCount == 0;
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
