/* $Id: disk.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/platforms/platform.h>
#include <sstream>
#include <fstream>

namespace hdpc {
	namespace platform {

		class Storage: public Platform {
		public:
			enum StreamType {STREAM_IN, STREAM_OUT};

			Storage(const char *filename, StreamType type, size_t bufsize = -1);
			~Storage();
			bool init();
			bool deinit();

			bool allocmem(size_t size, void *&buf);
			bool freemem(void *&buf);
			inline bool write(void *dst, const void *element, size_t size);
			inline bool read(const void *src, void *element, size_t size);

		protected:
			std::string file;
			StreamType mode;
			std::fstream fp;
		};

		Storage::Storage(const char *filename, StreamType type, size_t bufsize): mode(type) {
			file = filename;
		}

		Storage::~Storage() {
		}

		bool Storage::init() {
			if (fp.is_open()) fp.close();

			std::stringstream ss;
			ss << "fileIO to" << file;
			if (Platform::get_name() == NULL) Platform::set_name(ss.rdbuf()->str().c_str());

			fp.open(file.c_str(), std::ios_base::binary | ((mode == Storage::STREAM_IN) ? std::ios_base::in : std::ios_base::out));
			return fp.good();
		}

		bool Storage::deinit() {
			fp.close();
			return !fp.fail();
		}

		bool Storage::allocmem(size_t size, void *&buf) {
			buf = NULL;
			return true;
		}

		bool Storage::freemem(void *&buf) {
			buf = NULL;
			return true;
		}

		bool Storage::write(void *dst, const void *element, size_t size) {
			assert(dst == NULL);
			fp.write(static_cast<const char*>(element), size);
			return !fp.bad();
		}

		bool Storage::read(const void *src, void *element, size_t size) {
			assert(src == NULL);
			fp.read(static_cast<char*>(element), size);
			return !fp.bad();
		}

	} /* namespace platform */
} /* namespace hdpc */
