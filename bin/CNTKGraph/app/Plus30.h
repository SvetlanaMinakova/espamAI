// File automatically generated by ESPAM

#ifndef Plus30_H
#define Plus30_H
#include "csdfNode.h"
#include <map>

class Plus30 : public csdfNode{
public:
    Plus30();
    virtual ~Plus30();

    void main(void *threadarg) override;
    // specific const parameters definition
    std::map<std::string,int> int_params;
  const int neurons = 8;

  //FIFO sizes
    int IP0_fifo_size;
    int OP0_fifo_size;

  const int input_dims = 1;
  //input array definition
    const int input_dim_0 = 224;
    int input[224] = {0};

  const int output_dims = 1;
  //output array definition
    const int output_dim_0 = 224;
    int output[224] = {0};

  //specific node parameters and functions
};
#endif // Plus30_H
