// File automatically generated by ESPAM

#ifndef Convolution110_H
#define Convolution110_H
#include "csdfNode.h"
#include <map>

class Convolution110 : public csdfNode{
public:
    Convolution110();
    virtual ~Convolution110();

    void main(void *threadarg) override;
    // specific const parameters definition
    std::map<std::string,int> int_params;
  const int neurons = 16;

  //FIFO sizes
    int IP0_fifo_size;
    int OP1_fifo_size;

  const int input_dims = 3;
  //input array definition
    const int input_dim_0 = 8;
    const int input_dim_1 = 5;
    const int input_dim_2 = 14;
    int input[8][5][14] = {{{0}}};

  const int output_dims = 3;
  //output array definition
    const int output_dim_0 = 16;
    const int output_dim_1 = 1;
    const int output_dim_2 = 14;
    int output[16][1][14] = {{{0}}};

  //weights array definition
    const int weights_dim_0 = 16;
    const int weights_dim_1 = 8;
    const int weights_dim_2 = 5;
    const int weights_dim_3 = 5;
    int weights[16][8][5][5] = {{{{0}}}};

//const parameters
  const int k_h = 5;
  const int k_w = 5;
  const int stride = 1;
  //specific node parameters and functions
};
#endif // Convolution110_H