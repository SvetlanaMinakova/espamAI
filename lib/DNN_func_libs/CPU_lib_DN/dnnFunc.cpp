/*
 * dnnFunc.cpp
 *
 *  Created on: 2 Jul 2019
 *      Author: minakovas
 */

#include "dnnFunc.h"
#include <map>
#include <math.h>
#include <cstdint>
#include <limits>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
using namespace std;

dnnFunc::dnnFunc() { }

dnnFunc::~dnnFunc() { }

//void dnnFunc::hello_from_darknet();

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

///////////////////////////////
//            LRN          ///

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

void dnnFunc::softmax_cpu(float *input, int n, int batch, int batch_offset, int groups, int group_offset, int stride, float temp, float *output)
{
    int g, b;
    for(b = 0; b < batch; ++b){
        for(g = 0; g < groups; ++g){
            softmax(input + b*batch_offset + g*group_offset, n, temp, stride, output + b*batch_offset + g*group_offset);
        }
    }
}

void dnnFunc::softmax(float *input, int n, float temp, int stride, float *output)
{   float FLT_MAX = std::numeric_limits<float>::max();
    int i;
    float sum = 0;
    float largest = -FLT_MAX;
    for(i = 0; i < n; ++i){
        if(input[i*stride] > largest) largest = input[i*stride];
    }
    for(i = 0; i < n; ++i){
        float e = exp(input[i*stride]/temp - largest/temp);
        sum += e;
        output[i*stride] = e;
    }
    for(i = 0; i < n; ++i){
        output[i*stride] /= sum;
    }
}

///////////////////////////////
//            CONV          //
//void  darknet_conv::forward_convolutional_layer(const &int n, network net)
void dnnFunc::conv(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){
   //parameters preparation
   int batch = int_params_ptr->at("batch");
   int groups = int_params_ptr->at("groups");

   //std::cout<<"batch: "<<batch<<", groups: "<<groups<<std::endl;
   
   int size = int_params_ptr->at("k_w");
   int stride = int_params_ptr->at("stride");

   //std::cout<<"size: "<<size<<", stride: "<<stride<<std::endl;

   int ch = int_params_ptr->at("channels");
   int h = int_params_ptr->at("h");
   int w = int_params_ptr->at("w");

   //std::cout<<"ch: "<<ch<<",h: "<<h<<",w: "<<w<<std::endl;

   int neurons = int_params_ptr->at("neurons");
   int out_h = int_params_ptr->at("out_h");
   int out_w = int_params_ptr->at("out_w");
   int outputs = int_params_ptr->at("output_len");

   float *input = tensor_params_ptr->at("input");
   float *output = tensor_params_ptr->at("output");
   float *weights = tensor_params_ptr->at("weights");
   float *biases = tensor_params_ptr->at("bias");
   float *networkspace = tensor_params_ptr->at("networkspace");

   int netwspace_len = int_params_ptr->at("networkspace_len");

    int pad =0;
    int pads = int_params_ptr->at("pads");
    if (pads)
	pad = int_params_ptr->at("pad_w0");

   int nweights = ch/groups * neurons * h * w;

   //darknet function
   int i, j;
   fill_cpu(outputs*batch, 0, output, 1);

    if(int_params_ptr->at("xnor")){ //binary weighs, inputs and outputs, not suppotred currently
        //binarize_weights(l.weights, l.n, l.c/l.groups*l.size*l.size, l.binary_weights);
        //swap_binary(&l);
        //binarize_cpu(net.input, l.c*l.h*l.w*l.batch, l.binary_input);
        //net.input = l.binary_input;
    }
       fill_cpu(netwspace_len, 0, networkspace, 1); //fill output with zeros

       int m = neurons/groups;
       int k = size*size*ch/groups;
       int n = out_w*out_h;
       for(i = 0; i < batch; ++i){
    	   //std::cout<<"lbatch "<<i<<std::endl;
           for(j = 0; j < groups; ++j){
               float *a = weights + j*nweights/groups;
               float *b = networkspace;//net.workspace;
               float *c = output + (i*groups + j)*n*m;
               float *im =  input;//+ (i*lgroups + j)*lc/lgroups*lh*lw;

               if (size == 1) {
                   b = im;
               } else {
                   im2col_cpu(im, ch/groups, h, w, size, stride, pad, b);
               }
               gemm(0,0,m,n,k,1,a,k,b,n,1,c,n);
              // appFunc::show_val(loutput,loutputs,loutputs);
           }
       }

         if(int_params_ptr->at("batchnormalize")){
          // forward_batchnorm_layer(l, net);
       } else {
    	   if(biases!=nullptr)
    		   add_bias(output, biases, batch, neurons, out_h*out_w);
       }

       //activate_array(l.output, l.outputs*l.batch, l.activation);
       //if(l.binary || l.xnor) swap_binary(&l);
   }

//CPU functions
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

float im2col_get_pixel(float *im, int height, int width, int channels,
                        int row, int col, int channel, int pad)
{
    row -= pad;
    col -= pad;

    if (row < 0 || col < 0 ||
        row >= height || col >= width) return 0;
    return im[col + width*(row + height*channel)];
}

