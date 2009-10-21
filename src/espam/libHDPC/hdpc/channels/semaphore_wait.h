/* $Id: semaphore_wait.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <windows.h>
#include <hdpc/stdafx.h>
#include <hdpc/debug.hpp>
#include <hdpc/channels/base.h>

/* Locking implementation using MS Windows semaphores */
namespace hdpc {
	namespace channel {
		namespace lock {

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
				static inline void wait(HANDLE &h);
				static inline void release(HANDLE &h);

				HANDLE empty, full;
			};

			Lock<SEMAPHORE>::Lock(size_t len): LockBase(len) {
				empty = CreateSemaphore(NULL, len, len, NULL);
				full  = CreateSemaphore(NULL, 0,   len, NULL);
			}

			Lock<SEMAPHORE>::~Lock() {
				CloseHandle(full);
				CloseHandle(empty);
			};

			void Lock<SEMAPHORE>::finish() {
				return WaitForSingleObject(full, 0) != WAIT_OBJECT_0;
			};

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

			void Lock<SEMAPHORE>::wait(HANDLE &h) {
				DWORD res = WaitForSingleObject(h, INFINITE);
				if (HDPC_DEBUG_MODE && res == WAIT_FAILED) debug::error("WaitForSingleObject failed");
			}

			void Lock<SEMAPHORE>::release(HANDLE &h) {
				BOOL res = ReleaseSemaphore(h, 1, NULL);
				if (HDPC_DEBUG_MODE && !res) debug::error("ReleaseSemaphore failed");
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
