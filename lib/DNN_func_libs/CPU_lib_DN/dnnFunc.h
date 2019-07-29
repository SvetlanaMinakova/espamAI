/*
 * dnnFunc.h
 *
 *  Created on: 2 Jul 2019
 *      Author: minakovas
 */

#ifndef dnnFunc_H_
#define dnnFunc_H_
#include<map>
#include<string>
using namespace std;

class dnnFunc {
public:
	dnnFunc();
	virtual ~dnnFunc();
         
        //static void hello_from_darknet();

	typedef enum{
	    LOGISTIC, RELU, RELIE, LINEAR, RAMP, TANH, PLSE, LEAKY, ELU, LOGGY, STAIR, HARDTAN, LHTAN, SELU
	} ACTIVATION;

	static void lrn(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void batch_normalization(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void normalize_cpu(float *x, float *mean, float *variance, int batch, int filters, int spatial);

	static void conv(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void maxpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void avgpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);

	static void activation(std::string str_activation, std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static ACTIVATION get_activation(std::string s);
	static void activate_array(float *x, const int n, ACTIVATION a);
	static float activate(float x, ACTIVATION a);

	static void softmax(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void softmax_cpu(float *input, int n, int batch, int batch_offset, int groups, int group_offset, int stride, float temp, float *output);
        static void softmax(float *input, int n, float temp, int stride, float *output);

	static void gemm(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);

	static void gemm(int TA, int TB, int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float BETA,
	        float *C, int ldc);

	static void gemm_cpu(int TA, int TB, int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float BETA,
	        float *C, int ldc);
	static void gemm_nn(int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float *C, int ldc);
	static void gemm_nt(int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float *C, int ldc);

	static void gemm_tn(int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float *C, int ldc);
	static void gemm_tt(int M, int N, int K, float ALPHA,
	        float *A, int lda,
	        float *B, int ldb,
	        float *C, int ldc);

	static void sub_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void div_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void mul_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);

//the rest
	static void fill_cpu(int N, float ALPHA, float *X, int INCX);
	static void copy_cpu(int N, float *X, int INCX, float *Y, int INCY);
	static void scal_cpu(int N, float ALPHA, float *X, int INCX);
	static void pow_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY);
	static void const_cpu(int N, float ALPHA, float *X, int INCX);
	static void axpy_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY);
	static void mul_cpu(int N, float *X, int INCX, float *Y, int INCY);
	static void add_bias(float *output, float *biases, int batch, int n, int size);
	static void scale_bias(float *output, float *scales, int batch, int n, int size);
	static void im2col_cpu(float* data_im,
		        int channels, int height, int width,
		        int ksize, int stride, int pad, float* data_col);
};

#endif /* dnnFunc_H_ */
