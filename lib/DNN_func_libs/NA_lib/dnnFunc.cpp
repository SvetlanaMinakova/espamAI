#include "dnnFunc.h"
#include <iostream>
#include <math.h>
#include <algorithm>



using namespace std;



void relu(DATA* input, DATA* output, std::map<std::string,int>* int_params_ptr)
{ 
 
  //printf("relu params: input lenght %d\n", int_params_ptr->at("input_len"));
  
  for(int i=0; i<int_params_ptr->at("input_len"); i++) 
      output[i] = input[i]>0? input[i]:0;
  
}

void maxpool(DATA* input, DATA* output, std::map<std::string,int>* int_params_ptr)
{

	int plane = 0;
	int hout = 0;
	int wout = 0;
	int hkern = 0;
	int wkern = 0;
	int _stride0 = int_params_ptr->at("stride");
	int _stride1 = int_params_ptr->at("stride");
	int _of   = int_params_ptr->at("output_dim_2");
	int _of_h = int_params_ptr->at("output_dim_0");
	int _of_w = int_params_ptr->at("output_dim_1");
	int _if   = int_params_ptr->at("input_dim_2");
	int _if_h = int_params_ptr->at("input_dim_0");
	int _if_w = int_params_ptr->at("input_dim_1");
	int _k_h = int_params_ptr->at("k_h");
	int _k_w = int_params_ptr->at("k_w");
	int _pad_h = 0;
	int _pad_w = 0;

	int hin = 0;
	int win = 0;
	bool cond = 0;


  /*printf("maxpool params: kern %d, stride %d, output: %d %d %d, input %d %d %d \n", _k_h, _stride0, _of, _of_h, _of_w,
              _if, _if_h, _if_w);
   */           
              
	for (plane = 0; plane < _of; plane++) {

		/* for output matrix */
		for (hout = 0; hout < _of_h; hout++) {
			int of_h_idx = (plane*_of_h + hout)*_of_w;

                         
			for (wout = 0; wout < _of_w; wout++) {
				bool first_element = true;
				DATA max;
				DATA current;

				/* for kernel matrix */
				for (hkern = 0; hkern < _k_h; hkern++) {
					/* calculate required input position */
					hin = _stride0 * hout + hkern - _pad_h;
					int if_h_idx = (plane*_if_h + hin)*_if_w;

					for (wkern = 0; wkern < _k_w; wkern++) {
						/* calculate required input position */
						win = _stride1 * wout + wkern - _pad_w;

						/* test if position is inside bounds*/
						cond = hin >=0 && win >= 0 && (int) win < _if_w && (int) hin < _if_h;

						/* if outside bounds => set to zero */
						DATA inVal;
						if (cond) {
							//inVal = in[plane][hin][win];
							inVal = input[if_h_idx + win];
						} else {
							inVal = 0;
						}
						current = inVal;

						if(first_element){
							max = current;
							first_element = false;
						} else {
							max = (max > current) ? max : current;
						}
					}
				}

				output[of_h_idx + wout] = max;
			}
		}
	}
}




void execute_dense_block (std::string function,DATA* input, DATA* output, DATA* weights, DATA* bias,    std::map<std::string,int>* int_params_ptr )
{   

	   int input_len = 1;
	   for(int i=0; i<int_params_ptr->at("input_dims");i++)
		  input_len *= int_params_ptr->at(("input_dim_"+std::to_string(i)));
     int output_len = int_params_ptr->at("output_dim_0");
     //printf("dense params: output: %d , input %d \n", int_params_ptr->at("output_dim_0"), int_params_ptr->at("input_dim_0"));  

	   //::weight_and_sum(int *input, int *weights, int *result, int input_len){
	   weight_and_sum(input, output, weights, bias, input_len, output_len);
	   
	   /** TODO extend by any NonLinear function, not only softmax*/
//	   if (function.find("SOFTMAX") != std::string::npos)
//		   softmax(input, input_len);
	   

}

/**
 * Execute convolutional function
 * TODO: optimize, support nD input tensors
 */
void execute_conv (DATA* input, DATA* output, DATA* weights,  DATA* bias, std::map<std::string,int>* int_params_ptr )
    {  

		   int stride;
		   int k_h;
		   int k_w;
		   int input_ch;
		   int neurons;
		   int input_h;
		   int input_w;
		   int output_ch;
		   int output_h;
		   int output_w;
		   
		   k_h = int_params_ptr->at("k_h");
		   k_w = int_params_ptr->at("k_w");
		   stride =  int_params_ptr->at("stride");
		   neurons =  int_params_ptr->at("neurons");

		   output_h = int_params_ptr->at("output_dim_0");
			 output_w  = int_params_ptr->at("output_dim_1");
			 output_ch  = int_params_ptr->at("output_dim_2");

			 input_w = int_params_ptr->at("input_dim_1");
			 input_ch = int_params_ptr->at("input_dim_2");
			 input_h = int_params_ptr->at("input_dim_0");

			 conv_sw(input, output, weights, bias, input_ch, input_h, input_w, output_ch, output_h, output_w, k_h, k_w, stride);

    }
    
    

