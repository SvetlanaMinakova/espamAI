#include "dnnFunc.h"
#include <iostream>
#include <math.h>
#include <cstdint>
#include <limits>
#include "appFunc.h"
#include <sys/time.h>

using namespace std;

dnnFunc::dnnFunc() {

}

dnnFunc::~dnnFunc() {

}


///////////////////////////
////// CONVOLUTION ////////

/**convolution for GPU function*/
extern void kernelhost_3d (float *input, float *weights, float *output, float *bias, int channels, int input_h, int input_w, int output_d, int output_h, int output_w, int k_h, int k_w, int stride, int blocksize);

/** convolution param interface function*/
void dnnFunc::conv (std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
  {

  float *input = tensor_params_ptr->at("input");
  float *output = tensor_params_ptr->at("output");
  float *weights = tensor_params_ptr->at("weights");
  float *bias = tensor_params_ptr->at("bias");

  int input_len = int_params_ptr->at("input_len");
  int output_len = int_params_ptr->at("output_len");

  int stride=int_params_ptr->at("stride");
  int k_h = int_params_ptr->at("k_h");
  int k_w = int_params_ptr->at("k_w");

  int channels = int_params_ptr->at("channels");
  int input_h = int_params_ptr->at("h");
  int input_w = int_params_ptr->at("w");

  int output_d = int_params_ptr->at("neurons");;
  int output_h = int_params_ptr->at("out_h");
  int output_w =  int_params_ptr->at("out_w");

  bool pads = (bool)int_params_ptr->at("pads");
  
  int core_id = int_params_ptr->at("core_id");
  int gpu = int_params_ptr->at("gpu");
  int blocksize = int_params_ptr->at("block_size");
  
  bool use_gpu = false;
  if(gpu>=0) {use_gpu = true;}
 
  //no pads defined
  if(!pads){ 
        if(use_gpu) 
            kernelhost_3d(input, weights, output, bias, channels,input_h, input_w, output_d, output_h, output_w, k_h, k_w, stride, blocksize);
        else 
	   dnnFunc::convolution_3d_inp_cpu(input, weights, output, bias, channels,input_h, input_w, output_d, output_h, output_w, k_h, k_w, stride);
	
        return;
   }
   
   //pads defined
   int w_pad = int_params_ptr->at("pad_w0");
   int h_pad = int_params_ptr->at("pad_h0");
   int e_i_h = input_h + h_pad * 2;
   int e_i_w = input_w + w_pad * 2;

   float inp_envided[channels * e_i_h * e_i_w] = {0};
   dnnFunc::envide_input(input,&inp_envided[0],channels,input_h,input_w,h_pad,w_pad);

   if (use_gpu) 
	kernelhost_3d(&inp_envided[0], weights, output, bias, channels, e_i_h, e_i_w, output_d, output_h, output_w, k_h, k_w, stride, blocksize);        
   else 
	dnnFunc::convolution_3d_inp_cpu(&inp_envided[0], weights, output, bias, channels,e_i_h, e_i_w, output_d, output_h, output_w, k_h, k_w, stride);
}

/** convolution for CPU function*/
void dnnFunc::convolution_3d_inp_cpu(float *input, float *weights, float *output, float* bias, int channels, int input_h,
 int input_w,int output_d, int output_h,int output_w, int k_h, int k_w, int stride){
         
	  //init output
	  if(bias==nullptr){
	    for (int i = 0; i < output_d * output_h * output_w; i++)
	      output[i] = 0;
	    }
	  else {
	    float bias_val;
	    for (int d = 0; d < output_d; d++) {
	      bias_val = bias[d];
	      for (int i = 0; i < output_w * output_h; i++)
	        output[i + d * output_w * output_h] = bias_val;
	      }
	    }

  float fold_cell = 0;
  int input_elem_ind = 0;
  int k_elem_ind = 0;
  int k_elem_ind_reverse = 0;
  int k_size = k_h*k_w;
  int outp_elem_ind =0;
  int sub_kern_start = 0;

  float *sub_input;
  float *sub_output;
  float *sub_kernel;
  
for (int ofm=0; ofm<output_d;ofm++){
  
  sub_output = output + output_h * output_w * ofm;
  for (int ifm=0; ifm<channels;ifm++){

    sub_input = input + input_w * input_h * ifm;
    sub_kern_start =  k_h * k_w * ifm  + k_h * k_w * channels * ofm;

    sub_kernel = weights + sub_kern_start;

     for (int j = 0; j < output_h; j++){
     for (int i = 0; i < output_w; i++){


        fold_cell = 0;
         //summ k,l
        for (int l = 0; l < k_h; l++){
          for (int k = 0; k < k_w; k++){
            k_elem_ind = l + k * k_h;
            input_elem_ind = (j*stride+l) + (i*stride+k)* input_w;
            fold_cell += *(sub_input + input_elem_ind) * *(sub_kernel + k_elem_ind);
 
          }
        }
        outp_elem_ind = i * output_w + j;
        sub_output[outp_elem_ind] += fold_cell;
        fold_cell = 0;
}
}
}
}

}


///////////////////////////////////////
///// Dense function (MATMUL/GEMM) /////
void dnnFunc::execute_dense_block (std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){
  

  float *input = tensor_params_ptr->at("input");
  float *output = tensor_params_ptr->at("output");
  float *weights = tensor_params_ptr->at("weights");
  float *bias = tensor_params_ptr->at("bias");

  int input_len = int_params_ptr->at("input_len");
  int output_len = int_params_ptr->at("output_len");

  
  int core_id = int_params_ptr->at("core_id");
  /**TODO: GPU use condition here!*/
  bool use_gpu = false;
  if(core_id>8) {use_gpu = true;}

  if(use_gpu){
  	// TODO:USE GPU MATMUL instead!
  	if(bias==nullptr) dnnFunc::execute_matmul(input, weights, output, input_len, output_len);
  	// TODO:USE GPU GEMM instead!
  	else dnnFunc::execute_gemm(input, weights, output, bias, input_len, output_len);
	}

  else{
  	//MATMUL
  	if(bias==nullptr) dnnFunc::execute_matmul(input, weights, output, input_len, output_len);
  	//GEMM
  	else dnnFunc::execute_gemm(input, weights, output, bias, input_len, output_len);
      }
}

/**matmul*/
void dnnFunc::execute_matmul(float *input, float *weights, float *result, int input_len, int output_len){
  for (int j=0; j<output_len; j++){
    *(result+j) = 0;
    for(int i=0; i<input_len; i++)
      *(result+j) +=*(input + i) * *(weights + i + j*input_len);
}
}

/**gemm*/
void dnnFunc::execute_gemm(float *input, float *weights, float *result, float *bias, int input_len, int output_len){
  for (int j=0; j<output_len; j++){
    *(result+j) = *(bias+j);
    for(int i=0; i<input_len; i++)
      *(result+j) +=*(input + i) * *(weights + i + j*input_len);
}
}

//////////////////////////////////
//   BATCHNORMALIZATION        ///

void dnnFunc::batch_normalization(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){
	//params initialization
	    int h = int_params_ptr->at("h");
	    int w = int_params_ptr->at("w");
	    int c = int_params_ptr->at("channels");
	    int batch = int_params_ptr->at("batch");

	    float *netinput = tensor_params_ptr->at("input");
	    float *output = tensor_params_ptr->at("output");

		int inputs = int_params_ptr->at("input_len");

		int neurons = int_params_ptr->at("neurons");
		int out_h = int_params_ptr->at("out_h");
		int out_w = int_params_ptr->at("out_w");
		int outputs = int_params_ptr->at("output_len");

		float *biases = tensor_params_ptr->at("bias");
		float *scale = tensor_params_ptr->at("scale");
		float *mean = tensor_params_ptr->at("mean"); //rolling mean
		float *variance = tensor_params_ptr->at("variance"); //rolling variance

		/**
		 **/
	copy_cpu(outputs*batch, netinput, 1, output, 1); //copy input to output
    //copy_cpu(outputs*batch, output, 1, l.x, 1); //copy output into layer.x?

    /**if(net.train){ //we do not support training
        mean_cpu(l.output, l.batch, l.out_c, l.out_h*l.out_w, l.mean);
        variance_cpu(l.output, l.mean, l.batch, l.out_c, l.out_h*l.out_w, l.variance);

        scal_cpu(l.out_c, .99, l.rolling_mean, 1);
        axpy_cpu(l.out_c, .01, l.mean, 1, l.rolling_mean, 1);
        scal_cpu(l.out_c, .99, l.rolling_variance, 1);
        axpy_cpu(l.out_c, .01, l.variance, 1, l.rolling_variance, 1);

        normalize_cpu(l.output, l.mean, l.variance, l.batch, l.out_c, l.out_h*l.out_w);
        copy_cpu(l.outputs*l.batch, l.output, 1, l.x_norm, 1);
    } else {*/
        normalize_cpu(output, mean, variance, batch, neurons, out_h*out_w);
    //}
    scale_bias(output, scale, batch, neurons, out_h*out_w);

	if(biases!=nullptr)
	    add_bias(output, biases, batch, neurons, out_h*out_w);
}

void dnnFunc::normalize_cpu(float *x, float *mean, float *variance, int batch, int filters, int spatial)
{
    int b, f, i;
    for(b = 0; b < batch; ++b){
        for(f = 0; f < filters; ++f){
            for(i = 0; i < spatial; ++i){
                int index = b*filters*spatial + f*spatial + i;
                x[index] = (x[index] - mean[f])/(sqrt(variance[f]) + .000001f);
            }
        }
    }
}

//////////////////////////////////////////////////
// LRN (LOCAL RESPONSE normalization)         ///

void dnnFunc::lrn(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){
//params initialization
    int h = int_params_ptr->at("h");
    int w = int_params_ptr->at("w");
    int c = int_params_ptr->at("channels");
    int batch = int_params_ptr->at("batch");

    int size = int_params_ptr->at("size");
    float alpha = (float)int_params_ptr->at("alpha") / (float)int_params_ptr->at("alpha_scale");
    float beta = (float)int_params_ptr->at("beta") / (float)int_params_ptr->at("beta_scale");
    float kappa = (float)int_params_ptr->at("kappa") / (float)int_params_ptr->at("kappa_scale");

    float *netinput = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");

    /** move squared and norms to LRN node! */
    float *lsquared = tensor_params_ptr->at("squared");
    float *lnorms = tensor_params_ptr->at("norms");

/**
    float *lsquared = (float *)calloc(h * w * c * batch, sizeof(float));
    float *lnorms = (float *)calloc(h * w * c * batch, sizeof(float));
*/

//darknet lrn
    int k,b;

        scal_cpu(w*h*c*batch, 0, lsquared, 1);

        for(b = 0; b < batch; ++b){
            float *squared = lsquared + w*h*c*b;
            float *norms   = lnorms + w*h*c*b;
            float *input   = netinput + w*h*c*b;
            pow_cpu(w*h*c, 2, input, 1, squared, 1);

            const_cpu(w*h, kappa, norms, 1);
            for(k = 0; k < size/2; ++k){
                axpy_cpu(w*h, alpha, squared + w*h*k, 1, norms, 1);
            }

            for(k = 1; k < c; ++k){
                copy_cpu(w*h, norms + w*h*(k-1), 1, norms + w*h*k, 1);
                int prev = k - ((size-1)/2) - 1;
                int next = k + (size/2);
                if(prev >= 0)      axpy_cpu(w*h, -alpha, squared + w*h*prev, 1, norms + w*h*k, 1);
                if(next < c) axpy_cpu(w*h,  alpha, squared + w*h*next, 1, norms + w*h*k, 1);
            }
        }
        pow_cpu(w*h*c*batch, -beta, lnorms, 1, output, 1);
        mul_cpu(w*h*c*batch, netinput, 1, output, 1);
}

///////////////////////////////
//            MAXPOOL      ///

void dnnFunc::maxpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{
    float FLT_MAX = std::numeric_limits<float>::max();
//parameters setup
    int batch = int_params_ptr->at("batch");
    int groups = int_params_ptr->at("groups");
    int size = int_params_ptr->at("k_w");
    int stride = int_params_ptr->at("stride");

    int ch = int_params_ptr->at("channels");
    int lh = int_params_ptr->at("h");
    int lw = int_params_ptr->at("w");

   // int neurons = int_params_ptr->at("neurons");
    int out_h = int_params_ptr->at("out_h");
    int out_w = int_params_ptr->at("out_w");
    //int outputs = int_params_ptr->at("output_len");

    float *input = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");
    //float *biases = tensor_params_ptr->at("bias");

    int pad =0;
    int pads = int_params_ptr->at("pads");
    if (pads)
	pad = int_params_ptr->at("pad_w0");

//DARKNET maxpool
    int b,i,j,k,m,n;
        int w_offset = pad/2;
        int h_offset = pad/2;

        int h = out_h;
        int w = out_w;
        int c = ch;

        for(b = 0; b < batch; ++b){
            for(k = 0; k < c; ++k){
                for(i = 0; i < h; ++i){
                    for(j = 0; j < w; ++j){
                        int out_index = j + w*(i + h*(k + c*b));
                        float max = -FLT_MAX;
                        int max_i = -1;
                        for(n = 0; n < size; ++n){
                            for(m = 0; m < size; ++m){
                                int cur_h = h_offset + i*stride + n;
                                int cur_w = w_offset + j*stride + m;
                                int index = cur_w + lw*(cur_h + lh*(k + b*ch));
                                int valid = (cur_h >= 0 && cur_h < lh &&
                                             cur_w >= 0 && cur_w < lw);
                                float val = (valid != 0) ? input[index] : -FLT_MAX;
                                max_i = (val > max) ? index : max_i;
                                max   = (val > max) ? val   : max;
                            }
                        }
                        output[out_index] = max;
                        //l.indexes[out_index] = max_i;??
                    }
                }
            }
        }
}

//////////////////////////////////////////////////
//////// AVGPOOL ////////////////////////////////

void dnnFunc::avgpool(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{
	//parameters setup
	    int batch = int_params_ptr->at("batch");
	    int size = int_params_ptr->at("k_w");
	    int stride = int_params_ptr->at("stride");

	    int ch = int_params_ptr->at("channels");
	    int lh = int_params_ptr->at("h");
	    int lw = int_params_ptr->at("w");

	   // int neurons = int_params_ptr->at("neurons");
	    int out_h = int_params_ptr->at("out_h");
	    int out_w = int_params_ptr->at("out_w");
	    //int outputs = int_params_ptr->at("output_len");

	    float *input = tensor_params_ptr->at("input");
	    float *output = tensor_params_ptr->at("output");

	    int pad =0;
            int pads = int_params_ptr->at("pads");
            if (pads)
		pad = int_params_ptr->at("pad_w0");
//avg_pool
	    int b,i,j,k,m,n;
	            int w_offset = pad/2;
	            int h_offset = pad/2;

	            int h = out_h;
	            int w = out_w;
	            int c = ch;
	            float sqrtsize = (float) (size*size);

	            for(b = 0; b < batch; ++b){
	                for(k = 0; k < c; ++k){
	                    for(i = 0; i < h; ++i){
	                        for(j = 0; j < w; ++j){
	                            int out_index = j + w*(i + h*(k + c*b));
	                            float val = 0;
	                            for(n = 0; n < size; ++n){
	                                for(m = 0; m < size; ++m){
	                                    int cur_h = h_offset + i*stride + n;
	                                    int cur_w = w_offset + j*stride + m;
	                                    int index = cur_w + lw*(cur_h + lh*(k + b*ch));
	                                    val += input[index];
	                                }
	                            }
	                            output[out_index] = val/sqrtsize;
	                        }
	                    }
	                }
	            }
}

//////////////////////////////////////////////////////
///// CONST ARITHMETIC (SUBCONST, DIVCONST, MULCONST)

void dnnFunc::sub_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{
    int outputs = int_params_ptr->at("output_len");
    float *input = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");
    float constval = (float)int_params_ptr->at("constval") / (float)int_params_ptr->at("constval_scale");
    
    int i;
    for(i = 0; i < outputs; ++i){
    	output[i] = input[i] - constval;
    }
}

void dnnFunc::div_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{
    int outputs = int_params_ptr->at("output_len");
    float *input = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");
    float constval = (float)int_params_ptr->at("constval") / (float)int_params_ptr->at("constval_scale");
    if (constval==0){
    std::cout<<"div null is not possible! check div nodes!";
	return;
    }
    int i;
    for(i = 0; i < outputs; ++i){
    	output[i] = input[i] / constval;
    }
}

void dnnFunc::mul_const(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{
    int outputs = int_params_ptr->at("output_len");
    float *input = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");
    float constval = (float)int_params_ptr->at("constval") / (float)int_params_ptr->at("constval_scale");
    if (constval==0){
    std::cout<<"div null is not possible! check div nodes!";
	return;
    }
    int i;
    for(i = 0; i < outputs; ++i){
    	output[i] = input[i] * constval;
    }
}

/////////////////////////////////
//           SOFTMAX         ///

void dnnFunc::softmax(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr)
{

    //parameters setup
    int inputs = int_params_ptr->at("input_len");
    int outputs = int_params_ptr->at("output_len");

    float *input = tensor_params_ptr->at("input");
    float *output = tensor_params_ptr->at("output");

    int i;
    float sum = 0;
    float largest = -numeric_limits<float>::max();
    for(i = 0; i < inputs; ++i){
        if(input[i] > largest) largest = input[i];
    }
    for(i = 0; i < inputs; ++i){
        float e = exp(input[i] - largest);
        sum += e;
        output[i] = e;
    }
    for(i = 0; i < inputs; ++i){
        output[i] /= sum;
    }
}

///////////////////////////////
//  ACTIVATION (LINEAR, LOGISTIC, RELU,...)   ///


dnnFunc::ACTIVATION dnnFunc::get_activation(std::string function)
{
    if (function.find("logistic") != std::string::npos || function.find("SIGM")!= std::string::npos) return LOGISTIC;
    if (function.find("loggy") != std::string::npos) return LOGGY;
    if (function.find("relu") != std::string::npos || function.find("ReLU") != std::string::npos) return RELU;
    if (function.find("elu") != std::string::npos) return ELU;
    if (function.find("selu") != std::string::npos || function.find("SELU") != std::string::npos) return SELU;
    if (function.find("relie") != std::string::npos) return RELIE;
    if (function.find("plse") != std::string::npos) return PLSE;
    if (function.find("hardtan")!= std::string::npos) return HARDTAN;
    if (function.find("lhtan") != std::string::npos) return LHTAN;
    if (function.find("linear") != std::string::npos) return LINEAR;
    if (function.find("ramp") != std::string::npos) return RAMP;
    if (function.find("leaky") != std::string::npos) return LEAKY;
    if (function.find("tanh") != std::string::npos || function.find("THN") != std::string::npos) return TANH;
    if (function.find("stair")!= std::string::npos) return STAIR;
    //fprintf(stderr, "Couldn't find activation function %s, going with ReLU\n", function);
    return RELU;
}


void dnnFunc::activation(std::string str_activation, std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){
	int batch = int_params_ptr->at("batch");
	int outputs = int_params_ptr->at("output_len");

	float *input = tensor_params_ptr->at("input");
	float *output = tensor_params_ptr->at("output");

	copy_cpu(outputs*batch, input, 1, output, 1);
	ACTIVATION activation = dnnFunc::get_activation(str_activation);
	dnnFunc::activate_array(output, outputs*batch, activation);
}

void dnnFunc::activate_array(float *x, const int n, dnnFunc::ACTIVATION a)
{
    int i;
    for(i = 0; i < n; ++i){
    	x[i] = dnnFunc::activate(x[i], a);
    }
}

float dnnFunc::activate(float x, ACTIVATION a)
{
    switch(a){
        case LINEAR:
        return x;

        case LOGISTIC:
        	return 1./(1. + exp(-x));
        /**case LOGGY:
            return loggy_activate(x);*/

        case RELU:
            return x*(x>0);

        /**case ELU:
            return elu_activate(x);*/
        case SELU:
        	return (x >= 0)*1.0507*x + (x < 0)*1.0507*1.6732*(exp(x)-1);

        /**case RELIE:
            return relie_activate(x);
        case RAMP:
            return ramp_activate(x);
        case LEAKY:
            return leaky_activate(x);*/
        case TANH:
        	return (exp(2*x)-1)/(exp(2*x)+1);

        /**case PLSE:
            return plse_activate(x);
        case STAIR:
            return stair_activate(x);
        case HARDTAN:
            return hardtan_activate(x);
        case LHTAN:
            return lhtan_activate(x);*/
        default: return 0;
    }
    return 0;
}

void dnnFunc::init_zeros(float *matrix, int h, int w){
	for (int j = 0; j < h; j++){
		for (int i = 0; i < w; i++){
			matrix[i * w + j]=0;
		}
	}}

void dnnFunc::init_zeros(float *matrix, int d, int h, int w){
	for (int k = 0; k < d; k++){
		for (int j = 0; j < h; j++){
			for (int i = 0; i < w; i++){
				matrix[i * w + j]=0;
				}
	}		}
}

void dnnFunc::softmax(float *input, int n, float *output)
{
    int i;
    float sum = 0;
    float largest = -numeric_limits<float>::max();
    for(i = 0; i < n; ++i){
        if(input[i] > largest) largest = input[i];
    }
    for(i = 0; i < n; ++i){
        float e = exp(input[i] - largest);
        sum += e;
        output[i] = e;
    }
    for(i = 0; i < n; ++i){
        output[i] /= sum;
    }
}


void dnnFunc::execute_sigm (float* input, float* output, int input_len){
    for(int i = 0; i < input_len; i++)
        output[i] = 1 / (1 + (exp(-1 * input[i])));
}

void dnnFunc::execute_thn (float* input, float* output, int input_len){
    for(int i = 0; i < input_len; i++)
        output[i] = tanh(input[i]);
}

void dnnFunc::transpose(float *input, int inp_h, int inp_w){
	float tmp[inp_w][inp_h] = {0};

	for(int j=0; j<inp_h; j++){
		for(int i=0; i<inp_w; i++){
		tmp[i][j] = *(input+i+j*inp_w);
		}
	}

	for(int j=0; j<inp_h; j++){
		for(int i=0; i<inp_w; i++){
		*(input+j+i*inp_h) = tmp[i][j];
		}
	}

	std::cout<<std::endl;

}

void dnnFunc::envide_input(float *input, float* envided, int inp_d, int inp_h, int inp_w, int h_pad, int w_pad){
	   float *sub_input;
	   float *sub_input_line;
	   float *sub_envided;
	   float *sub_envided_line;

	   for(int d=0; d<inp_d; d++){
		   sub_input = input + inp_w * inp_h * d;

		   sub_envided = envided + (inp_h + h_pad*2) * (inp_w + w_pad * 2) * d;
		   for(int j=0; j<(inp_h+h_pad*2); j++){
			   for(int i=0; i<(inp_w+w_pad*2); i++){
				  *( sub_envided + i + j*(inp_w + w_pad*2)) = 0;
			   }
		   }

		   for(int j=0; j<inp_h; j++){
			   sub_input_line = sub_input + j*(inp_h);
			   sub_envided_line = sub_envided + (j+h_pad) *(inp_w + w_pad*2);
			   for(int i=0; i<inp_w; i++){
				* (sub_envided_line + i + w_pad) = *(sub_input_line + i);
				//  cout<< input[i * inp_w + j] <<" ";
			   }

			   //cout<<std::endl;
		   }

	   }
}

//CPU functions

void dnnFunc::scal_cpu(int N, float ALPHA, float *X, int INCX)
{
    int i;
    for(i = 0; i < N; ++i) X[i*INCX] *= ALPHA;
}

void dnnFunc::copy_cpu(int N, float *X, int INCX, float *Y, int INCY)
{
    int i;
    for(i = 0; i < N; ++i) Y[i*INCY] = X[i*INCX];
}

void dnnFunc::fill_cpu(int N, float ALPHA, float *X, int INCX)
{
    int i;
    for(i = 0; i < N; ++i) X[i*INCX] = ALPHA;
}

void dnnFunc::pow_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY)
{
    int i;
    for(i = 0; i < N; ++i) Y[i*INCY] = pow(X[i*INCX], ALPHA);
}

void dnnFunc::mul_cpu(int N, float *X, int INCX, float *Y, int INCY)
{
    int i;
    for(i = 0; i < N; ++i) Y[i*INCY] *= X[i*INCX];
}

void dnnFunc::const_cpu(int N, float ALPHA, float *X, int INCX)
{
    int i;
    for(i = 0; i < N; ++i) X[i*INCX] = ALPHA;
}

void dnnFunc::axpy_cpu(int N, float ALPHA, float *X, int INCX, float *Y, int INCY)
{
    int i;
    for(i = 0; i < N; ++i) Y[i*INCY] += ALPHA*X[i*INCX];
}

//common functions
void dnnFunc::add_bias(float *output, float *biases, int batch, int n, int size)
{
    int i,j,b;
    for(b = 0; b < batch; ++b){
        for(i = 0; i < n; ++i){
            for(j = 0; j < size; ++j){
                output[(b*n + i)*size + j] += biases[i];
            }
        }
    }
}

void dnnFunc::scale_bias(float *output, float *scales, int batch, int n, int size)
{
    int i,j,b;
    for(b = 0; b < batch; ++b){
        for(i = 0; i < n; ++i){
            for(j = 0; j < size; ++j){
                output[(b*n + i)*size + j] *= scales[i];
            }
        }
    }
}

// Declare the variables for measuring elapsed time
//double sTime;
//double eTime;


double dnnFunc::getMicroSecond( void )
{

    double sec;

    struct timeval timev;      // time value
    struct timezone timez;     // time zone

    if( gettimeofday( &timev, &timez ) == -1 ) {
	std::cerr << "Could not get time by gettimeofday()." << std::endl;
	exit(1);
    }

    // the unit of returned value is second
    sec = static_cast<double>(timev.tv_sec) + static_cast<double>(timev.tv_usec) * 1e-6;

    return sec;

}
