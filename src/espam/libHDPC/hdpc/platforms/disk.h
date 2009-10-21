/* $Id: disk.h,v 1.1 2009/10/21 10:30:35 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/platforms/platform.h>
#include <tchar.h>
#include <sstream>

namespace hdpc {
	namespace platform {

		class Storage: public Platform {
		public:
			typedef enum StreamType {STREAM_IN, STREAM_OUT};

			Storage(const TCHAR *filename, StreamType type, size_t bufsize = -1);
			~Storage();
			bool init();
			bool deinit();

			bool allocmem(size_t size, void *&buf);
			bool freemem(void *&buf);
			inline bool write(void *dst, const void *element, size_t size);
			inline bool read(const void *src, void *element, size_t size);

		protected:
			TCHAR *file;
			StreamType mode;
			FILE *fp;
		};

		Storage::Storage(const TCHAR *filename, StreamType type, size_t bufsize): fp(NULL), mode(type) {
			file = _tcsdup(filename);
		}

		Storage::~Storage() {
			free(file);
			file = NULL;
		}

		bool Storage::init() {
			if (fp != NULL) fclose(fp);

			std::stringstream ss;
			ss << "fileIO to" << file;
			if (Platform::get_name() == NULL) Platform::set_name(ss.rdbuf()->str().c_str());

			const TCHAR *m = (mode == Storage::STREAM_IN) ? _T("rb") : _T("wb");
			return _tfopen_s(&fp, file, m) == 0;
		}

		bool Storage::deinit() {
			return fclose(fp) == 0;
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
			return fwrite(element, size, 1, fp) == 1;
		}

		bool Storage::read(const void *src, void *element, size_t size) {
			assert(src == NULL);
			return fread(element, size, 1, fp) == 1;
		}

	} /* namespace platform */
} /* namespace hdpc */
