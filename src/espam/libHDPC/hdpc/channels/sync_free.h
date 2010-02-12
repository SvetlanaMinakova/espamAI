/* $Id: sync_free.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/debug.hpp>
#include <hdpc/channels/base.h>

/* a channel implementation for inter-process communication. Since inside a single process everything
 * is single-threaded, no locking mechanisms are needed. */
namespace hdpc {
	namespace channel {
		namespace lock {

			template <> class Lock<SYNC_FREE>: public LockBase {
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
				size_t bufCount;
			};

			Lock<SYNC_FREE>::Lock(size_t len): LockBase(len), bufCount(0) {}

			bool Lock<SYNC_FREE>::finish() {
				return isEmpty();
			}

			void Lock<SYNC_FREE>::wait_read() {
				if (HDPC_DEBUG_MODE && isEmpty()) debug::error("cannot read from an emtpy channel");
			}

			void Lock<SYNC_FREE>::wait_read_ptr(const index_t&) {
				wait_read();
			}

			void Lock<SYNC_FREE>::release_read(const index_t&) {
				--bufCount;
			}

			void Lock<SYNC_FREE>::wait_write() {
				if (HDPC_DEBUG_MODE && isFull()) debug::error("cannot write to a full channel");
			}

			void Lock<SYNC_FREE>::wait_write_ptr(const index_t&) {
				wait_write();
			}

			void Lock<SYNC_FREE>::release_write(const index_t&) {
				++bufCount;
			}

			bool Lock<SYNC_FREE>::isFull() const {
				return bufCount == LockBase::channelLength;
			}

			bool Lock<SYNC_FREE>::isEmpty() const {
				return bufCount == 0;
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
