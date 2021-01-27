// File automatically generated by ESPAM

#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>
#include <map>
#include "appFunc.h"
#include "types.h"
#include "dnnFunc.h"
#include <cstddef>
using namespace std;

appFunc::appFunc() {}
appFunc::~appFunc() {}

// Execution function primitive

  /** simplest exec MoC 
  (only function name is a parameter)*/ 
   void appFunc::execute (std::string function)
    {
      // cout<<function<<endl;
    }

  /** TODO: place an API to your CNN operators library here*/
   void appFunc::execute (std::string function,std::map<std::string,float*>* tensor_params_ptr, std::map<std::string,int>* int_params_ptr )
    {
      bool op_done = false;
      if (function.find("SOFTMAX") != std::string::npos) {
        dnnFunc::softmax(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if ((function.find("ReLU") != std::string::npos) || 
	(function.find("THN") != std::string::npos) || 
	(function.find("SIGM") != std::string::npos) || 
	(function.find("LeakyReLu") != std::string::npos) ){
        dnnFunc::activation(function, int_params_ptr, tensor_params_ptr);
        op_done=true;
      }
      if (function.find("CONV") != std::string::npos) {
        dnnFunc::conv(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("MAXPOOL") != std::string::npos) {
        dnnFunc::maxpool(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("AVGPOOL") != std::string::npos) {
        dnnFunc::avgpool(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("DENSEBLOCK") != std::string::npos || function.find("MATMUL") != std::string::npos ||function.find("GEMM") != std::string::npos) {
         dnnFunc::gemm( int_params_ptr, tensor_params_ptr);       
        op_done=true;
      }
      if (function.find("LRN") != std::string::npos) {
         dnnFunc::lrn(int_params_ptr,tensor_params_ptr); 
        op_done=true;
      }
      if (function.find("BN") != std::string::npos) {
        dnnFunc::batch_normalization(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("SUBConst") != std::string::npos) {
        dnnFunc::sub_const(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("DIVConst") != std::string::npos) {
        dnnFunc::div_const(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }
      if (function.find("MULconst") != std::string::npos) {
        dnnFunc::mul_const(int_params_ptr,tensor_params_ptr);
        op_done=true;
      }

      if(!op_done){
        std::cout<<function<<" operation not found!"<<std::endl;
      }
    }

/**
Transpose matrix
@param input : matrix to transpose
@param inp_h : input matrix height
@param inp_w : input matrix width
*/
 void appFunc::transpose(float *input, int inp_h, int inp_w) {
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
  
}


/**
* execution moc (for communication checkout) 
* set output to (first_element_of_input + 1)
* input -input data ptr
* output - output data ptr
* outp_len - output data length
*/
  void appFunc::communication_moc(float* input, float* output, int inp_len, int outp_len){
  int to_fill = std::min(inp_len, outp_len);
  for(int i=0;i<to_fill;i++)
    output[i] = input[i]+1;
}
  /**
    * copies 2D-data line from src to dst.
    * data_h - src data height
    * data_w - src data width
    * src - pointer to first data source array element
    * dst - pointer to first copy destination array element
  */
   void appFunc::cpy_2D_data_line(const int &data_w, float *src,float *dst, const int &line_id)
    {
      int line_start = line_id * data_w;
      for (int i = 0; i < data_w ; i++)
        dst[i] = src[line_start];
    }
  /**
  Data shift functions (for shifting overlapping data in I/O arrays)
  @param array : I/O overlapping array
  @param dim   : I/O overlapping array dimensionality
  */
  /**
    * Moves 2D data on n lines to top.
    * Required for overlapping data.
    * h - array height
    * w - array width
    * x - pointer to first array element
  */
   void appFunc::shift_2D (const int &h, const int &w, float *x, const int &stride)
    {
      for(int line_ind = stride; line_ind < w ; line_ind ++){
        for(int i=0; i<w; i++)
          x[i + (line_ind - stride)* w] = x[i + line_ind * w];
        }
    }
  /**
    * Moves 3D data on n lines to top.
    * Required for overlapping data.
    * d - array depth
    * h - array height
    * w - array width
    * x - pointer to first array element
  */
   void appFunc::shift_3D (const int &d, const int &h, const int &w, float *x, const int &stride)
    {
      int start_elem_id = 0;
      for(int depth=0; depth < d; depth++ ){
        for(int line_ind = stride; line_ind < w ; line_ind ++){
          for(int i=0; i<w; i++)
            x[i + (line_ind - stride)* w + start_elem_id] = x[i + line_ind * w + start_elem_id];
          }
        start_elem_id +=w*h;
      }
    }
  // function to show first num values of array
   void appFunc::show_val(float *x, int xlen, int num){
    for (int i = 0; i < std::min(xlen,num); i++)
      std::cout << x[i] << ' ';
    std::cout << std::endl;
  }
  //2D array print function, type: float
   void appFunc::print_2D(const int &h, const int &w, float *x)
    {
      for (int i = 0; i < h; i++){
        for (int j = 0; j < w ; j++)
          std::cout << x[i * w + j] << ' ';
        std::cout<<endl;
      }
    }
  //3D array print function, type: float
   void appFunc::print_3D(const int &d, const int &h, const int &w, float *x)
    {
      int start_elem_id = 0;
      for(int depth=0; depth < d; depth++ ){
        std::cout<<"depth"<<depth<<endl;
        for (int i = 0; i < h; i++){
          for (int j = 0; j < w ; j++)
            std::cout << x[i * w + j + start_elem_id] << ' ';
          std::cout<<endl;
        }
        start_elem_id +=w*h;
      }
    }

  // get fifo buffer by src
  fifo_buf* appFunc::get_buf_by_src (std::string name, std::vector<fifo_buf>& fifos){
    for (auto & fifos_elem: fifos)  {
      if (name.compare(fifos_elem.src) == 0)
        return &fifos_elem;
      }
      return nullptr;
  }

  // get fifo buffer by dst
  fifo_buf* appFunc::get_buf_by_dst (std::string name, std::vector<fifo_buf>& fifos){
    for (auto & fifos_elem: fifos)  {
      if (name.compare(fifos_elem.dst) == 0)
        return &fifos_elem;
      }
      return nullptr;
  }