//From Berkeley Vision's Caffe!
//https://github.com/BVLC/caffe/blob/master/LICENSE
void dnnFunc::im2col_cpu(float* data_im,
     int channels,  int height,  int width,
     int ksize,  int stride, int pad, float* data_col)
{
    int c,h,w;
    int height_col = (height + 2*pad - ksize) / stride + 1;
    int width_col = (width + 2*pad - ksize) / stride + 1;

    int channels_col = channels * ksize * ksize;
    for (c = 0; c < channels_col; ++c) {
        int w_offset = c % ksize;
        int h_offset = (c / ksize) % ksize;
        int c_im = c / ksize / ksize;
        for (h = 0; h < height_col; ++h) {
            for (w = 0; w < width_col; ++w) {
                int im_row = h_offset + h * stride;
                int im_col = w_offset + w * stride;
                int col_index = (c * height_col + h) * width_col + w;
                data_col[col_index] = im2col_get_pixel(data_im, height, width, channels,
                        im_row, im_col, c_im, pad);
                //std::cout<<"data_col["<<col_index<<"]= "<< data_col[col_index]<<std::endl;
            }
        }
    }
}



//////////////////////////////
//            GEMM          //

void dnnFunc::gemm(std::map<std::string,int>* int_params_ptr, std::map<std::string,float*>* tensor_params_ptr){

	//default mode in GEMM layers, forced by the weights/pthread generator
	int TA = 0;
	int TB = 1;

	int M = 1; //always by definition of Gemm layer
	int N = int_params_ptr->at("output_len");
	int K = int_params_ptr->at("input_len");

	float *A = tensor_params_ptr->at("input");
	float *B = tensor_params_ptr->at("weights");
	float *C = tensor_params_ptr->at("output");
	float *bias = tensor_params_ptr->at("bias");

	int lda = int_params_ptr->at("input_len");
	int ldb = int_params_ptr->at("input_len");
	int ldc = int_params_ptr->at("output_len");

	//std::cout<<"TA = "<<TA<<",TB = "<<TB<<",M = "<<M<<",N= "<<N<<",K = "<<K<<",lda = "<<lda<<", ldb = "<<ldb<<", ldc = "<<ldc<<std::endl;
        //std::cout<<"C ["<<M<<" x "<<N<<"] = input ["<<M<<" x "<<K<<"] x weights ["<<N<<" x "<<K<<"]^T "<<std::endl;

	if(bias==nullptr)
		fill_cpu(ldc, 0, C, 1); //fill output with zeros
	else
		copy_cpu(ldc,bias,1,C,1);

    dnnFunc::gemm(TA,TB,M,N,K,1,
    		A, lda,
    		B, ldb,
    		1,
			C, ldc);

}

void dnnFunc::gemm(int TA, int TB, int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float BETA,
        float *C, int ldc)
{
    gemm_cpu( TA,  TB,  M, N, K, ALPHA,
    		A,lda,
			B, ldb,
			BETA,
			C,ldc);
}

void dnnFunc::gemm_cpu(int TA, int TB, int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float BETA,
        float *C, int ldc)
{
    //printf("cpu: %d %d %d %d %d %f %d %d %f %d\n",TA, TB, M, N, K, ALPHA, lda, ldb, BETA, ldc);
    int i, j;
    for(i = 0; i < M; ++i){
        for(j = 0; j < N; ++j){
            C[i*ldc + j] *= BETA;
        }
    }
    if(!TA && !TB)
        gemm_nn(M, N, K, ALPHA,A,lda, B, ldb,C,ldc);
    else if(TA && !TB)
        gemm_tn(M, N, K, ALPHA,A,lda, B, ldb,C,ldc);
    else if(!TA && TB)
        gemm_nt(M, N, K, ALPHA,A,lda, B, ldb,C,ldc);
    else
        gemm_tt(M, N, K, ALPHA,A,lda, B, ldb,C,ldc);
}

void dnnFunc::gemm_nn(int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float *C, int ldc)
{
    int i,j,k;
   #pragma omp parallel for
    for(i = 0; i < M; ++i){
        for(k = 0; k < K; ++k){
            register float A_PART = ALPHA*A[i*lda+k];
            for(j = 0; j < N; ++j){
                C[i*ldc+j] += A_PART*B[k*ldb+j];
            }
        }
    }
}

void dnnFunc::gemm_nt(int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float *C, int ldc)
{
    int i,j,k;
   #pragma omp parallel for
    for(i = 0; i < M; ++i){
        for(j = 0; j < N; ++j){
            register float sum = 0;
            for(k = 0; k < K; ++k){
                sum += ALPHA*A[i*lda+k]*B[j*ldb + k];
            }
            C[i*ldc+j] += sum;
        }
    }
}

void dnnFunc::gemm_tn(int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float *C, int ldc)
{
    int i,j,k;
    #pragma omp parallel for
    for(i = 0; i < M; ++i){
        for(k = 0; k < K; ++k){
            register float A_PART = ALPHA*A[k*lda+i];
            for(j = 0; j < N; ++j){
                C[i*ldc+j] += A_PART*B[k*ldb+j];
            }
        }
    }
}

void dnnFunc::gemm_tt(int M, int N, int K, float ALPHA,
        float *A, int lda,
        float *B, int ldb,
        float *C, int ldc)
{
    int i,j,k;
    #pragma omp parallel for
    for(i = 0; i < M; ++i){
        for(j = 0; j < N; ++j){
            register float sum = 0;
            for(k = 0; k < K; ++k){
                sum += ALPHA*A[i+k*lda]*B[k+j*ldb];
            }
            C[i*ldc+j] += sum;
        }
    }
}
