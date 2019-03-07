// File automatically generated by ESPAM

#ifndef Plus214_H
#define Plus214_H
#include "csdfNode.h"
#include <map>

class Plus214 : public csdfNode{
public:
    Plus214();
    virtual ~Plus214();

    void main(void *threadarg) override;
    // specific const parameters definition
    std::map<std::string,int> int_params;
  const int neurons = 1;

  //FIFO sizes
    int IP0_fifo_size;
    int OP0_fifo_size;

  const int input_dims = 1;
  //input array definition
    const int input_dim_0 = 10;
    int input[10] = {0};

  const int output_dims = 1;
  //output array definition
    const int output_dim_0 = 10;
    int output[10] = {0};

  //specific node parameters and functions
};
#endif // Plus214_H