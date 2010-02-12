/* $Id: platform.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>

namespace hdpc {
	namespace platform {

		class Platform {
		public:
			Platform(): _name(NULL) {}
			virtual ~Platform();

			/* Real classes need to implement these functions */
			virtual bool init()              = 0; // initialiser with parameters from constructor
			virtual bool deinit()            = 0; // deinitialiser, eg close handles, etc.
			const char *get_name() const;         // get the name of process
			void set_name(const char* str);       // set the name of the process

			virtual bool allocmem(size_t size, void *&buf) = 0;                  // memory allocator
			virtual bool freemem(void *&buf) = 0;                                // free allocated memory
			virtual bool write(void *dst, const void *element, size_t size) = 0; // write to action-memory
			virtual bool read(const void *src, void *element, size_t size) = 0;  // read from action-memory
		private:
			char* _name; // a custom name assigned to the process
		};

		Platform::~Platform() {
			free(_name);
			_name = NULL;
		}

		const char *Platform::get_name() const {
			return _name;
		}

		void Platform::set_name(const char *str) {
			free(_name);
			_name = _strdup(str);
		}

	} /* namespace platform */
} /* namespace hdpc */

