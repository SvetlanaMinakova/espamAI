/* $Id: cuda.h,v 1.2 2010/02/12 14:46:28 nikolov Exp $ */
/* $license$ */
#pragma once

#include <hdpc/stdafx.h>
#include <hdpc/platforms/platform.h>
#include <cuda_runtime.h>
#include <sstream>

#pragma comment(lib, "cudart.lib")

namespace hdpc {
	namespace platform {

		class CUDA: public Platform {
		public:
			CUDA(int device_id = 0): device_id(device_id) {}
			bool init();
			bool deinit();

			bool allocmem(size_t size, void *&buf);
			bool freemem(void *&buf);
			inline bool write(void *dst, const void *element, size_t size);
			inline bool read(const void *src, void *element, size_t size);

		private:
			int device_id;
		};

		bool CUDA::init() {
			int device_count;
			cudaError err = cudaGetDeviceCount(&device_count);
			if (err != cudaSuccess || device_id >= device_count) return false;

			cudaDeviceProp prop;
			err = cudaGetDeviceProperties(&prop, device_id);
			std::stringstream ss;
			ss << prop.name << " " << prop.major << "." << prop.minor;
			if (Platform::get_name() == NULL) Platform::set_name(ss.rdbuf()->str().c_str());

			return cudaSetDevice(device_id) == cudaSuccess;
		}

		bool CUDA::deinit() {
			cudaError err = cudaThreadExit();
			return err == cudaSuccess;
		}

		bool CUDA::allocmem(size_t size, void *&buf) {
			cudaError err = cudaMalloc((void**)&buf, size);
			return err == cudaSuccess;
		}

		bool CUDA::freemem(void *&buf) {
			cudaError err = cudaFree(buf);
			buf = NULL;
			return err == cudaSuccess;
		}

		bool CUDA::write(void *dst, const void *element, size_t size) {
			cudaError err = cudaMemcpy(dst, element, size, cudaMemcpyHostToDevice);
			return err == cudaSuccess;
		}

		bool CUDA::read(const void *src, void *element, size_t size) {
			cudaError err = cudaMemcpy(element, src, size, cudaMemcpyDeviceToHost);
			return err == cudaSuccess;
		}

	} /* namespace platform */ 
} /* namespace hdpc */
