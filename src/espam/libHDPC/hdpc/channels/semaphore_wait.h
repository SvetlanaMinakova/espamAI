/* $Id: semaphore_wait.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#if defined(_WIN32)
#include <windows.h>
#else
#include <boost/interprocess/sync/interprocess_semaphore.hpp>
#endif /* _WIN32 */

#include <hdpc/stdafx.h>
#include <hdpc/debug.hpp>
#include <hdpc/channels/base.h>

/* Locking implementation using MS Windows semaphores */
namespace hdpc {
	namespace channel {
		namespace lock {

		#if !defined(_WIN32)
			typedef boost::interprocess::interprocess_semaphore HANDLE;
		#endif /* !_WIN32 */

			template<> class Lock<SEMAPHORE>: public LockBase {
			public:
				Lock(size_t len);
				virtual ~Lock();
				inline bool finish();

				inline void wait_read();
				inline void wait_read_ptr(const index_t&);
				inline void release_read(const index_t&);

				inline void wait_write();
				inline void wait_write_ptr(const index_t&);
				inline void release_write(const index_t&);
			private:
				static inline void wait(HANDLE &);
				static inline void release(HANDLE &);

				HANDLE empty, full;
			};

		#if defined(_WIN32)
			Lock<SEMAPHORE>::Lock(size_t len): LockBase(len) {
				empty = CreateSemaphore(NULL, len, len, NULL);
				full  = CreateSemaphore(NULL, 0,   len, NULL);
			}

			Lock<SEMAPHORE>::~Lock() {
				CloseHandle(full);
				CloseHandle(empty);
			};

			bool Lock<SEMAPHORE>::finish() {
				return WaitForSingleObject(full, 0) != WAIT_OBJECT_0;
			};
		#else
			Lock<SEMAPHORE>::Lock(size_t len)
				: LockBase(len)
				, empty(len)
				, full(0)
			{}

			Lock<SEMAPHORE>::~Lock() {};

			bool Lock<SEMAPHORE>::finish() {
				return !full.try_wait();
			};
		#endif /* _WIN32 */

			void Lock<SEMAPHORE>::wait_read() {
				wait(full);
			}

			void Lock<SEMAPHORE>::wait_read_ptr(const index_t&) {
				wait_read();
			}

			void Lock<SEMAPHORE>::release_read(const index_t&) {
				release(empty);
			}

			void Lock<SEMAPHORE>::wait_write() {
				wait(empty);
			}

			void Lock<SEMAPHORE>::wait_write_ptr(const index_t&) {
				wait_write();
			}

			void Lock<SEMAPHORE>::release_write(const index_t&) {
				release(full);
			}

		#if defined(_WIN32)
			void Lock<SEMAPHORE>::wait(HANDLE &h) {
				DWORD res = WaitForSingleObject(h, INFINITE);
				if (HDPC_DEBUG_MODE && res == WAIT_FAILED) debug::error("WaitForSingleObject failed");
			}

			void Lock<SEMAPHORE>::release(HANDLE &h) {
				BOOL res = ReleaseSemaphore(h, 1, NULL);
				if (HDPC_DEBUG_MODE && !res) debug::error("ReleaseSemaphore failed");
			}
		#else
			void Lock<SEMAPHORE>::wait(HANDLE &semaphore) {
				semaphore.wait();
			}

			void Lock<SEMAPHORE>::release(HANDLE &semaphore) {
				semaphore.post();
			}
		#endif /* _WIN32 */

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
