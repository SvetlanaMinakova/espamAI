/* $Id: lock_free.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/channels/base.h>

/* Locking implementation using a LOCK_FREE mechanism */
namespace hdpc {
	namespace channel {
		namespace lock {

			template<> class Lock<LOCK_FREE>: public LockBase {
			public:
				Lock(size_t len);
				inline bool finish();

				inline void wait_read();
				inline void wait_read_ptr(const index_t&);
				inline void increment_read(index_t&);
				inline void release_read(const index_t&);

				inline void wait_write();
				inline void wait_write_ptr(const index_t&);
				inline void increment_write(index_t&);
				inline void release_write(const index_t&);
			private:
				index_t readIndexLocal, writeIndexLocal;
				index_t readBuffIndex,  writeBuffIndex;
				index_t readFlag, writeFlag;

				static const int FULL_FLAG = 0x80000000;
			};

			Lock<LOCK_FREE>::Lock(size_t len): LockBase(len) {
				readIndexLocal = writeIndexLocal = 0;
				readBuffIndex  = writeBuffIndex  = 0;
				readFlag = writeFlag = 0;
			}

			bool Lock<LOCK_FREE>::finish() {
				return readBuffIndex == writeBuffIndex;
			}

			void Lock<LOCK_FREE>::wait_read() {	
				while (readBuffIndex == writeIndexLocal) {
					writeIndexLocal = writeBuffIndex;
					if (readBuffIndex != writeIndexLocal) return; 

					boost::this_thread::yield();
				}
			}

			void Lock<LOCK_FREE>::wait_read_ptr(const index_t& readIndex) {
				/* The 'while' check for full status is not like in wait_read.
				 * readBuffIndex is updated by the release_read() function and in case of 
				 * multiple writes without release -> wrong FIFO synchronization 
				 * Therefore, explicit check using readFlag and readIndex (instead of readBuffIndex) is required */
				while ((readFlag | readIndex) == writeIndexLocal) {
					writeIndexLocal = writeBuffIndex;
					if ((readFlag | readIndex) != writeIndexLocal) return; 

					boost::this_thread::yield();
				}
			}

			void Lock<LOCK_FREE>::increment_read(index_t& readIndex) {
				readIndex++;
				if (readIndex == LockBase::channelLength) {
					readIndex = 0;
					readFlag ^= FULL_FLAG;
				}
			}

			void Lock<LOCK_FREE>::release_read(const index_t& readIndex) {
				readBuffIndex = readFlag | readIndex;
			}

			void Lock<LOCK_FREE>::wait_write() {
				while ((writeBuffIndex ^ readIndexLocal) == FULL_FLAG) {
					readIndexLocal = readBuffIndex;
					if ((writeBuffIndex ^ readIndexLocal) != FULL_FLAG) return;

					boost::this_thread::yield();
				}
			}

			void Lock<LOCK_FREE>::wait_write_ptr(const index_t& writeIndex) {
				/* The 'while' check for full status is not like in wait_write.
				 * writeBuffIndex is updated by the release_write() function and in case of 
				 * multiple writes without release -> wrong FIFO synchronization 
				 * Therefore, explicit check using writeFlag and writeIndex (instead of writeBuffIndex) is required */
				while (((writeFlag | writeIndex) ^ readIndexLocal) == FULL_FLAG) {
					readIndexLocal = readBuffIndex;
					if (((writeFlag | writeIndex) ^ readIndexLocal) != FULL_FLAG) return; 
					
					boost::this_thread::yield();
				}
			}

			void Lock<LOCK_FREE>::increment_write(index_t& writeIndex) {
				writeIndex++;
				if (writeIndex == LockBase::channelLength) {
					writeIndex = 0;
					writeFlag ^= FULL_FLAG;
				}
			}

			void Lock<LOCK_FREE>::release_write(const index_t& writeIndex) {
				writeBuffIndex = writeFlag | writeIndex;
			}

		} /* namespace lock */
	} /* namespace channel */
} /* namespace hdpc */
