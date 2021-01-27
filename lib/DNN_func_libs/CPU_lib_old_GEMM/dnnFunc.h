//File automatic generated by espamAI

#ifndef dnnFunc_H_
#define dnnFunc_H_


#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>
#include <map>

class dnnFunc {
public:
	dnnFunc();
	virtual ~dnnFunc();

	typedef enum{
	    LOGISTIC, RELU, RELIE, LINEAR, RAMP, TANH, PLSE, LEAKY, ELU, LOGGY, STAIR, HARDTAN, LHTAN, SELU
	} ACTIVATION;


    //convolution
    static void conv (std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    static void convolution_3d_inp_gpu(float* input, float* kernel, float* output, int channels, int input_h, int input_w,
				int output_h,int output_w, int k_h, int k_w, int stride);
    
    static void convolution_3d_inp_cpu(float* input, float* kernel, float* output,float*bias, int channels, int input_h, int input_w,
				 int output_d, int output_h,int output_w, int k_h, int k_w, int stride);
    //dense(FC): matmul and gemm
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
    
    //max and average pooling 
    static void maxpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    static void avgpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    
    //Nonlinear (activation) functions
    static void activation(std::string str_activation, std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    static ACTIVATION get_activation(std::string s);
    static void activate_array(float *x, const int n, ACTIVATION a);
    static float activate(float x, ACTIVATION a);

    static void execute_relu (float* input, float* output, int input_len);
    static void execute_sigm (float* input, float* output, int input_len);
    static void execute_thn (float* input, float* output, int input_len);

    static void execute_softmax( float *non_activated_stages, float *output, int len);
    static void softmax(float *input, int n, float *output);
    
    static void softmax(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    //helpers

    static void execute_addconst (float* input, float* weights, float* output, int input_len);
    static void transpose(float *input, int inp_h, int inp_w);
    static void envide_input(float *inp, float* envided, int inp_d, int inp_h, int inp_w, int h_pad, int w_pad);
    static void init_zeros(float *matrix, int h, int w);
    static void init_zeros(float *matrix, int d, int h, int w);
    static void init_output(float *arr, float* bias, int d, int h, int w);

    //normalizations
    static void lrn(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    static void batch_normalization(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
    static void normalize_cpu(float *x, float *mean, float *variance, int batch, int filters, int spatial);


//const funcs
	static void sub_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void div_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);
	static void mul_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr);

//const cpu funcs
	static void fill_cpu(int N, float ALPHA, float *X, int INCX);
	static void copy_cpu(int N, float *X, int INCX, float *Y, int INCY);
	static void scal_cpu(int N, float ALPHA, float *X, int INCX);
	static void pow_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY);
	static void const_cpu(int N, float ALPHA, float *X, int INCX);
	static void axpy_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY);
	static void mul_cpu(int N, float *X, int INCX, float *Y, int INCY);
	static void add_bias(float *output, float *biases, int batch, int n, int size);
	static void scale_bias(float *output, float *scales, int batch, int n, int size);
};
#endif /* dnnFunc_H_ */