int conv_sw(DATA* input, DATA* output, DATA* kernel, DATA* bias, int input_ch, int input_h, int input_w, int output_ch, 
            int output_h, int output_w, int k_h, int k_w, int stride)
{
  
  unsigned int OF = output_ch;
  unsigned int IF = input_ch;
  int pad = 0;

if(k_h == 3)
  pad = 1;
else if (k_h == 5)
  pad = 2;
else pad = k_h/2;
  
    /*   printf("conv params: kern %d, pad %d, stride %d, output: %d %d %d, input %d %d %d\n", k_h, pad, stride, output_ch, 
              output_h, output_w, input_ch, input_h, input_w);
                
*/

#ifdef _FIXED_
  long long int mac;
  int if_count = 0;
#else
  float mac;
#endif

  DATA bi, current, curr_kern;

  // foreach output plane
  for (int pout = 0; pout < OF; pout++) {
    //printf("OF %d \n\n", pout);

    // buffer bias 
    bi = bias[pout];

    // for output matrix 
    for (int hout = 0; hout < output_h; hout++) {
      for (int wout = 0; wout < output_w; wout++) {

        // initialise multiply-accumulate to bias 
      #ifdef _FIXED_
        mac = ((long long int)bi) << QF;
      #else
        mac = bi;
      #endif

        // foreach input plane 
        for (int pin = 0; pin < IF; pin++) {

          // for kernel matrix 
          for (int hkern = 0; hkern < k_h; hkern++) {
          

            // calculate required input position 
            int hin = stride * hout + hkern - pad;

            // test if position is inside bounds
            bool cond = hin >= 0 && (int) hin < input_h;

            // if outside bounds => continue 
            if (cond == 0)
              continue;

            for (int wkern = 0; wkern < k_w; wkern++) {

              // calculate required input position 
              int win = stride * wout + wkern - pad;

              // test if position is inside bounds
              cond = win >= 0 && (int) win < input_w;

              // if outside bounds => continue 
              if (cond == 0)
                continue;

              current = input[(pin*input_h + hin)*input_w + win];
              curr_kern = kernel[((pout*IF + pin)*k_h + hkern)*k_w + wkern]; 
              mac += current * curr_kern;


            }
          }
        

#ifdef _FIXED_
        mac = (saturate(mac >> QF, "Conv")) << QF;
#endif
        }

        #ifdef _FIXED_
          output[(pout*output_h + hout)*output_w + wout] = (DATA)(mac >> QF);
        #else
          output[(pout*output_h + hout)*output_w + wout] = mac;
        #endif

      }
    } 
  }
  return 0;
}



/**
 * element-wise mult input * weights, save result in result; Analogue to MatMul?
 */
void weight_and_sum(DATA *input, DATA *result, DATA *weights, DATA* bias, int input_len, int output_len)
{ 


  #ifdef _FIXED_
    long long int mac;
  #else
    float mac;
  #endif  


	for (int i=0; i<output_len; i++){
	
	  #ifdef _FIXED_
      mac = ((long long int)bias[i]) << QF;
    #else
      mac = bias[i];
    #endif
	  for(int j=0; j<input_len; j++){
		  mac += input[j] * weights[j+i*input_len];
	  }

	  #ifdef _FIXED_
      result[i] = (DATA)((saturate(mac >> QF, "Conv")) << QF >> QF);
    #else
      result[i] = mac;
    #endif
	}
}

/**
 * element-wise mult input * weights, save result in result;
 */
void weight_and_sum(DATA *input, DATA *weights, DATA *result, int input_len){
	*result = 0;
	for(int i=0; i<input_len; i++)
		*result +=*(input+i) * *(weights+i);
}



void softmax(DATA *input, int n, DATA *output)
{
    int i;
    float sum = 0;
    float largest = -DATA_MAX;
    for(i = 0; i < n; ++i){
        if(input[i] > largest) largest = input[i];
    }
    for(i = 0; i < n; ++i){
        float e = exp(input[i] - largest);
        sum += e;
        output[i] = (DATA) e;
    }
    #ifdef _FIXED_
      for(i = 0; i < n; ++i){
          output[i] = round(output[i]/sum);
    #else
      for(i = 0; i < n; ++i){
          output[i] /= sum;
    #endif
    }
}


void classify(DATA* input, int len, std::vector<int> labels)
{
   std::pair<int,int> index(get_max(input, len));
   int perc1 = round(input[index.first]*100);
   int perc2 = round(input[index.second]*100);
   
   cout<<"\nClassified as: "<<endl;
   #ifndef _FIXED_
    cout<<"["<<perc1<<"%] "<<labels[index.first]<<endl;
    cout<<"["<<perc2<<"%] "<<labels[index.second]<<endl;
   #else
    cout<<labels[index.first]<<endl;
    cout<<labels[index.second]<<endl;
   #endif

}


std::pair<int,int> get_max(DATA* input, int len)
{
  std::vector<DATA> array;
 
  for(int i=0; i<len; i++)
    array.push_back(input[i]); 

  int index1 = std::distance(array.begin(), std::max_element(array.begin(), array.end()));
  array[index1] = - DATA_MAX;
  int index2 = std::distance(array.begin(), std::max_element(array.begin(), array.end()));
  return std::make_pair (index1, index2);
}


float get_softmax_summ(float *non_activated_stages, int len)
{
	float softmax_summ = 0;
	for( int i=0;i<len;i++)
		softmax_summ+=exp( *(non_activated_stages + i));
	return softmax_summ;
}

void init_zeros(DATA *matrix, int h, int w){
	for (int j = 0; j < h; j++){
		for (int i = 0; i < w; i++){
			matrix[i * w + j]=0;
		}
	}}

void init_zeros(DATA *matrix, int d, int h, int w){
	for (int k = 0; k < d; k++){
		for (int j = 0; j < h; j++){
			for (int i = 0; i < w; i++){
				matrix[i * w + j]=0;
				}
	}		}
}


void transpose(DATA *input, int inp_h, int inp_w){
	DATA tmp[inp_w][inp_h] = {0};

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


static inline long long int saturate(long long int mac, const char* module)
{

	if(mac > _MAX_) {
		return _MAX_;
	}

	if(mac < _MIN_){
		return _MIN_;
	}
	
    return mac;
}

