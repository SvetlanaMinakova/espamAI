/* $Id: cpu.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/platforms/platform.h>

namespace hdpc {
	namespace platform {

		class CPU: public Platform {
		public:
			bool init();
			bool deinit();

			bool allocmem(size_t size, void *&buf);
			bool freemem(void *&buf);
			inline bool write(void *dst, const void *element, size_t size);
			inline bool read(const void *src, void *element, size_t size);
		};

		bool CPU::init() {
			if (Platform::get_name() == NULL) Platform::set_name("cpu");
			return true;
		}

		bool CPU::deinit() {
			return true;
		}

		bool CPU::allocmem(size_t size, void *&buf) {
			buf = malloc(size);
			return buf != NULL;
		}

		bool CPU::freemem(void *&buf) {
			free(buf);
			buf = NULL;
			return true;
		}

		bool CPU::write(void *dst, const void *element, size_t size) {
			memcpy(dst, element, size);
			return true;
		}

		bool CPU::read(const void *src, void *element, size_t size) {
			memcpy(element, src, size);
			return true;
		}

	} /* namespace platform */
} /* namespace hdpc */
