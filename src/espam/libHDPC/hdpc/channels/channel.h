/* $Id: channel.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/channels/base.h>

namespace hdpc {
	namespace channel {

		template <typename token_t, lock::lock_t lock_t> class Channel: public ChannelBase {
		public:
			BOOST_STATIC_ASSERT(__is_base_of(LockBase, lock::Lock<lock_t>));

			Channel(size_t length);
			~Channel();

			inline void read(token_t &element);
			inline const token_t &acquire_read_ptr();
			inline void release_read_ptr();

			inline void write(const token_t &element);
			inline token_t &acquire_write_ptr();
			inline void release_write_ptr();

			inline size_t get_sizeof() const;
			inline size_t get_length() const;
			inline void finish();
		private:
			lock::Lock<lock_t> lock;
			index_t readIndex, writeIndex;
			token_t *buffer;
		};

		template <typename token_t, lock::lock_t lock_t>
		Channel<token_t, lock_t>::Channel(size_t length): lock(length), ChannelBase(lock_t) {
			if (HDPC_DEBUG_MODE && length == 0) debug::error("a channel of size 0 is not allowed");
			readIndex = writeIndex = 0;
			buffer = new token_t[length];
		}

		template <typename token_t, lock::lock_t lock_t>
		Channel<token_t, lock_t>::~Channel() {
			delete[] buffer;
		}

		template <typename token_t, lock::lock_t lock_t>
		void Channel<token_t, lock_t>::read(token_t &element) {
			ChannelBase::t_read.start();
			lock.wait_read();
			ChannelBase::t_read.stop(1);

			element = buffer[readIndex];
			lock.increment_read(readIndex);
			lock.release_read(readIndex);

			ChannelBase::t_read.stop(0);
		}

		template <typename token_t, lock::lock_t lock_t>
		const token_t& Channel<token_t, lock_t>::acquire_read_ptr() {
			ChannelBase::t_read.start();
			lock.wait_read_ptr(readIndex);
			ChannelBase::t_read.stop(1);

			const token_t &val = buffer[readIndex];
			lock.increment_read(readIndex);

			ChannelBase::t_read.stop(0);
			return val;
		}

		template <typename token_t, lock::lock_t lock_t>
		void Channel<token_t, lock_t>::release_read_ptr() {
			lock.release_read(readIndex);
		}

		template <typename token_t, lock::lock_t lock_t>
		void Channel<token_t, lock_t>::write(const token_t &element) {
			ChannelBase::t_write.start();
			lock.wait_write();
			ChannelBase::t_write.stop(1);

			buffer[writeIndex] = element;
			lock.increment_write(writeIndex);
			lock.release_write(writeIndex);

			ChannelBase::update_bufcount(readIndex, writeIndex, lock.get_length());
			ChannelBase::t_write.stop(0);
		}

		template <typename token_t, lock::lock_t lock_t>
		token_t& Channel<token_t, lock_t>::acquire_write_ptr() {
			ChannelBase::t_write.start();
			lock.wait_write_ptr(writeIndex);
			ChannelBase::t_write.stop(1);

			token_t &val = buffer[writeIndex];
			lock.increment_write(writeIndex);

			ChannelBase::update_bufcount(readIndex, writeIndex, lock.get_length());
			ChannelBase::t_write.stop(0);
			return val;
		}

		template <typename token_t, lock::lock_t lock_t>
		void Channel<token_t, lock_t>::release_write_ptr() {
			lock.release_write(writeIndex);
		}

		template <typename token_t, lock::lock_t lock_t>
		size_t Channel<token_t, lock_t>::get_length() const {
			return lock.get_length();
		}

		template <typename token_t, lock::lock_t lock_t>
		size_t Channel<token_t, lock_t>::get_sizeof() const {
			return sizeof(token_t);
		}

		template <typename token_t, lock::lock_t lock_t>
		void Channel<token_t, lock_t>::finish() {
			bool res = lock.finish();
			if (!res && HDPC_DEBUG_MODE) debug::error("process is finished but data remains in channel");
		}

	} /* namespace channel */ 
} /* namespace hdpc */